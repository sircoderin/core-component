package dot.cpp.core.exceptions;

public class EntityNotFoundException extends Exception {
  public EntityNotFoundException() {
    super("general.recordNotFound");
  }
}
