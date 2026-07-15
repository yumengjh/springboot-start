package com.yumg.starter.modules.resume.application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class ResumeBootstrap implements ApplicationRunner {

    private final ResumeService resume;

    public ResumeBootstrap(ResumeService resume) {
        this.resume = resume;
    }

    @Override
    public void run(ApplicationArguments args) {
        resume.createDefaultIfAbsent();
    }
}
