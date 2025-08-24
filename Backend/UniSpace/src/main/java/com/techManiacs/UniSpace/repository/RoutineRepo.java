package com.techManiacs.UniSpace.repository;

import com.techManiacs.UniSpace.model.Routine;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RoutineRepo extends MongoRepository<Routine, ObjectId> {
    List<Routine> findAllByClassroomId(ObjectId classroomId);

    List<Routine> findAllByProgramAndSemesterAndSection(String program, Integer semester, Integer section);
}
