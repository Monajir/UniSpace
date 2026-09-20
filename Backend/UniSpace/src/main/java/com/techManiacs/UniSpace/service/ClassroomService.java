package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.exception.ApiException;
import com.techManiacs.UniSpace.model.Classroom;
import com.techManiacs.UniSpace.repository.BookingRepo;
import com.techManiacs.UniSpace.repository.ClassroomRepo;
import com.techManiacs.UniSpace.repository.RoutineRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ClassroomService {

    private final ClassroomRepo classroomRepo;
    private final ClassroomCatalogueCache catalogueCache;
    private final BookingRepo bookingRepo;
    private final RoutineRepo routineRepo;

    public ClassroomService(
            ClassroomRepo classroomRepo,
            ClassroomCatalogueCache catalogueCache,
            BookingRepo bookingRepo,
            RoutineRepo routineRepo) {
        this.classroomRepo = classroomRepo;
        this.catalogueCache = catalogueCache;
        this.bookingRepo = bookingRepo;
        this.routineRepo = routineRepo;
    }

    @Transactional(readOnly = true)
    public List<Classroom> getAllAvailableClassrooms() {
        return catalogueCache.get().orElseGet(() -> {
            List<Classroom> classrooms = classroomRepo.findAllByIsAvailable(true);
            catalogueCache.put(classrooms);
            return classrooms;
        });
    }

    @Transactional(readOnly = true)
    public String getClassroomNameById(UUID id) {
        return classroomRepo.findById(id).map(Classroom::getRoom_number).orElse(null);
    }

    @Transactional
    public Classroom createClassroom(Classroom classroom) {
        normalizeAndValidate(classroom);
        if (classroomRepo.existsByRoomNumberIgnoreCase(classroom.getRoom_number())) {
            throw new ApiException(HttpStatus.CONFLICT, "A classroom with this room number already exists");
        }
        classroom.setIsAvailable(true);
        return classroomRepo.saveAndFlush(classroom);
    }

    @Transactional
    public Classroom updateClassroom(UUID id, Classroom changes) {
        Classroom classroom = classroomRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Classroom not found"));
        normalizeAndValidate(changes);
        if (!classroom.getRoom_number().equalsIgnoreCase(changes.getRoom_number())
                && classroomRepo.existsByRoomNumberIgnoreCase(changes.getRoom_number())) {
            throw new ApiException(HttpStatus.CONFLICT, "A classroom with this room number already exists");
        }

        classroom.setRoom_number(changes.getRoom_number());
        classroom.setBuilding(changes.getBuilding());
        classroom.setCapacity(changes.getCapacity());
        classroom.setEquipment(new ArrayList<>(changes.getEquipment()));
        return classroomRepo.saveAndFlush(classroom);
    }

    @Transactional
    public void deleteClassroom(UUID id) {
        Classroom classroom = classroomRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Classroom not found"));
        if (bookingRepo.existsByClassroomId(id) || routineRepo.existsByClassroomId(id)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Classrooms with booking or routine history cannot be deleted");
        }
        classroomRepo.delete(classroom);
        classroomRepo.flush();
    }

    private void normalizeAndValidate(Classroom classroom) {
        if (classroom.getRoom_number() == null || classroom.getRoom_number().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Room number is required");
        }
        if (classroom.getBuilding() == null || classroom.getBuilding().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Building is required");
        }
        if (classroom.getCapacity() == null || classroom.getCapacity() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Capacity must be greater than zero");
        }

        String roomNumber = classroom.getRoom_number().trim();
        String building = classroom.getBuilding().trim();
        if (roomNumber.length() > 255 || building.length() > 255) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Room number and building cannot exceed 255 characters");
        }
        List<String> equipment = classroom.getEquipment() == null
                ? List.of()
                : classroom.getEquipment().stream()
                        .filter(item -> item != null && !item.isBlank())
                        .map(String::trim)
                        .distinct()
                        .toList();
        if (equipment.stream().anyMatch(item -> item.length() > 255)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Equipment names cannot exceed 255 characters");
        }

        classroom.setRoom_number(roomNumber);
        classroom.setBuilding(building);
        classroom.setEquipment(new ArrayList<>(equipment));
    }

    public Classroom lockAvailableClassroom(UUID id) {
        return verifyAvailable(classroomRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Classroom not found")));
    }

    private Classroom verifyAvailable(Classroom classroom) {
        if (!Boolean.TRUE.equals(classroom.getIsAvailable())) {
            throw new ApiException(HttpStatus.CONFLICT, "Classroom is not available for booking");
        }
        return classroom;
    }
}
