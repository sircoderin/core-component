package dot.cpp.core.enums;

public enum UserRole {
  USER("Utilizator"),
  ADMIN("Administrator");

  private final String value;

  UserRole(final String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
