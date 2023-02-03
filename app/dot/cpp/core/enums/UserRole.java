package dot.cpp.core.enums;

public enum UserRole {
  USER("User"),
  ADMIN("Admin");

  private final String value;

  UserRole(final String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
