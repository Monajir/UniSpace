package com.techManiacs.UniSpace.repository;

import com.techManiacs.UniSpace.model.Booking;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookingRepo extends MongoRepository<Booking, ObjectId> {
    List<Booking> findAllByClassroomId(ObjectId classroomId);
    List<Booking> findAllByStatus(String status);
    List<Booking> findAllByUserId(ObjectId userId);
}
