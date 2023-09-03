package com.torresj.footballteammanagementapi.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class LoggerInterceptor implements HandlerInterceptor {

  // UUID key
  private final String UUID_KEY = "UUID";

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView)
      throws Exception {
    MDC.remove(UUID_KEY);
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String uuid =
        request.getHeader(UUID_KEY) == null
            ? UUID.randomUUID().toString()
            : request.getHeader(UUID_KEY);
    MDC.put(UUID_KEY, uuid);
    return true;
  }
}
