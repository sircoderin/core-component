package dot.cpp.core.helpers;

import static dot.cpp.core.helpers.ValidationHelper.isEmpty;

import dev.morphia.aggregation.expressions.Expressions;
import dev.morphia.aggregation.expressions.impls.Expression;
import dev.morphia.aggregation.stages.Match;
import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;
import dot.cpp.core.builders.FilterBuilder;
import dot.cpp.core.utils.BindableLocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
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
    return isEmpty(value) ? null : Filters.regex(fieldName).pattern("(?i).*" + value + ".*");
  }

  public static Filter in(String field, List<String> values) {
    return values.isEmpty() ? null : Filters.in(field, values);
  }

  public static Filter lte(String field, String value) {
    return getFilterOrNull(Filters::lte, field, value);
  }

  public static Filter lt(String field, String value) {
    return getFilterOrNull(Filters::lt, field, value);
  }

  public static Filter gte(String field, String value) {
    return getFilterOrNull(Filters::gte, field, value);
  }

  public static Filter gt(String field, String value) {
    return getFilterOrNull(Filters::gt, field, value);
  }

  public static Filter elemMatch(String field, Filter filter) {
    return filter == null ? null : Filters.elemMatch(field, filter);
  }

  public static Match getMatch(Filter... filter) {
    return Match.match(Arrays.stream(filter).filter(Objects::nonNull).toArray(Filter[]::new));
  }

  public static Expression localField(String fieldName) {
    return Expressions.field(String.format("$$%s", fieldName));
  }

  private static Filter getFilterOrNull(
      BiFunction<String, String, Filter> filter, String field, String value) {
    return isEmpty(value) ? null : filter.apply(field, value);
  }
}
