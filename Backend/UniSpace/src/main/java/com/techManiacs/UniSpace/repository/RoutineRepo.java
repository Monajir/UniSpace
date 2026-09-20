package com.techManiacs.UniSpace.repository;

import com.techManiacs.UniSpace.model.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoutineRepo extends JpaRepository<Routine, UUID> {
    List<Routine> findAllByClassroomId(UUID classroomId);
    boolean existsByClassroomId(UUID classroomId);
    List<Routine> findAllByProgramAndSemesterAndSection(String program, Integer semester, Integer section);
}
