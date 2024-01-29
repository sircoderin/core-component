package dot.cpp.core.exceptions;

import dot.cpp.core.enums.ErrorCodes;

public class BaseException extends Exception {

  private final transient ErrorCode errorCode;

  public BaseException(ErrorCode errorCode) {
    super(errorCode.toString());
    this.errorCode = errorCode;
  }

  public static BaseException from(ErrorCodes errorCode) {
    return new BaseException(errorCode.getCode());
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
