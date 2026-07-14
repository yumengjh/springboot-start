package com.yumg.starter.modules.security.application;

import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.entities.IpAccessRule;
import com.yumg.starter.modules.security.api.dto.IpAccessRuleRequest;
import com.yumg.starter.modules.security.api.dto.IpAccessRuleResponse;
import com.yumg.starter.modules.security.infrastructure.IpAccessRuleRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IpAccessRuleService {
 private static final String API_SCOPE="API";
 private final IpAccessRuleRepository rules; private final AuditService audit;
 public IpAccessRuleService(IpAccessRuleRepository rules, AuditService audit){this.rules=rules;this.audit=audit;}
 @Transactional(readOnly=true) public List<IpAccessRuleResponse> list(){return rules.findAllByScope(API_SCOPE).stream().map(IpAccessRuleResponse::from).toList();}
 @Transactional public IpAccessRuleResponse create(IpAccessRuleRequest request){
  if(!IpNetworkMatcher.matches(request.network(), networkAddress(request.network()))) throw ApiException.notFound();
  Instant expiresAt=request.expiresAt()==null?null:Instant.ofEpochMilli(request.expiresAt());
  IpAccessRule rule=rules.save(new IpAccessRule(request.type(),request.network(),API_SCOPE,expiresAt,request.reason().trim()));
  audit.event("IP_ACCESS_RULE_CREATED","IpAccessRule",rule.getId(),"SUCCESS","{\"type\":\""+request.type()+"\"}"); return IpAccessRuleResponse.from(rule);
 }
 @Transactional public void delete(String id){rules.delete(rules.findById(id).orElseThrow(ApiException::notFound));audit.event("IP_ACCESS_RULE_DELETED","IpAccessRule",id);}
 @Transactional(readOnly=true) public boolean denies(String ip){return rules.findAllByScope(API_SCOPE).stream().anyMatch(rule->rule.getType()==IpAccessRule.Type.DENY&&rule.activeAt(Instant.now())&&IpNetworkMatcher.matches(rule.getNetwork(),ip));}
 @Transactional(readOnly=true) public boolean allows(String ip){return rules.findAllByScope(API_SCOPE).stream().anyMatch(rule->rule.getType()==IpAccessRule.Type.ALLOW&&rule.activeAt(Instant.now())&&IpNetworkMatcher.matches(rule.getNetwork(),ip));}
 @Transactional(readOnly=true) public boolean hasActiveAllowRules(){return rules.findAllByScope(API_SCOPE).stream().anyMatch(rule->rule.getType()==IpAccessRule.Type.ALLOW&&rule.activeAt(Instant.now()));}
 private String networkAddress(String network){return network.split("/",2)[0];}
}
