package com.weather.api.shared.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.weather.api.shared.model.ErrorResponse;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
public final class ExceptionControllerAdvice {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex, WebRequest request) {

    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error ->
        errors.put(error.getObjectName(), error.getDefaultMessage()));

    log.error("Validation error: {}", errors);

    return ResponseEntity.badRequest().body(new ErrorResponse(
        OffsetDateTime.now(),
        BAD_REQUEST.value(),
        BAD_REQUEST.getReasonPhrase(),
        "Input validation error.",
        ((ServletWebRequest) request).getRequest().getRequestURI(),
        errors
    ));
  }

  /**
   * Ловить усі винятки що наслідуються від {@link ResourceException}, таким чином цей клас виступає
   * обгорткою над іншими винятками. Це дозволяє мати визначений ряд винятків, які вертають статус
   * код (і не тільки його), стандартизовано, уникаючи створення подібних винятків
   *
   * @param ex      виняток базовий або його наслідувачі
   * @param request запит для витягування URI
   * @return повертає помилку {@link ErrorResponse}, в якій міститься вся базова інформація про
   * помилку для клієнта
   */
  @ExceptionHandler(ResourceException.class)
  public ResponseEntity<ErrorResponse> apiHandlerExceptions(
      ResourceException ex, WebRequest request) {
    if (ex.getMessageLog() != null && !ex.getMessageLog().isEmpty()) {
      log.error(ex.getMessageLog(), ex);
    }

    Class<?> clazz = ex.getClass();
    log.info("Class: {}", clazz);
    if (clazz.isAnnotationPresent(ResponseStatus.class)) {
      ResponseStatus annotation = clazz.getAnnotation(ResponseStatus.class);
      HttpStatus status = annotation.value();
      int code = status.value();

      return ResponseEntity.status(code).body(new ErrorResponse(
          OffsetDateTime.now(), code,
          status.getReasonPhrase(),
          ex.getMessage(),
          ((ServletWebRequest) request).getRequest().getRequestURI(), null
      ));
    }

    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ErrorResponse(
        OffsetDateTime.now(),
        INTERNAL_SERVER_ERROR.value(),
        INTERNAL_SERVER_ERROR.getReasonPhrase(),
        "An unexpected server error occurred. Please try again later or contact support.",
        ((ServletWebRequest) request).getRequest().getRequestURI(),
        null
    ));
  }
}
