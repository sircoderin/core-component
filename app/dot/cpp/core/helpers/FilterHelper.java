package dot.cpp.core.helpers;

import dev.morphia.aggregation.stages.Match;
import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;
import dot.cpp.core.builders.FilterBuilder;
import dot.cpp.core.utils.BindableLocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FilterHelper {

  private static final Logger logger = LoggerFactory.getLogger(FilterHelper.class);

  private FilterHelper() {}

  public static Filter and(Filter... filters) {
    return FilterBuilder.newInstance().and(filters).build();
  }

  public static Filter or(Filter... filters) {
    return FilterBuilder.newInstance().or(filters).build();
  }

  public static Filter getDateRangeFilter(
      String fieldName, BindableLocalDate startDate, BindableLocalDate endDate) {
    return Filters.and(
        Filters.gte(fieldName, startDate.getDate().toString()),
        Filters.lte(fieldName, endDate.getDate().toString()));
  }

  public static Filter contains(String fieldName, String value) {
    return Filters.regex(fieldName).pattern("(?i).*" + value + ".*");
  }

  public static Match getMatch(Filter filter) {
    return filter != null ? Match.match(filter) : Match.match();
  }
}
