package com.sequenceiq.environment.environment.validation.validators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.sequenceiq.cloudbreak.validation.ValidationResult;
import com.sequenceiq.environment.environment.dto.AuthenticationDto;
import com.sequenceiq.environment.environment.dto.EnvironmentDto;

class EnvironmentAuthenticationValidatorTest {

    private static final String PUBLIC_KEY = "AAAAB3NzaC1yc2EAAAADAQABAAABAQCasJyap4swb4Hk4xOlnF3OmKVwzmv2e053yrtvcUPaxCeboSltOBReuT"
            + "QxX+kYCgKCdtEwpIvEDXk16T6nCI4tSptAalFgpUWn+JOysCuLuWnwrk6mSKOzEiPYCrB54444mDY6rbBDSRuE/V"
            + "UYQ/yi0imocARlOiFdPRlZGTN0XGE1V8LSo+m0oIzTwBKn58I4v5iB4ZUL/6adGXo7dgdBh/Fmm4uYbgrCZnL1EaK"
            + "pMxSG76XWhuzFpHjLkRndz88ha0rB6davag6nZGdno5IepLAWg9oB4jTApHwhN2j1rWLN2y1c+pTxsF6LxBiN5rsY"
            + "KR495VFmuOepLYz5I8Dn sequence-eu";

    private static final String VALID_PUBLIC_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCasJyap4swb4Hk4xOlnF3OmKVwzmv2e053yrtvcUPaxCeboSltOBReuT"
            + "QxX+kYCgKCdtEwpIvEDXk16T6nCI4tSptAalFgpUWn+JOysCuLuWnwrk6mSKOzEiPYCrB54444mDY6rbBDSRuE/V"
            + "UYQ/yi0imocARlOiFdPRlZGTN0XGE1V8LSo+m0oIzTwBKn58I4v5iB4ZUL/6adGXo7dgdBh/Fmm4uYbgrCZnL1EaK"
            + "pMxSG76XWhuzFpHjLkRndz88ha0rB6davag6nZGdno5IepLAWg9oB4jTApHwhN2j1rWLN2y1c+pTxsF6LxBiN5rsY"
            + "KR495VFmuOepLYz5I8Dn sequence-eu";

    private final EnvironmentAuthenticationValidator underTest = new EnvironmentAuthenticationValidator();

    @Test
    void testValidateShouldReturnValidationError() {
        EnvironmentDto environmentDto = createEnvironmentDto(PUBLIC_KEY, null);
        String expected = "1. Failed to parse public key. Detailed message: Corrupt or unknown public key file format";

        ValidationResult actual = underTest.validate(environmentDto);

        Assertions.assertEquals(expected, actual.getFormattedErrors());
    }

    @Test
    void testValidateShouldReturnWithoutError() {
        EnvironmentDto environmentDto = createEnvironmentDto(VALID_PUBLIC_KEY, null);

        ValidationResult actual = underTest.validate(environmentDto);

        Assertions.assertFalse(actual.hasError());
    }

    @Test
    void testValidateShouldReturnWithoutErrorWhenPublicKeyIdIsPresent() {
        EnvironmentDto environmentDto = createEnvironmentDto(null, "public-key-id");

        ValidationResult actual = underTest.validate(environmentDto);

        Assertions.assertFalse(actual.hasError());
    }

    private EnvironmentDto createEnvironmentDto(String publicKey, String publicKeyId) {
        return EnvironmentDto.builder()
                .withAuthentication(AuthenticationDto.builder()
                        .withPublicKey(publicKey)
                        .withPublicKeyId(publicKeyId)
                        .build())
                .build();
    }

}