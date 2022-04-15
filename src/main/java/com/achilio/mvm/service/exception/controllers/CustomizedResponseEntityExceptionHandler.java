package com.achilio.mvm.service.exception.controllers;

import com.achilio.mvm.service.exceptions.ConnectionInUseException;
import com.achilio.mvm.service.exceptions.InvalidPayloadException;
import com.achilio.mvm.service.exceptions.InvalidSettingsException;
import com.achilio.mvm.service.exceptions.MaterializedViewAppliedException;
import com.achilio.mvm.service.exceptions.NotFoundException;
import com.achilio.mvm.service.exceptions.UnauthorizedException;
import com.google.api.gax.rpc.PermissionDeniedException;
import com.google.cloud.resourcemanager.ResourceManagerException;
import java.util.Date;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
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

  @ExceptionHandler(ClientAbortException.class)
  public final void handleClientAbortException(Exception ex) {
    LOGGER.warn(ex.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  public final ResponseEntity<Object> NotFoundException(Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    return new ResponseEntity<>(exResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ConnectionInUseException.class)
  public final ResponseEntity<Object> ConnectionIsUseException(Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    return new ResponseEntity<>(exResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MaterializedViewAppliedException.class)
  public final ResponseEntity<Object> MaterializedViewAppliedException(
      Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    return new ResponseEntity<>(exResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(InvalidPayloadException.class)
  public final ResponseEntity<Object> InvalidPayloadException(Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    return new ResponseEntity<>(exResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public final ResponseEntity<Object> accessTokenInvalidException(
      Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    LOGGER.warn(ex.getMessage());
    return new ResponseEntity<>(exResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public final ResponseEntity<Object> handleAccessDeniedException(
      Exception ex, WebRequest request) {
    if (ex.getMessage().toLowerCase().contains("access is denied")) {
      LOGGER.warn(ex.getMessage());
      return new ResponseEntity<>("Unauthorized Access", new HttpHeaders(), HttpStatus.FORBIDDEN);
    }
    LOGGER.error(ex.getMessage(), ex);
    return new ResponseEntity<>(
        ex.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
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
    if ("insufficientPermissions".equals(ex.getReason())) {
      LOGGER.warn(ex.getMessage());
      return new ResponseEntity<>(exResponse, HttpStatus.UNAUTHORIZED);
    }
    LOGGER.error(ex.getMessage(), ex);
    return new ResponseEntity<>(exResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(PermissionDeniedException.class)
  public final ResponseEntity<Object> handleGooglePermissionDenied(
      PermissionDeniedException ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    LOGGER.warn(ex.getMessage());
    return new ResponseEntity<>(exResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(Exception.class)
  public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    LOGGER.error(ex.getMessage(), ex);
    return new ResponseEntity<>(exResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /** Handling invalid User Fields send in the request. */
  protected @NonNull ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatus status,
      @NonNull WebRequest request) {
    ExceptionResponse exResponse =
        new ExceptionResponse(new Date(), ex.getBindingResult().toString(), "Invalid User Fields");
    return new ResponseEntity<>(exResponse, HttpStatus.BAD_REQUEST);
  }
}
