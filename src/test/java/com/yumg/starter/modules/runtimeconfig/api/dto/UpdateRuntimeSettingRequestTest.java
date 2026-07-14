package com.yumg.starter.modules.runtimeconfig.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

class UpdateRuntimeSettingRequestTest {

    @Test
    void acceptsAnEmptyStringToClearAnOptionalStringSetting() {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();

        assertThat(validator.validate(new UpdateRuntimeSettingRequest(""))).isEmpty();
        assertThat(validator.validate(new UpdateRuntimeSettingRequest(null))).isNotEmpty();
    }
}
