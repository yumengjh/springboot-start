package com.yumg.starter.modules.security.application;
import com.yumg.starter.common.web.TraceIdFilter;
import com.yumg.starter.entities.AuditEvent;
import com.yumg.starter.modules.security.infrastructure.AuditEventRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import com.yumg.starter.common.api.PageResponse;
import com.yumg.starter.modules.security.api.dto.AuditEventResponse;
@Service public class AuditService {
 private final AuditEventRepository events; public AuditService(AuditEventRepository events){this.events=events;}
 @Transactional public void runtimeSettingChanged(String key,String value){events.save(new AuditEvent("RUNTIME_SETTING_UPDATED","RuntimeSetting",key,MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY),"{\"value\":\""+value.replace("\"","\\\"")+"\"}"));}
 @Transactional(readOnly = true) public PageResponse<AuditEventResponse> list(int page, int size) { return PageResponse.from(events.findAllByOrderByOccurredAtDesc(PageRequest.of(page, size)).map(AuditEventResponse::from)); }
}
