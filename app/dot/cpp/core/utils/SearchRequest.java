package dot.cpp.core.utils;

import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.validation.Constraints;
import play.libs.Json;
import play.mvc.QueryStringBindable;

public class SearchRequest implements QueryStringBindable<SearchRequest> {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Constraints.MaxLength(value = 200, message = "constraints.field.invalid")
  private String filter;

  /** Creates a SearchRequest from filter. */
  public static SearchRequest from(String filter) {
    final var searchRequest = new SearchRequest();
    searchRequest.setFilter(filter);
    return searchRequest;
  }

  /** Creates an empty SearchRequest. */
  public static SearchRequest empty() {
    return SearchRequest.from("");
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  /**
   * Get filter for field.
   *
   * @return {@link Filter}
   */
  public Filter getFilterForField(String field) {
    if (filter == null || filter.isBlank()) {
      return null;
    }

    return Filters.regex(field).pattern("(?i).*" + filter + ".*");
  }

  /**
   * Get filter for multiple fields.
   *
   * @return {@link Filter}
   */
  public Filter getFilterForFields(List<String> filterFields) {
    if (filter == null || filter.isBlank()) {
      return null;
    }

    final var filters = new ArrayList<Filter>();
    filterFields.forEach(
        field -> filters.add(Filters.regex(field).pattern("(?i).*" + filter + ".*")));

    return Filters.or(filters.toArray(new Filter[0]));
  }

  @Override
  public Optional<SearchRequest> bind(String key, Map<String, String[]> data) {
    final var possibleFilter = data.get("filter");
    if (possibleFilter == null) {
      return Optional.empty();
    }
    return Optional.of(SearchRequest.from(possibleFilter[0]));
  }

  @Override
  public String unbind(String key) {
    return filter != null ? "filter=" + filter : "";
  }

  @Override
  public String javascriptUnbind() {
    return Json.stringify(Json.toJson(this));
  }

  @Override
  public String toString() {
    return Json.stringify(Json.toJson(this));
  }
}
