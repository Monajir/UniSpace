package com.techManiacs.UniSpace.migration;

import java.util.List;

public enum LegacyCollection {
    USERS("users", List.of("users.json", "user.json")),
    PROFILES("profiles", List.of("profiles.json", "profile.json")),
    CLASSROOMS("classrooms", List.of("classrooms.json", "classroom.json")),
    ROUTINES("routines", List.of("routines.json", "routine.json")),
    BOOKINGS("bookings", List.of("bookings.json", "booking.json")),
    PENDING_ROLES("pending_roles", List.of("pending_roles.json", "pendingRole.json", "pendingrole.json"));

    private final String reportName;
    private final List<String> fileNames;

    LegacyCollection(String reportName, List<String> fileNames) {
        this.reportName = reportName;
        this.fileNames = fileNames;
    }

    public String reportName() {
        return reportName;
    }

    public List<String> fileNames() {
        return fileNames;
    }
}
