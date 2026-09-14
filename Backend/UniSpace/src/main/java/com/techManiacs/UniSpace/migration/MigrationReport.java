package com.techManiacs.UniSpace.migration;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class MigrationReport {
    private Instant generatedAt = Instant.now();
    private String mode;
    private String status;
    private String inputDirectory;
    private Map<String, Integer> sourceCounts;
    private Map<String, Integer> targetCounts;
    private Map<String, Map<String, String>> idMappings;
    private List<MigrationIssue> issues;
}
