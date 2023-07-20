package dot.cpp.core.exceptions;

import dot.cpp.core.enums.ErrorCodes;

public class LoginException extends BaseException {

  public LoginException(ErrorCode errorCode) {
    super(errorCode);
  }

  public static LoginException from(ErrorCodes errorCode) {
    return new LoginException(errorCode.getCode());
  }
}
