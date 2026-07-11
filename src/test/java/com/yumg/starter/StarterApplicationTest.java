package com.yumg.starter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration"
})
class StarterApplicationTest {

    @Test
    void contextLoads() {}
}
