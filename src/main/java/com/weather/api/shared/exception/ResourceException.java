package com.weather.api.shared.exception;

public class ResourceException extends RuntimeException {

  private String messageLog;

  public ResourceException(String message) {
    super(message);
  }

  public ResourceException(String message, Throwable cause) {
    super(message, cause);
  }

  public ResourceException(String message, Throwable cause, String messageLog) {
    super(message, cause);
    this.messageLog = messageLog;
  }

  public String getMessageLog() {
    return messageLog;
  }

  public void setMessageLog(String messageLog) {
    this.messageLog = messageLog;
  }
}
