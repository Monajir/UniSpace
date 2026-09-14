package com.techManiacs.UniSpace.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.techManiacs.UniSpace.domain.BookingStatus;
import com.techManiacs.UniSpace.domain.Role;
import com.techManiacs.UniSpace.model.Booking;
import com.techManiacs.UniSpace.model.Classroom;
import com.techManiacs.UniSpace.model.PendingRole;
import com.techManiacs.UniSpace.model.Profile;
import com.techManiacs.UniSpace.model.Routine;
import com.techManiacs.UniSpace.model.User;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class LegacyMigrationTransformer {
    public MigrationPlan transform(LegacyExport legacyExport) {
        List<MigrationIssue> issues = new ArrayList<>();
        if (legacyExport.documents().values().stream().mapToInt(List::size).sum() == 0) {
            issues.add(error("migration", null, null, "No legacy documents were found in the export directory"));
        }
        Map<LegacyCollection, Map<String, UUID>> ids = initializeIdMaps();
        Map<String, Map<String, String>> reportMappings = new LinkedHashMap<>();

        List<User> users = transformUsers(legacyExport, ids, issues);
        List<Classroom> classrooms = transformClassrooms(legacyExport, ids, issues);

        Map<String, UUID> usersByEmail = uniqueUsersByEmail(users, issues);
        List<Profile> profiles = transformProfiles(legacyExport, ids, usersByEmail, issues);
        List<Routine> routines = transformRoutines(legacyExport, ids, issues);
        List<Booking> bookings = transformBookings(legacyExport, ids, issues);
        List<PendingRole> pendingRoles = transformPendingRoles(legacyExport, ids, usersByEmail, issues);

        validateUniqueClassrooms(classrooms, issues);
        validateUniqueProfiles(profiles, issues);
        validateBookings(bookings, routines, issues);

        ids.forEach((collection, mapping) -> {
            Map<String, String> rendered = new LinkedHashMap<>();
            mapping.forEach((legacyId, uuid) -> rendered.put(legacyId, uuid.toString()));
            reportMappings.put(collection.reportName(), rendered);
        });

        return new MigrationPlan(
                List.copyOf(users), List.copyOf(profiles), List.copyOf(classrooms),
                List.copyOf(routines), List.copyOf(bookings), List.copyOf(pendingRoles),
                reportMappings, List.copyOf(issues)
        );
    }

    public static UUID deterministicUuid(LegacyCollection collection, String legacyId) {
        String key = "unispace:mongo:" + collection.reportName() + ":" + legacyId;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    private List<User> transformUsers(LegacyExport export, Map<LegacyCollection, Map<String, UUID>> ids,
                                      List<MigrationIssue> issues) {
        List<User> users = new ArrayList<>();
        Set<String> emails = new HashSet<>();
        for (JsonNode node : export.documents(LegacyCollection.USERS)) {
            transformOne(LegacyCollection.USERS, node, ids, issues, () -> {
                String legacyId = requiredId(node);
                UUID id = registerId(LegacyCollection.USERS, legacyId, ids);
                String email = email(node, "email");
                if (!emails.add(email)) {
                    throw invalid("email", "Duplicate user email: " + email);
                }
                List<String> roles = roles(node, issues, legacyId);
                users.add(new User(id, requiredText(node, "name"), requiredText(node, "password"), email,
                        roles, optionalText(node, "program"), optionalInteger(node, "semester"),
                        optionalInteger(node, "section")));
            });
        }
        return users;
    }

    private List<Classroom> transformClassrooms(LegacyExport export,
                                                 Map<LegacyCollection, Map<String, UUID>> ids,
                                                 List<MigrationIssue> issues) {
        List<Classroom> classrooms = new ArrayList<>();
        for (JsonNode node : export.documents(LegacyCollection.CLASSROOMS)) {
            transformOne(LegacyCollection.CLASSROOMS, node, ids, issues, () -> {
                String legacyId = requiredId(node);
                UUID id = registerId(LegacyCollection.CLASSROOMS, legacyId, ids);
                Integer capacity = requiredInteger(node, "capacity");
                if (capacity <= 0) {
                    throw invalid("capacity", "Capacity must be positive");
                }
                Boolean available = optionalBoolean(node, "isAvailable", "is_available");
                classrooms.add(new Classroom(id, requiredText(node, "room_number", "roomNumber"),
                        requiredText(node, "building"), capacity, stringList(node, "equipment"),
                        available == null || available));
            });
        }
        return classrooms;
    }

    private List<Profile> transformProfiles(LegacyExport export,
                                             Map<LegacyCollection, Map<String, UUID>> ids,
                                             Map<String, UUID> usersByEmail,
                                             List<MigrationIssue> issues) {
        List<Profile> profiles = new ArrayList<>();
        for (JsonNode node : export.documents(LegacyCollection.PROFILES)) {
            transformOne(LegacyCollection.PROFILES, node, ids, issues, () -> {
                String legacyId = requiredId(node);
                UUID id = registerId(LegacyCollection.PROFILES, legacyId, ids);
                String email = email(node, "email");
                String userReference = optionalText(node, "userId", "user_id");
                UUID userId = userReference == null ? null : ids.get(LegacyCollection.USERS).get(userReference);
                if (userId == null) {
                    userId = usersByEmail.get(email);
                    if (userId != null) {
                        issues.add(warning(LegacyCollection.PROFILES, legacyId, "user_id",
                                "Missing or stale profile user reference repaired by matching email"));
                    }
                }
                if (userId == null) {
                    throw invalid("user_id", "Profile cannot be linked to a migrated user");
                }
                String roleValue = optionalText(node, "role");
                Role role = roleValue == null ? Role.STUDENT : parseRole(roleValue, "role");
                profiles.add(new Profile(id, userId, requiredText(node, "full_name", "fullName", "name"),
                        email, optionalText(node, "program"), optionalText(node, "semester"), role.name()));
            });
        }
        return profiles;
    }

    private List<Routine> transformRoutines(LegacyExport export,
                                             Map<LegacyCollection, Map<String, UUID>> ids,
                                             List<MigrationIssue> issues) {
        List<Routine> routines = new ArrayList<>();
        for (JsonNode node : export.documents(LegacyCollection.ROUTINES)) {
            transformOne(LegacyCollection.ROUTINES, node, ids, issues, () -> {
                String legacyId = requiredId(node);
                UUID id = registerId(LegacyCollection.ROUTINES, legacyId, ids);
                UUID classroomId = reference(node, LegacyCollection.CLASSROOMS, ids,
                        "classroomId", "classroom_id");
                LocalTime start = requiredTime(node, "startTime", "start_time");
                LocalTime end = requiredTime(node, "endTime", "end_time");
                validateInterval(start, end);
                routines.add(new Routine(id, classroomId, requiredText(node, "facultyName", "faculty_name"),
                        requiredText(node, "courseCode", "course_code"), normalizeDay(requiredText(node, "day")),
                        start, end, optionalText(node, "program"), optionalInteger(node, "semester"),
                        optionalInteger(node, "section")));
            });
        }
        return routines;
    }

    private List<Booking> transformBookings(LegacyExport export,
                                             Map<LegacyCollection, Map<String, UUID>> ids,
                                             List<MigrationIssue> issues) {
        List<Booking> bookings = new ArrayList<>();
        for (JsonNode node : export.documents(LegacyCollection.BOOKINGS)) {
            transformOne(LegacyCollection.BOOKINGS, node, ids, issues, () -> {
                String legacyId = requiredId(node);
                UUID id = registerId(LegacyCollection.BOOKINGS, legacyId, ids);
                UUID classroomId = reference(node, LegacyCollection.CLASSROOMS, ids,
                        "classroomId", "classroom_id");
                UUID userId = reference(node, LegacyCollection.USERS, ids, "userId", "user_id");
                LocalDate date = requiredDate(node, "bookingDate", "booking_date");
                LocalTime start = requiredTime(node, "startTime", "start_time");
                LocalTime end = requiredTime(node, "endTime", "end_time");
                validateInterval(start, end);
                if (ChronoUnit.MINUTES.between(start, end) > 75) {
                    throw invalid("endTime", "Booking duration exceeds 75 minutes");
                }
                BookingStatus status = BookingStatus.fromValue(requiredText(node, "status"));
                if (status == BookingStatus.AVAILABLE) {
                    throw invalid("status", "AVAILABLE is a computed API state and cannot be migrated");
                }
                String facultyEmail = email(node, "facultyEmail", "faculty_email");
                if (!facultyEmail.endsWith("@iut-dhaka.edu")) {
                    throw invalid("facultyEmail", "Faculty email must use the @iut-dhaka.edu domain");
                }
                String facultyName = optionalText(node, "facultyName", "faculty_name");
                if (facultyName == null) {
                    facultyName = facultyEmail.substring(0, facultyEmail.indexOf('@'));
                    issues.add(warning(LegacyCollection.BOOKINGS, legacyId, "facultyName",
                            "Missing faculty name derived from faculty email"));
                }
                LocalDate createdAt = optionalDate(node, "createdAt", "created_at");
                bookings.add(new Booking(id, classroomId, userId, facultyEmail, facultyName,
                        requiredText(node, "reason"), optionalText(node, "courseCode", "course_code"),
                        date.getDayOfWeek().name(), date, start, end, status,
                        createdAt == null ? date : createdAt, optionalDate(node, "approvedAt", "approved_at")));
            });
        }
        return bookings;
    }

    private List<PendingRole> transformPendingRoles(LegacyExport export,
                                                     Map<LegacyCollection, Map<String, UUID>> ids,
                                                     Map<String, UUID> usersByEmail,
                                                     List<MigrationIssue> issues) {
        List<PendingRole> pendingRoles = new ArrayList<>();
        Set<String> emails = new HashSet<>();
        for (JsonNode node : export.documents(LegacyCollection.PENDING_ROLES)) {
            transformOne(LegacyCollection.PENDING_ROLES, node, ids, issues, () -> {
                String legacyId = requiredId(node);
                UUID id = registerId(LegacyCollection.PENDING_ROLES, legacyId, ids);
                String email = email(node, "email");
                if (!usersByEmail.containsKey(email)) {
                    throw invalid("email", "Pending role request has no migrated user");
                }
                if (!emails.add(email)) {
                    throw invalid("email", "Duplicate pending role request for " + email);
                }
                Role role = parseRole(requiredText(node, "role"), "role");
                if (role != Role.STUDENT && role != Role.CR) {
                    throw invalid("role", "Pending role must be STUDENT or CR");
                }
                pendingRoles.add(new PendingRole(id, requiredText(node, "name"), email, role.name()));
            });
        }
        return pendingRoles;
    }

    private void validateUniqueClassrooms(List<Classroom> classrooms, List<MigrationIssue> issues) {
        Set<String> roomNumbers = new HashSet<>();
        classrooms.forEach(classroom -> {
            if (!roomNumbers.add(classroom.getRoom_number().toLowerCase(Locale.ROOT))) {
                issues.add(error("classrooms", classroom.getId().toString(), "room_number",
                        "Duplicate classroom room number: " + classroom.getRoom_number()));
            }
        });
    }

    private void validateUniqueProfiles(List<Profile> profiles, List<MigrationIssue> issues) {
        Set<String> emails = new HashSet<>();
        Set<UUID> userIds = new HashSet<>();
        profiles.forEach(profile -> {
            if (!emails.add(profile.getEmail())) {
                issues.add(error("profiles", profile.getId().toString(), "email",
                        "Duplicate profile email: " + profile.getEmail()));
            }
            if (!userIds.add(profile.getUserId())) {
                issues.add(error("profiles", profile.getId().toString(), "user_id",
                        "More than one profile references the same user"));
            }
        });
    }

    private void validateBookings(List<Booking> bookings, List<Routine> routines, List<MigrationIssue> issues) {
        for (int index = 0; index < bookings.size(); index++) {
            Booking booking = bookings.get(index);
            if (booking.getStatus() != BookingStatus.BOOKED) {
                continue;
            }
            for (int otherIndex = 0; otherIndex < index; otherIndex++) {
                Booking other = bookings.get(otherIndex);
                if (other.getStatus() == BookingStatus.BOOKED
                        && booking.getClassroomId().equals(other.getClassroomId())
                        && booking.getBookingDate().equals(other.getBookingDate())
                        && overlaps(booking.getStartTime(), booking.getEndTime(), other.getStartTime(), other.getEndTime())) {
                    issues.add(error("bookings", booking.getId().toString(), "startTime",
                            "Approved booking overlaps another approved booking: " + other.getId()));
                }
            }
            for (Routine routine : routines) {
                if (booking.getClassroomId().equals(routine.getClassroomId())
                        && booking.getDay().equalsIgnoreCase(routine.getDay())
                        && overlaps(booking.getStartTime(), booking.getEndTime(), routine.getStartTime(), routine.getEndTime())) {
                    issues.add(error("bookings", booking.getId().toString(), "startTime",
                            "Approved booking overlaps recurring routine: " + routine.getId()));
                }
            }
        }
    }

    private boolean overlaps(LocalTime start, LocalTime end, LocalTime otherStart, LocalTime otherEnd) {
        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }

    private Map<String, UUID> uniqueUsersByEmail(List<User> users, List<MigrationIssue> issues) {
        Map<String, UUID> result = new HashMap<>();
        users.forEach(user -> {
            UUID previous = result.putIfAbsent(user.getEmail(), user.getId());
            if (previous != null) {
                issues.add(error("users", user.getId().toString(), "email", "User email is not unique"));
            }
        });
        return result;
    }

    private Map<LegacyCollection, Map<String, UUID>> initializeIdMaps() {
        Map<LegacyCollection, Map<String, UUID>> result = new EnumMap<>(LegacyCollection.class);
        for (LegacyCollection collection : LegacyCollection.values()) {
            result.put(collection, new LinkedHashMap<>());
        }
        return result;
    }

    private UUID registerId(LegacyCollection collection, String legacyId,
                            Map<LegacyCollection, Map<String, UUID>> ids) {
        UUID id = deterministicUuid(collection, legacyId);
        if (ids.get(collection).putIfAbsent(legacyId, id) != null) {
            throw invalid("_id", "Duplicate legacy identifier: " + legacyId);
        }
        return id;
    }

    private UUID reference(JsonNode node, LegacyCollection target,
                           Map<LegacyCollection, Map<String, UUID>> ids, String... fields) {
        String legacyReference = requiredText(node, fields);
        UUID id = ids.get(target).get(legacyReference);
        if (id == null) {
            throw invalid(fields[0], "Reference does not resolve to a migrated " + target.reportName()
                    + " document: " + legacyReference);
        }
        return id;
    }

    private void transformOne(LegacyCollection collection, JsonNode node,
                              Map<LegacyCollection, Map<String, UUID>> ids,
                              List<MigrationIssue> issues, Runnable transformation) {
        String legacyId = optionalText(node, "_id", "id");
        try {
            transformation.run();
        } catch (MigrationValidationException exception) {
            removeFailedMapping(collection, legacyId, ids);
            issues.add(error(collection.reportName(), legacyId, exception.field(), exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            removeFailedMapping(collection, legacyId, ids);
            issues.add(error(collection.reportName(), legacyId, null, exception.getMessage()));
        }
    }

    private void removeFailedMapping(LegacyCollection collection, String legacyId,
                                     Map<LegacyCollection, Map<String, UUID>> ids) {
        if (legacyId != null) {
            ids.get(collection).remove(legacyId);
        }
    }

    private List<String> roles(JsonNode node, List<MigrationIssue> issues, String legacyId) {
        List<String> values = stringList(node, "roles");
        if (values.isEmpty()) {
            issues.add(warning(LegacyCollection.USERS, legacyId, "roles",
                    "Missing roles defaulted to STUDENT"));
            return new ArrayList<>(List.of(Role.STUDENT.name()));
        }
        return new ArrayList<>(values.stream().map(value -> parseRole(value, "roles").name()).distinct().toList());
    }

    private Role parseRole(String value, String field) {
        try {
            return Role.fromValue(value);
        } catch (IllegalArgumentException exception) {
            throw invalid(field, exception.getMessage());
        }
    }

    private void validateInterval(LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw invalid("endTime", "End time must be after start time");
        }
    }

    private String requiredId(JsonNode node) {
        return requiredText(node, "_id", "id");
    }

    private String email(JsonNode node, String... fields) {
        String value = requiredText(node, fields).trim().toLowerCase(Locale.ROOT);
        if (!value.contains("@") || value.startsWith("@") || value.endsWith("@")) {
            throw invalid(fields[0], "Invalid email address: " + value);
        }
        return value;
    }

    private String requiredText(JsonNode node, String... fields) {
        String value = optionalText(node, fields);
        if (value == null || value.isBlank()) {
            throw invalid(fields[0], "Required value is missing");
        }
        return value.trim();
    }

    private String optionalText(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value == null || value.isNull()) {
                continue;
            }
            if (value.isObject()) {
                for (String wrapper : List.of("$oid", "$date", "$numberInt", "$numberLong")) {
                    JsonNode wrapped = value.get(wrapper);
                    if (wrapped != null && !wrapped.isNull()) {
                        return wrapped.asText();
                    }
                }
            }
            if (value.isValueNode()) {
                return value.asText();
            }
        }
        return null;
    }

    private List<String> stringList(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return new ArrayList<>();
        }
        if (!value.isArray()) {
            throw invalid(field, "Expected an array");
        }
        List<String> result = new ArrayList<>();
        value.forEach(item -> {
            if (!item.isTextual()) {
                throw invalid(field, "Expected an array of strings");
            }
            if (!item.asText().isBlank()) {
                result.add(item.asText().trim());
            }
        });
        return result;
    }

    private Integer requiredInteger(JsonNode node, String... fields) {
        Integer value = optionalInteger(node, fields);
        if (value == null) {
            throw invalid(fields[0], "Required integer is missing");
        }
        return value;
    }

    private Integer optionalInteger(JsonNode node, String... fields) {
        String value = optionalText(node, fields);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            throw invalid(fields[0], "Invalid integer: " + value);
        }
    }

    private Boolean optionalBoolean(JsonNode node, String... fields) {
        String value = optionalText(node, fields);
        if (value == null) {
            return null;
        }
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw invalid(fields[0], "Invalid boolean: " + value);
        }
        return Boolean.valueOf(value);
    }

    private LocalDate requiredDate(JsonNode node, String... fields) {
        LocalDate value = optionalDate(node, fields);
        if (value == null) {
            throw invalid(fields[0], "Required date is missing");
        }
        return value;
    }

    private LocalDate optionalDate(JsonNode node, String... fields) {
        String value = optionalText(node, fields);
        if (value == null || value.isBlank()) {
            return null;
        }
        String datePart = value.length() >= 10 ? value.substring(0, 10) : value;
        try {
            return LocalDate.parse(datePart);
        } catch (DateTimeParseException exception) {
            throw invalid(fields[0], "Invalid ISO date: " + value);
        }
    }

    private LocalTime requiredTime(JsonNode node, String... fields) {
        String value = requiredText(node, fields);
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException exception) {
            try {
                return LocalTime.parse(value + ":00");
            } catch (DateTimeParseException ignored) {
                throw invalid(fields[0], "Invalid ISO time: " + value);
            }
        }
    }

    private String normalizeDay(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            java.time.DayOfWeek.valueOf(normalized);
            return normalized;
        } catch (IllegalArgumentException exception) {
            throw invalid("day", "Invalid day of week: " + value);
        }
    }

    private MigrationIssue warning(LegacyCollection collection, String legacyId, String field, String message) {
        return new MigrationIssue(MigrationIssue.Severity.WARNING, collection.reportName(), legacyId, field, message);
    }

    private MigrationIssue error(String collection, String legacyId, String field, String message) {
        return new MigrationIssue(MigrationIssue.Severity.ERROR, collection, legacyId, field, message);
    }

    private MigrationValidationException invalid(String field, String message) {
        return new MigrationValidationException(field, message);
    }
}
