package com.techManiacs.UniSpace.repository;

import com.techManiacs.UniSpace.model.Classroom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassroomRepo extends JpaRepository<Classroom, UUID> {
    List<Classroom> findAllByIsAvailable(Boolean isAvailable);

    @Query("select (count(classroom) > 0) from Classroom classroom "
            + "where lower(classroom.room_number) = lower(:roomNumber)")
    boolean existsByRoomNumberIgnoreCase(@Param("roomNumber") String roomNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Classroom c where c.id = :id")
    Optional<Classroom> findByIdForUpdate(@Param("id") UUID id);
}
