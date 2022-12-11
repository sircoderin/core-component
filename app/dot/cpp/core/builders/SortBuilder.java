package dot.cpp.core.builders;

import dev.morphia.query.Sort;
import java.util.ArrayList;
import java.util.List;

public class SortBuilder {

  List<Sort> sortBy;

  private SortBuilder() {
    sortBy = new ArrayList<>();
  }

  public static SortBuilder newInstance() {
    return new SortBuilder();
  }

  public SortBuilder addAscending(String field) {
    sortBy.add(Sort.ascending(field));
    return this;
  }

  public SortBuilder addDescending(String field) {
    sortBy.add(Sort.descending(field));
    return this;
  }

  public SortBuilder removeField(String field) {
    sortBy.removeIf(sort -> sort.getField().equals(field));
    return this;
  }

  public Sort[] build() {
    return sortBy.toArray(Sort[]::new);
  }
}
