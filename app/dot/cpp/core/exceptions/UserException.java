package dot.cpp.core.exceptions;

import dot.cpp.core.enums.Error;

public class UserException extends Exception {
  public UserException(Error error) {
    super(error.getMessage());
  }
}
