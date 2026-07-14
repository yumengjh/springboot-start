package com.yumg.starter.modules.security.application;
import com.yumg.starter.common.web.TraceIdFilter;
import com.yumg.starter.entities.AuditEvent;
import com.yumg.starter.modules.security.infrastructure.AuditEventRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import com.yumg.starter.common.api.PageResponse;
import com.yumg.starter.modules.security.api.dto.AuditEventResponse;
import com.yumg.starter.modules.runtimeconfig.application.RuntimeSettingService;
import org.springframework.beans.factory.ObjectProvider;
@Service public class AuditService {
 private final AuditEventRepository events; private final ObjectProvider<RuntimeSettingService> settings;
 public AuditService(AuditEventRepository events, ObjectProvider<RuntimeSettingService> settings){this.events=events;this.settings=settings;}
 @Transactional public void runtimeSettingChanged(String key,String value){save("RUNTIME_SETTING_UPDATED","RuntimeSetting",key,"SUCCESS","{\"value\":\""+value.replace("\"","\\\"")+"\"}");}
 @Transactional public void event(String action,String targetType,String targetId){event(action,targetType,targetId,"SUCCESS",null);}
 @Transactional public void event(String action,String targetType,String targetId,String result,String metadata){ if(enabled()) save(action,targetType,targetId,result,metadata); }
 private void save(String action,String targetType,String targetId,String result,String metadata){events.save(new AuditEvent(actorId(),action,targetType,targetId,result,MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY),metadata));}
 private boolean enabled(){RuntimeSettingService service=settings.getIfAvailable();return service==null||service.enabled("security.audit.enabled");}
 private String actorId(){Object principal=SecurityContextHolder.getContext().getAuthentication()==null?null:SecurityContextHolder.getContext().getAuthentication().getPrincipal();return principal instanceof Jwt jwt?jwt.getSubject():null;}
 @Transactional(readOnly = true) public PageResponse<AuditEventResponse> list(int page, int size) { return PageResponse.from(events.findAllByOrderByOccurredAtDesc(PageRequest.of(page, size)).map(AuditEventResponse::from)); }
}
