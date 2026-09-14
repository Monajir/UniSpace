package com.techManiacs.UniSpace.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Profile("migration")
public class LegacyDataMigrationRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(LegacyDataMigrationRunner.class);

    private final LegacyMongoExportReader reader;
    private final LegacyMigrationTransformer transformer;
    private final LegacyDataImporter importer;
    private final ObjectMapper objectMapper;
    private final String inputDirectory;
    private final boolean dryRun;
    private final String reportFile;

    public LegacyDataMigrationRunner(LegacyMongoExportReader reader,
                                     LegacyMigrationTransformer transformer,
                                     LegacyDataImporter importer,
                                     ObjectMapper objectMapper,
                                     @Value("${app.migration.input-dir:}") String inputDirectory,
                                     @Value("${app.migration.dry-run:true}") boolean dryRun,
                                     @Value("${app.migration.report-file:}") String reportFile) {
        this.reader = reader;
        this.transformer = transformer;
        this.importer = importer;
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
        this.inputDirectory = inputDirectory;
        this.dryRun = dryRun;
        this.reportFile = reportFile;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (inputDirectory == null || inputDirectory.isBlank()) {
            throw new IllegalArgumentException("MIGRATION_INPUT_DIR or app.migration.input-dir is required");
        }

        Path input = Path.of(inputDirectory).toAbsolutePath().normalize();
        LegacyExport legacyExport = reader.read(input);
        MigrationPlan plan = transformer.transform(legacyExport);
        MigrationReport report = createReport(input, legacyExport, plan);
        Path reportPath = resolveReportPath(input);

        if (plan.hasErrors()) {
            report.setStatus("VALIDATION_FAILED");
            writeReport(reportPath, report);
            long errors = plan.issues().stream()
                    .filter(issue -> issue.severity() == MigrationIssue.Severity.ERROR)
                    .count();
            throw new IllegalStateException("Migration validation failed with " + errors
                    + " error(s). See " + reportPath);
        }

        if (dryRun) {
            report.setStatus("DRY_RUN_VALID");
        } else {
            try {
                importer.importPlan(plan);
                report.setStatus("IMPORTED");
            } catch (RuntimeException exception) {
                report.setStatus("IMPORT_FAILED");
                report.getIssues().add(new MigrationIssue(MigrationIssue.Severity.ERROR, "postgresql", null,
                        null, exception.getMessage()));
                writeReport(reportPath, report);
                throw exception;
            }
        }

        writeReport(reportPath, report);
        logger.info("Legacy migration {}: {} source documents, {} target records, report={}",
                report.getStatus(), report.getSourceCounts().values().stream().mapToInt(Integer::intValue).sum(),
                report.getTargetCounts().values().stream().mapToInt(Integer::intValue).sum(), reportPath);
    }

    private MigrationReport createReport(Path input, LegacyExport export, MigrationPlan plan) {
        MigrationReport report = new MigrationReport();
        report.setMode(dryRun ? "DRY_RUN" : "IMPORT");
        report.setInputDirectory(input.toString());
        Map<String, Integer> sourceCounts = new LinkedHashMap<>();
        for (LegacyCollection collection : LegacyCollection.values()) {
            sourceCounts.put(collection.reportName(), export.documents(collection).size());
        }
        report.setSourceCounts(sourceCounts);
        report.setTargetCounts(plan.targetCounts());
        report.setIdMappings(plan.idMappings());
        report.setIssues(new ArrayList<>(plan.issues()));
        return report;
    }

    private Path resolveReportPath(Path input) {
        if (reportFile == null || reportFile.isBlank()) {
            return input.resolve("migration-report.json");
        }
        return Path.of(reportFile).toAbsolutePath().normalize();
    }

    private void writeReport(Path reportPath, MigrationReport report) throws Exception {
        Path parent = reportPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        objectMapper.writeValue(reportPath.toFile(), report);
    }
}
