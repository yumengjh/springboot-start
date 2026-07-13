package com.yumg.starter.modules.security.web;
import com.yumg.starter.common.api.ApiErrorCode;
import com.yumg.starter.common.api.ApiErrorWriter;
import com.yumg.starter.modules.runtimeconfig.application.RuntimeSettingService;
import jakarta.servlet.*; import jakarta.servlet.http.*; import java.io.IOException; import java.util.*;
import org.springframework.http.HttpMethod; import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
@Component public class IpAccessFilter extends OncePerRequestFilter {
 private final RuntimeSettingService settings; private final ApiErrorWriter errors;
 public IpAccessFilter(RuntimeSettingService settings, ApiErrorWriter errors){this.settings=settings;this.errors=errors;}
 @Override protected boolean shouldNotFilter(HttpServletRequest r){return !r.getRequestURI().startsWith("/api/")||HttpMethod.OPTIONS.matches(r.getMethod());}
 @Override protected void doFilterInternal(HttpServletRequest r,HttpServletResponse s,FilterChain c)throws ServletException,IOException{
  if(r.getRequestURI().startsWith("/api/v1/system/runtime-config")){c.doFilter(r,s);return;}
  String ip=r.getRemoteAddr(); Set<String> allow=list("security.ip.allow-list"), deny=list("security.ip.deny-list");
  if((!allow.isEmpty()&&!allow.contains(ip))||deny.contains(ip)){errors.write(s,ApiErrorCode.IP_ACCESS_DENIED);return;} c.doFilter(r,s);
 }
 private Set<String> list(String k){return Arrays.stream(settings.string(k).split(",")).map(String::trim).filter(v->!v.isEmpty()).collect(java.util.stream.Collectors.toSet());}
}
