package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.model.Classroom;
import com.techManiacs.UniSpace.repository.ClassroomRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ClassroomService {

    @Autowired
    private ClassroomRepo classroomRepo;

    public List<Classroom> getAllAvailableClassrooms() {
        return classroomRepo.findAllByIsAvailable(true);
    }

    public String getClassroomNameById(ObjectId id) {
        Optional<Classroom> classroom = classroomRepo.findById(id);
        return classroom.map(Classroom::getRoom_number).orElse(null);
    }
}
