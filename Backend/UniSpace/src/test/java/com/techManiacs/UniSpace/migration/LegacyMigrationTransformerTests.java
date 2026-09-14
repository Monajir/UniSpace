package com.techManiacs.UniSpace.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techManiacs.UniSpace.domain.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LegacyMigrationTransformerTests {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LegacyMigrationTransformer transformer = new LegacyMigrationTransformer();

    @TempDir
    Path temporaryDirectory;

    @Test
    void mapsExtendedMongoIdsAndPreservesRelationshipsDeterministically() throws Exception {
        LegacyExport export = validExport(true);

        MigrationPlan first = transformer.transform(export);
        MigrationPlan second = transformer.transform(export);

        assertThat(first.hasErrors()).isFalse();
        assertThat(first.users()).hasSize(1);
        assertThat(first.classrooms()).hasSize(1);
        assertThat(first.bookings()).hasSize(1);
        assertThat(first.bookings().getFirst().getUserId()).isEqualTo(first.users().getFirst().getId());
        assertThat(first.bookings().getFirst().getClassroomId()).isEqualTo(first.classrooms().getFirst().getId());
        assertThat(first.bookings().getFirst().getStatus()).isEqualTo(BookingStatus.BOOKED);
        assertThat(first.bookings().getFirst().getDay()).isEqualTo("MONDAY");
        assertThat(second.users().getFirst().getId()).isEqualTo(first.users().getFirst().getId());
    }

    @Test
    void repairsMissingLegacyProfileUserReferenceByEmail() throws Exception {
        MigrationPlan plan = transformer.transform(validExport(false));

        assertThat(plan.hasErrors()).isFalse();
        assertThat(plan.profiles().getFirst().getUserId()).isEqualTo(plan.users().getFirst().getId());
        assertThat(plan.issues()).anySatisfy(issue -> {
            assertThat(issue.severity()).isEqualTo(MigrationIssue.Severity.WARNING);
            assertThat(issue.field()).isEqualTo("user_id");
        });
    }

    @Test
    void reportsOrphanReferencesWithoutProducingAnImportablePlan() throws Exception {
        LegacyExport export = validExport(true);
        JsonNode orphan = objectMapper.readTree("""
                {"_id":{"$oid":"booking-orphan"},"classroomId":{"$oid":"missing-room"},
                 "userId":{"$oid":"user-1"},"facultyEmail":"faculty@iut-dhaka.edu",
                 "facultyName":"Faculty","reason":"Test","bookingDate":"2030-01-07",
                 "startTime":"12:00","endTime":"13:00","status":"pending"}
                """);
        export.documents().get(LegacyCollection.BOOKINGS).add(orphan);

        MigrationPlan plan = transformer.transform(export);

        assertThat(plan.hasErrors()).isTrue();
        assertThat(plan.issues()).anySatisfy(issue -> {
            assertThat(issue.severity()).isEqualTo(MigrationIssue.Severity.ERROR);
            assertThat(issue.message()).contains("does not resolve");
        });
    }

    @Test
    void rejectsOverlappingApprovedBookingsBeforeDatabaseImport() throws Exception {
        LegacyExport export = validExport(true);
        JsonNode overlap = objectMapper.readTree("""
                {"_id":{"$oid":"booking-2"},"classroomId":{"$oid":"room-1"},
                 "userId":{"$oid":"user-1"},"facultyEmail":"faculty@iut-dhaka.edu",
                 "facultyName":"Faculty","reason":"Overlap","bookingDate":"2030-01-07",
                 "startTime":"10:30","endTime":"11:15","status":"booked"}
                """);
        export.documents().get(LegacyCollection.BOOKINGS).add(overlap);

        MigrationPlan plan = transformer.transform(export);

        assertThat(plan.hasErrors()).isTrue();
        assertThat(plan.issues()).anyMatch(issue -> issue.message().contains("overlaps another approved booking"));
    }

    @Test
    void readerAcceptsMongoJsonArrayAndNewlineDelimitedExports() throws Exception {
        Files.writeString(temporaryDirectory.resolve("user.json"), """
                [{"_id":{"$oid":"user-1"},"name":"Student","password":"hash",
                  "email":"student@example.com","roles":["STUDENT"]}]
                """);
        Files.writeString(temporaryDirectory.resolve("classroom.json"), """
                {"_id":{"$oid":"room-1"},"room_number":"101","building":"A","capacity":30}
                {"_id":{"$oid":"room-2"},"room_number":"102","building":"A","capacity":30}
                """);

        LegacyExport export = new LegacyMongoExportReader(objectMapper).read(temporaryDirectory);

        assertThat(export.documents(LegacyCollection.USERS)).hasSize(1);
        assertThat(export.documents(LegacyCollection.CLASSROOMS)).hasSize(2);
        assertThat(export.documents(LegacyCollection.BOOKINGS)).isEmpty();
    }

    @Test
    void emptyExportIsNotImportable() {
        Map<LegacyCollection, List<JsonNode>> documents = new EnumMap<>(LegacyCollection.class);

        MigrationPlan plan = transformer.transform(new LegacyExport(documents));

        assertThat(plan.hasErrors()).isTrue();
        assertThat(plan.issues()).anyMatch(issue -> issue.message().contains("No legacy documents"));
    }

    @Test
    void readerRejectsAmbiguousAliases() throws Exception {
        Files.writeString(temporaryDirectory.resolve("users.json"), "[]");
        Files.writeString(temporaryDirectory.resolve("user.json"), "[]");

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> new LegacyMongoExportReader(objectMapper).read(temporaryDirectory))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Multiple export files");
    }

    private LegacyExport validExport(boolean includeProfileUserId) throws Exception {
        Map<LegacyCollection, List<JsonNode>> documents = new EnumMap<>(LegacyCollection.class);
        for (LegacyCollection collection : LegacyCollection.values()) {
            documents.put(collection, new java.util.ArrayList<>());
        }
        documents.get(LegacyCollection.USERS).add(objectMapper.readTree("""
                {"_id":{"$oid":"user-1"},"name":"Student","password":"$2a$10$existingHash",
                 "email":"Student@Example.com","roles":["student"],"program":"CSE",
                 "semester":{"$numberInt":"5"},"section":1}
                """));
        documents.get(LegacyCollection.PROFILES).add(objectMapper.readTree("""
                {"_id":{"$oid":"profile-1"},%s"full_name":"Student",
                 "email":"student@example.com","program":"CSE","semester":"5","role":"student"}
                """.formatted(includeProfileUserId ? "\"user_id\":{\"$oid\":\"user-1\"}," : "")));
        documents.get(LegacyCollection.CLASSROOMS).add(objectMapper.readTree("""
                {"_id":{"$oid":"room-1"},"room_number":"101","building":"Academic",
                 "capacity":"30","equipment":["Projector"],"isAvailable":true}
                """));
        documents.get(LegacyCollection.BOOKINGS).add(objectMapper.readTree("""
                {"_id":{"$oid":"booking-1"},"classroomId":{"$oid":"room-1"},
                 "userId":{"$oid":"user-1"},"facultyEmail":"Faculty@iut-dhaka.edu",
                 "facultyName":"Faculty","reason":"Review","courseCode":"CSE-101",
                 "day":"FRIDAY","bookingDate":{"$date":"2030-01-07T00:00:00Z"},
                 "startTime":"10:00","endTime":"11:00","status":"booked",
                 "createdAt":"2030-01-01","approvedAt":"2030-01-02"}
                """));
        documents.get(LegacyCollection.PENDING_ROLES).add(objectMapper.readTree("""
                {"_id":{"$oid":"role-1"},"name":"Student","email":"student@example.com","role":"CR"}
                """));
        return new LegacyExport(documents);
    }
}
