package com.techManiacs.UniSpace.migration;

import com.techManiacs.UniSpace.model.Booking;
import com.techManiacs.UniSpace.model.Classroom;
import com.techManiacs.UniSpace.model.PendingRole;
import com.techManiacs.UniSpace.model.Profile;
import com.techManiacs.UniSpace.model.Routine;
import com.techManiacs.UniSpace.model.User;

import java.util.List;
import java.util.Map;

public record MigrationPlan(
        List<User> users,
        List<Profile> profiles,
        List<Classroom> classrooms,
        List<Routine> routines,
        List<Booking> bookings,
        List<PendingRole> pendingRoles,
        Map<String, Map<String, String>> idMappings,
        List<MigrationIssue> issues
) {
    public boolean hasErrors() {
        return issues.stream().anyMatch(issue -> issue.severity() == MigrationIssue.Severity.ERROR);
    }

    public Map<String, Integer> targetCounts() {
        return Map.of(
                "users", users.size(),
                "profiles", profiles.size(),
                "classrooms", classrooms.size(),
                "routines", routines.size(),
                "bookings", bookings.size(),
                "pending_roles", pendingRoles.size()
        );
    }
}
