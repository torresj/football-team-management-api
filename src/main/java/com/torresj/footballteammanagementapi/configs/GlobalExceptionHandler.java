package com.torresj.footballteammanagementapi.configs;

import com.torresj.footballteammanagementapi.exceptions.*;
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

  @ExceptionHandler(MemberBlockedException.class)
  ProblemDetail memberBlockedException(MemberBlockedException e) {
    ProblemDetail problemDetail =
            ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    problemDetail.setTitle("Member is blocked");
    log.error(e.toString());
    return problemDetail;
  }

  @ExceptionHandler(MovementNotFoundException.class)
  ProblemDetail movementNotFoundException(MovementNotFoundException e) {
    ProblemDetail problemDetail =
            ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    problemDetail.setTitle("Movement Not Found");
    log.error(e.toString());
    return problemDetail;
  }

  @ExceptionHandler(MatchNotFoundException.class)
  ProblemDetail matchNotFoundException(MatchNotFoundException e) {
    ProblemDetail problemDetail =
            ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    problemDetail.setTitle("Match Not Found");
    log.error(e.toString());
    return problemDetail;
  }

  @ExceptionHandler(NextMatchException.class)
  ProblemDetail nextMatchException(NextMatchException e) {
    ProblemDetail problemDetail =
            ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    problemDetail.setTitle("Next match not created yet");
    log.error(e.toString());
    return problemDetail;
  }

  @ExceptionHandler(MatchAlreadyExistsException.class)
  ProblemDetail matchAlreadyExistsException(MatchAlreadyExistsException e) {
    ProblemDetail problemDetail =
            ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    problemDetail.setTitle("Match already exists");
    log.error(e.toString());
    return problemDetail;
  }

  @ExceptionHandler(PlayerUnavailableException.class)
  ProblemDetail playerUnavailableException(PlayerUnavailableException e) {
    ProblemDetail problemDetail =
            ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    problemDetail.setTitle("Player unavailable");
    log.error(e.toString());
    return problemDetail;
  }
}
