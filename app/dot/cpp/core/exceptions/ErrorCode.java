package dot.cpp.core.exceptions;

import play.libs.Json;

public class ErrorCode {

  private Integer code;
  private String message;
  private Object details;

  private ErrorCode(Integer code, String message, Object details) {
    this.code = code;
    this.message = message;
    this.details = details;
  }

  public static ErrorCode from(Integer code, String message, Object details) {
    return new ErrorCode(code, message, details);
  }

  public static ErrorCode from(Integer code, String message) {
    return new ErrorCode(code, message, null);
  }

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Object getDetails() {
    return details;
  }

  public void setDetails(Object details) {
    this.details = details;
  }

  @Override
  public String toString() {
    return Json.toJson(this).toString();
  }
}
