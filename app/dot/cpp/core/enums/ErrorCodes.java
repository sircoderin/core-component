package dot.cpp.core.enums;

import dot.cpp.core.exceptions.ErrorCode;

public enum ErrorCodes {
  USER_NOT_FOUND(ErrorCode.from(1000, "user.not.found")),
  USER_EMAIL_NOT_FOUND(ErrorCode.from(1001, "user.email.not.found")),
  USER_EMAIL_EXISTS(ErrorCode.from(1002, "user.email.duplicate")),
  USER_NAME_EXISTS(ErrorCode.from(1003, "user.name.duplicate")),
  USER_ROLE_MISMATCH(ErrorCode.from(1004, "user.role.mismatch")),
  USER_INACTIVE_ACCOUNT(ErrorCode.from(1005, "user.inactive")),
  INCORRECT_PASSWORD(ErrorCode.from(1006, "login.password.invalid")),
  EXPIRED_ACCESS(ErrorCode.from(1007, "login.access.token.expired")),
  EXPIRED_REFRESH(ErrorCode.from(1008, "login.refresh.token.expired")),
  SESSION_NOT_FOUND(ErrorCode.from(1009, "session.not.found")),
  INVALID_JWT(ErrorCode.from(1010, "invalid.jwt")),
  FORM_VALIDATION_FAILED(ErrorCode.from(1011, "form.validation.errors")),
  GENERAL_ERROR(ErrorCode.from(1012, "general.application.error")),
  MISSING_REFRESH_TOKEN(ErrorCode.from(1013, "missing.refresh.token")),
  IP_NOT_FOUND(ErrorCode.from(1014, "ip.not.found")),
  IP_INVALID(ErrorCode.from(1015, "ip.invalid"));

  final ErrorCode code;

  ErrorCodes(ErrorCode code) {
    this.code = code;
  }

  public ErrorCode getCode() {
    return code;
  }
}
