package com.achilio.mvm.service.exception.controllers;

import com.achilio.mvm.service.exceptions.FetcherJobNotFoundException;
import com.achilio.mvm.service.exceptions.InvalidSettingsException;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.exceptions.QueryNotFoundException;
import com.achilio.mvm.service.exceptions.UnauthorizedException;
import com.google.cloud.resourcemanager.ResourceManagerException;
import java.util.Date;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger("ExceptionHandler");

  @ExceptionHandler(Exception.class)
  public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    LOGGER.error(ex.getMessage(), ex);
    return new ResponseEntity<>(exResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ClientAbortException.class)
  public final void handleClientAbortException(Exception ex) {
    LOGGER.warn(ex.getMessage());
  }

  @ExceptionHandler(ProjectNotFoundException.class)
  public final ResponseEntity<Object> projectNotFoundException(Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    return new ResponseEntity<>(exResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(FetcherJobNotFoundException.class)
  public final ResponseEntity<Object> fetcherJobNotFoundException(
      Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    return new ResponseEntity<>(exResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(QueryNotFoundException.class)
  public final ResponseEntity<Object> queryNotFoundException(
      Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    return new ResponseEntity<>(exResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public final ResponseEntity<Object> accessTokenInvalidException(
      Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    LOGGER.warn(ex.getMessage());
    return new ResponseEntity<>(exResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(InvalidSettingsException.class)
  public final ResponseEntity<Object> settingsInvalidException(Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    LOGGER.warn(ex.getMessage());
    return new ResponseEntity<>(exResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ResourceManagerException.class)
  public final ResponseEntity<Object> handleGoogleResourceManagerException(
      ResourceManagerException ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    LOGGER.warn(ex.getMessage());
    if ("insufficientPermissions".equals(ex.getReason())) {
      return new ResponseEntity<>(exResponse, HttpStatus.FORBIDDEN);
    }
    return new ResponseEntity<>(exResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /** Handling invalid User Fields send in the request. */
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getBindingResult().toString(), "Invalid User Fields");
    return new ResponseEntity<>(exResponse, HttpStatus.BAD_REQUEST);
  }
}
