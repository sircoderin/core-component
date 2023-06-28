package dot.cpp.core.exceptions;

public class LoginException extends BaseException {

  public LoginException(ErrorCode errorCode) {
    super(errorCode);
  }
}
