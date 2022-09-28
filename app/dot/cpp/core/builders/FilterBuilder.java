package dot.cpp.core.builders;

import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FilterBuilder {
  private Filter filter;

  private FilterBuilder() {}

  private FilterBuilder(Filter filter) {
    this.filter = filter;
  }

  public static FilterBuilder newInstance() {
    return new FilterBuilder();
  }

  public static FilterBuilder newInstance(Filter filter) {
    return new FilterBuilder(filter);
  }

  public Filter build() {
    return filter;
  }

  public FilterBuilder and(List<Filter> filters) {
    if (!filters.isEmpty()) {
      this.filter = Filters.and(getFilters(filters));
    }
    return this;
  }

  public FilterBuilder and(Filter... filters) {
    return and(getNonNullFilters(filters));
  }

  public FilterBuilder or(List<Filter> filters) {
    if (!filters.isEmpty()) {
      this.filter = Filters.or(getFilters(filters));
    }
    return this;
  }

  public FilterBuilder or(Filter... filters) {
    return or(new ArrayList<>(List.of(filters)));
  }

  public FilterBuilder nor(List<Filter> filters) {
    if (!filters.isEmpty()) {
      this.filter = Filters.nor(getFilters(filters));
    }
    return this;
  }

  public FilterBuilder nor(Filter... filters) {
    return nor(new ArrayList<>(List.of(filters)));
  }

  private Filter[] getFilters(List<Filter> filters) {
    if (filter != null) {
      filters.add(filter);
    }
    return getNonNullFilters(filters);
  }

  public List<Filter> getNonNullFilters(Filter[] filters) {
    return Arrays.stream(filters).filter(Objects::nonNull).collect(Collectors.toList());
  }

  public Filter[] getNonNullFilters(List<Filter> filters) {
    return filters.stream().filter(Objects::nonNull).toArray(Filter[]::new);
  }
}
