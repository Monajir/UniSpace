package com.techManiacs.UniSpace.migration;

final class MigrationValidationException extends RuntimeException {
    private final String field;

    MigrationValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    String field() {
        return field;
    }
}
