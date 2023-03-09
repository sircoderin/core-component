package dot.cpp.core.builders;

import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
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
    return operation(filters, Filters::and);
  }

  public FilterBuilder and(Filter... filters) {
    return and(getFiltersListFromArray(filters));
  }

  public FilterBuilder andEq(String fieldName, List<?> values) {
    return and(eq(fieldName, values));
  }

  public FilterBuilder or(List<Filter> filters) {
    return operation(filters, Filters::or);
  }

  public FilterBuilder or(Filter... filters) {
    return or(getFiltersListFromArray(filters));
  }

  public FilterBuilder nor(List<Filter> filters) {
    return operation(filters, Filters::nor);
  }

  public FilterBuilder nor(Filter... filters) {
    return nor(getFiltersListFromArray(filters));
  }

  public FilterBuilder operation(List<Filter> filters, Function<Filter[], Filter> function) {
    if (!filters.isEmpty()) {
      if (filter != null) {
        filters.add(filter);
      }

      this.filter = function.apply(getFiltersArrayFromList(filters));
    }
    return this;
  }

  private Filter[] eq(String fieldName, List<?> values) {
    return values.stream().map(value -> Filters.eq(fieldName, value)).toArray(Filter[]::new);
  }

  private List<Filter> getFiltersListFromArray(Filter[] filters) {
    return Arrays.stream(filters).filter(Objects::nonNull).collect(Collectors.toList());
  }

  private Filter[] getFiltersArrayFromList(List<Filter> filters) {
    return filters.stream().filter(Objects::nonNull).toArray(Filter[]::new);
  }
}
