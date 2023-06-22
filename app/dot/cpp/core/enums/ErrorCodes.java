package dot.cpp.core.enums;

import dot.cpp.core.exceptions.ErrorCode;

public enum ErrorCodes {
  USER_NOT_FOUND(ErrorCode.from(1000, "user.not.found")),
  USER_EMAIL_NOT_FOUND(ErrorCode.from(1001, "user.email.not.found")),
  USER_ROLE_MISMATCH(ErrorCode.from(1002, "user.role.mismatch")),
  USER_INACTIVE_ACCOUNT(ErrorCode.from(1003, "user.inactive")),
  INCORRECT_PASSWORD(ErrorCode.from(1004, "login.password.invalid")),
  EXPIRED_ACCESS(ErrorCode.from(1005, "login.access.token.expired")),
  SESSION_NOT_FOUND(ErrorCode.from(1006, "session.not.found")),
  INVALID_JWT(ErrorCode.from(1007, "invalid.jwt")),
  FORM_VALIDATION_FAILED(ErrorCode.from(1008, "form.validation.errors"));

  final ErrorCode code;

  ErrorCodes(ErrorCode code) {
    this.code = code;
  }

  public ErrorCode getCode() {
    return code;
  }
}