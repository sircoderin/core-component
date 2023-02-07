package dot.cpp.core.exceptions;

import dot.cpp.core.enums.Error;

public class LoginException extends Exception {
  public LoginException(Error error) {
    super(error.getMessage());
  }
}
