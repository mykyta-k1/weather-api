package com.weather.api.exception;

import com.weather.api.shared.exception.ResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends ResourceException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
