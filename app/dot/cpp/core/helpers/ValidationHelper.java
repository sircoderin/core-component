package dot.cpp.core.helpers;

public final class ValidationHelper {
  public static boolean isEmpty(String string) {
    return string == null || string.isBlank();
  }
}
