package com.achilio.mvm.service.exception.controllers;

import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.exceptions.UnauthorizedException;
import java.util.Date;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Handling exceptions with proper response. */
@RestController
@ControllerAdvice
public class CustomizedResponsedEntityExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(Exception.class)
  public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    return new ResponseEntity<Object>(exResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ProjectNotFoundException.class)
  public final ResponseEntity<Object> projectNotFoundException(Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    return new ResponseEntity<Object>(exResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public final ResponseEntity<Object> accessTokenInvalidException(
      Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    return new ResponseEntity<Object>(exResponse, HttpStatus.UNAUTHORIZED);
  }

  /** Handling invalid User Fields send in the request. */
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getBindingResult().toString(), "Invalid User Fields");
    return new ResponseEntity<Object>(exResponse, HttpStatus.BAD_REQUEST);
  }
}
