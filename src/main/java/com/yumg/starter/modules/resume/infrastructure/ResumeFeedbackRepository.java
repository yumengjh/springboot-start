package com.yumg.starter.modules.resume.infrastructure;

import com.yumg.starter.entities.ResumeFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ResumeFeedbackRepository extends JpaRepository<ResumeFeedback, String>, JpaSpecificationExecutor<ResumeFeedback> {
}
