package com.yumg.starter.modules.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.modules.auth.application.AuthenticationService;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

class AuthenticationTransactionContractTest {
    @Test
    void refreshDoesNotRollbackSecurityStateWhenRejectingAReusedToken() throws Exception {
        Method refresh = AuthenticationService.class.getMethod("refresh", String.class);
        Transactional transaction = refresh.getAnnotation(Transactional.class);

        assertThat(transaction).isNotNull();
        assertThat(transaction.noRollbackFor()).contains(ApiException.class);
    }
}
