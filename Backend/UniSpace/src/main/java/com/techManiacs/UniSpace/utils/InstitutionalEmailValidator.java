package com.techManiacs.UniSpace.utils;

import java.util.Locale;
import java.util.regex.Pattern;

public final class InstitutionalEmailValidator {
    private static final String DOMAIN = "iut-dhaka.edu";
    private static final Pattern LOCAL_PART = Pattern.compile(
            "[a-z0-9]+(?:[._%+-][a-z0-9]+)*",
            Pattern.CASE_INSENSITIVE);

    private InstitutionalEmailValidator() {
    }

    public static boolean isValid(String email) {
        if (email == null) {
            return false;
        }

        String normalized = normalize(email);
        if (normalized.isEmpty() || normalized.chars().anyMatch(Character::isWhitespace)) {
            return false;
        }

        int separator = normalized.indexOf('@');
        return separator > 0
                && separator == normalized.lastIndexOf('@')
                && normalized.substring(separator + 1).equals(DOMAIN)
                && LOCAL_PART.matcher(normalized.substring(0, separator)).matches();
    }

    public static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
