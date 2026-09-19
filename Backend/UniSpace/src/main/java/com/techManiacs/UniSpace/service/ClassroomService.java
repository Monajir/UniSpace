package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.exception.ApiException;
import com.techManiacs.UniSpace.model.Classroom;
import com.techManiacs.UniSpace.repository.ClassroomRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClassroomService {

    private final ClassroomRepo classroomRepo;
    private final ClassroomCatalogueCache catalogueCache;

    public ClassroomService(ClassroomRepo classroomRepo, ClassroomCatalogueCache catalogueCache) {
        this.classroomRepo = classroomRepo;
        this.catalogueCache = catalogueCache;
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

    @Transactional(readOnly = true)
    public Classroom requireAvailableClassroom(UUID id) {
        return verifyAvailable(classroomRepo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Classroom not found")));
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
