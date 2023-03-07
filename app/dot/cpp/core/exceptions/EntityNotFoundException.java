package dot.cpp.core.exceptions;

public class EntityNotFoundException extends BaseException {
  public EntityNotFoundException() {
    super("general.recordNotFound");
  }
}
