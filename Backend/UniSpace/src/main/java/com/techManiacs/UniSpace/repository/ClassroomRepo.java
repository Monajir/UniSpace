package com.techManiacs.UniSpace.repository;

import com.techManiacs.UniSpace.model.Classroom;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepo extends MongoRepository<Classroom, ObjectId> {
    List<Classroom> findAllByIsAvailable(Boolean isAvailable);
    Optional<Classroom> findById(ObjectId id);

}
