package dot.cpp.core.helpers;

public final class PaginationHelper {

  public static int getSkip(int page, int pageSize) {
    return (page - 1) * pageSize;
  }

  public static int getPagesNumber(int entriesNo, int pageSize) {
    return entriesNo % pageSize == 0 ? entriesNo / pageSize : entriesNo / pageSize + 1;
  }
}
