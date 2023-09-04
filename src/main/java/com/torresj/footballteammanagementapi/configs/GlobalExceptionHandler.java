package com.torresj.footballteammanagementapi.configs;

import com.torresj.footballteammanagementapi.exceptions.MemberAlreadyExistsException;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(MemberNotFoundException.class)
  ProblemDetail memberNotFoundException(MemberNotFoundException e) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    problemDetail.setTitle("Member Not Found");
    log.error(e.toString());
    return problemDetail;
  }

  @ExceptionHandler(MemberAlreadyExistsException.class)
  ProblemDetail memberAlreadyExistsException(MemberAlreadyExistsException e) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    problemDetail.setTitle("Member already exists");
    log.error(e.toString());
    return problemDetail;
  }
}
