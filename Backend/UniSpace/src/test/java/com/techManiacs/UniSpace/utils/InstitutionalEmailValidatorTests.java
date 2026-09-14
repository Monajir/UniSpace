package com.techManiacs.UniSpace.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class InstitutionalEmailValidatorTests {

    @ParameterizedTest
    @ValueSource(strings = {
            "student@iut-dhaka.edu",
            " Student.Name+demo@IUT-DHAKA.EDU "
    })
    void acceptsWellFormedInstitutionalAddresses(String email) {
        assertThat(InstitutionalEmailValidator.isValid(email)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "@iut-dhaka.edu",
            "student@@iut-dhaka.edu",
            "student @iut-dhaka.edu",
            "student@iut-dhaka.edu.evil.example",
            "student@example.com"
    })
    void rejectsMalformedOrNonInstitutionalAddresses(String email) {
        assertThat(InstitutionalEmailValidator.isValid(email)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {" Student@IUT-DHAKA.EDU ", "student@iut-dhaka.edu"})
    void normalizationProducesCanonicalAddress(String email) {
        assertThat(InstitutionalEmailValidator.normalize(email))
                .isEqualTo("student@iut-dhaka.edu");
    }
}
