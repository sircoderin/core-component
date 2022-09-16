package dot.cpp.core.helpers;

import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import dot.cpp.core.services.EntityService;
import dot.cpp.core.utils.BindableLocalDate;
import dot.cpp.repository.models.BaseEntity;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FilterHelper {

  private static final Logger logger = LoggerFactory.getLogger(FilterHelper.class);

  private FilterHelper() {}

  public static Filter getDateRangeFilter(
      String fieldName, BindableLocalDate startDate, BindableLocalDate endDate) {
    return Filters.and(
        Filters.gte(fieldName, startDate.getDate().toString()),
        Filters.lte(fieldName, endDate.getDate().toString()));
  }

  public static List<Filter> getIdFilters(
      EntityService<? extends BaseEntity> entityService,
      String searchValue,
      String searchFieldName,
      String idFieldName) {
    return entityService.listWithFilter(Filters.regex(searchFieldName).pattern(searchValue))
        .stream()
        .map(entity -> Filters.eq(idFieldName, entity.getId().toString()))
        .collect(Collectors.toList());
  }
}
