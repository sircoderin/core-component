package dot.cpp.core.enums;

public enum UserStatus {
  INACTIVE("Inactive"),
  ACTIVE("Active"),
  SUSPENDED("Suspended");

  final String message;

  UserStatus(String message) {
    this.message = message;
  }
}
