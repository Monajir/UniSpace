package com.techManiacs.UniSpace.repository;

import com.techManiacs.UniSpace.model.Profile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProfileRepo extends MongoRepository<Profile, ObjectId> {
    Profile findByEmail(String email);
}
