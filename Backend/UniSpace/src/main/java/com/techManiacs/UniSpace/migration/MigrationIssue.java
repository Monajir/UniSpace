package com.techManiacs.UniSpace.migration;

public record MigrationIssue(
        Severity severity,
        String collection,
        String legacyId,
        String field,
        String message
) {
    public enum Severity {
        WARNING,
        ERROR
    }
}
