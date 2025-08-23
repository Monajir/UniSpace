package com.techManiacs.UniSpace.repository;

import com.techManiacs.UniSpace.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    User findByName(String name);
    User findByEmail(String email);
    Optional<User> findById(ObjectId id);
}