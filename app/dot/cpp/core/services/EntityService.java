package dot.cpp.core.services;

import static dot.cpp.core.helpers.PaginationHelper.getPagesNumber;
import static dot.cpp.core.helpers.ValidationHelper.isEmpty;
import static dot.cpp.repository.models.BaseEntity.RECORD_ID;

import com.typesafe.config.Config;
import dev.morphia.query.Sort;
import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;
import dot.cpp.core.enums.ErrorCodes;
import dot.cpp.core.exceptions.BaseException;
import dot.cpp.core.models.BaseRequest;
import dot.cpp.core.models.HistoryEntry;
import dot.cpp.core.models.user.entity.User;
import dot.cpp.core.models.user.repository.UserRepository;
import dot.cpp.repository.models.BaseEntity;
import dot.cpp.repository.repository.BaseRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.Validation;
import javax.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

public abstract class EntityService<T extends BaseEntity, S extends BaseRequest> {

  private static final String INVALID = "invalid";
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final BaseRepository<T> repository;
  private final Validator validator;
  protected final int pageSize;

  @Inject private UserRepository userRepository;

  protected EntityService(BaseRepository<T> repository, Config config) {
    this.repository = repository;
    try (final var factory =
        Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()) {
      validator = factory.getValidator();
    }
    this.pageSize = config.getInt("list.page.size");
  }

  protected BaseRepository<T> getRepository() {
    return repository;
  }

  public T findById(String id) throws BaseException {
    if (isEmpty(id)) {
      throw notFoundException();
    }

    final var entity = repository.findById(id);
    if (entity == null) {
      throw notFoundException();
    }

    return entity;
  }

  public T findHistoryRecord(String id, Long timestamp) throws BaseException {
    if (isEmpty(id)) {
      throw notFoundException();
    }

    final var entity = repository.findHistoryRecord(id, timestamp);
    if (entity == null) {
      throw notFoundException();
    }

    return entity;
  }

  public T findByField(String field, String value) throws BaseException {
    if (isEmpty(field) || isEmpty(value)) {
      throw notFoundException();
    }
    final var entity = repository.findByField(field, value);
    if (entity == null) {
      throw notFoundException();
    }
    return entity;
  }

  public List<T> list(int skip, int length, Sort... sortBy) {
    return repository.list(null, skip, length, sortBy);
  }

  public List<T> list(Filter filter, int skip, int length, Sort... sortBy) {
    return repository.list(filter, skip, length, sortBy);
  }

  public List<T> listByIds(Collection<String> ids, Sort... sortBy) {
    return ids.isEmpty() ? List.of() : listByFieldWithPossibleValues(RECORD_ID, ids, sortBy);
  }

  public List<T> listByField(String field, String value, Sort... sortBy) {
    return repository.listByField(field, value, sortBy);
  }

  public List<T> listByFieldWithPossibleValues(String field, Collection<?> values, Sort... sortBy) {
    return repository.listWithFilter(Filters.in(field, values), sortBy);
  }

  public List<T> listAll(Sort... sortBy) {
    return repository.listAll(sortBy);
  }

  public List<T> listWithFilter(Filter filter, Sort... sortBy) {
    return filter == null ? repository.listAll(sortBy) : repository.listWithFilter(filter, sortBy);
  }

  public List<T> listAllPaginated(int pageNum, Sort... sortBy) {
    return repository.listAllPaginated(pageSize, pageNum - 1, sortBy);
  }

  public List<T> listWithFilterPaginated(Filter filter, int pageNum, Sort... sortBy) {
    return filter == null
        ? repository.listAllPaginated(pageSize, pageNum - 1, sortBy)
        : repository.listWithFilterPaginated(filter, pageSize, pageNum - 1, sortBy);
  }

  public List<T> listHistoryRecords(String id) {
    return repository.listHistoryRecords(id);
  }

  public <U> List<U> getEntitiesByPage(List<U> entities, int pageNum) {
    final var toIndex = Math.min(entities.size(), pageNum * pageSize);
    return entities.subList((pageNum - 1) * pageSize, toIndex);
  }

  public T findFirst(Filter filter) {
    return repository.findFirst(filter);
  }

  public T findFirst(Sort sort) {
    return repository.findFirst(sort);
  }

  public T findFirst(Filter filter, Sort sort) {
    return repository.findFirst(filter, sort);
  }

  public long count() {
    return repository.count();
  }

  public long count(Filter filter) {
    return filter == null ? repository.count() : repository.count(filter);
  }

  public boolean exists(Filter filter) {
    return count(filter) > 0L;
  }

  public long sum(String field) {
    return repository.sum(field);
  }

  public long sum(String field, Filter filter) {
    return filter == null ? repository.sum(field) : repository.sum(field, filter);
  }

  public int getNumberOfPages() {
    return getPagesNumber((int) count(), pageSize);
  }

  public int getNumberOfPages(int size) {
    return getPagesNumber(size, pageSize);
  }

  public int getNumberOfPages(Filter filter) {
    return getPagesNumber((int) count(filter), pageSize);
  }

  public T save(T entity) throws BaseException {
    validateEntity(entity);
    return repository.save(entity);
  }

  public List<T> save(List<T> entities) throws BaseException {
    for (var entity : entities) {
      validateEntity(entity);
    }
    return repository.save(entities);
  }

  public T save(String id, S request) throws BaseException {
    final var entity = findByIdOrGetNewEntity(id);
    setEntityFromRequest(entity, request);

    saveWithHistory(entity, request.getUserId());
    processAfterSave(entity, request.getUserId());

    return entity;
  }

  public T saveWithHistory(T entity) throws BaseException {
    validateEntity(entity);
    return repository.saveWithHistory(entity);
  }

  public T saveWithHistory(T entity, String userId) throws BaseException {
    validateEntity(entity);
    entity.setModifiedBy(userId);
    return repository.saveWithHistory(entity);
  }

  private void validateEntity(T entity) throws BaseException {
    final var violations = validator.validate(entity);
    if (!violations.isEmpty()) {
      logger.error("{}", violations);
      throw BaseException.from(ErrorCodes.GENERAL_ERROR);
    }
  }

  public void delete(T entity) {
    repository.delete(entity);
  }

  public long delete(Filter filter) {
    return repository.deleteWithFilter(filter);
  }

  public S getRequest(String id) throws BaseException {
    return getRequest(id, null);
  }

  public S getRequest(String id, Long timestamp) throws BaseException {
    final var request = getNewRequest();
    if (isEmpty(id)) {
      return request;
    }

    final var entity = findByIdAndTimestamp(id, timestamp);
    setRequestFromEntity(request, entity);

    return request;
  }

  public T findByIdAndTimestamp(String id, Long timestamp) throws BaseException {
    return (timestamp != null && timestamp > 0L) ? findHistoryRecord(id, timestamp) : findById(id);
  }

  public T findByIdOrGetNewEntity(String id) throws BaseException {
    return isEmpty(id) ? getNewEntity() : findById(id);
  }

  public List<HistoryEntry> getHistoryEntries(String id) throws BaseException {
    if (isEmpty(id)) {
      return List.of();
    }

    final var currentState = findById(id);
    final var historyStates = listHistoryRecords(id);

    final var userIdSet =
        historyStates.stream().map(BaseEntity::getModifiedBy).collect(Collectors.toSet());
    userIdSet.add(currentState.getModifiedBy());

    final var users =
        userRepository.listWithFilter(Filters.in(RECORD_ID, userIdSet)).stream()
            .collect(Collectors.toMap(User::getRecordId, User::getUserName));

    final var historyEntries = new ArrayList<HistoryEntry>();

    historyEntries.add(getHistoryEntry(users, currentState));
    historyStates.forEach(
        historyEntity -> historyEntries.add(getHistoryEntry(users, historyEntity)));

    return historyEntries;
  }

  @NotNull
  public static <V extends BaseEntity> Map<String, V> mapIdToBaseEntity(List<V> entities) {
    return entities.stream()
        .collect(Collectors.toMap(BaseEntity::getRecordId, Function.identity()));
  }

  @NotNull
  public static <V extends BaseEntity> Collection<String> getIds(List<V> entities) {
    return entities.stream().map(BaseEntity::getRecordId).collect(Collectors.toSet());
  }

  @NotNull
  private HistoryEntry getHistoryEntry(Map<String, String> users, BaseEntity entityState) {
    return new HistoryEntry(
        users.getOrDefault(entityState.getModifiedBy(), INVALID),
        entityState.getModifiedAt(),
        entityState.getModifiedComment());
  }

  public void setEntityFromRequest(T entity, S request) throws BaseException {
    BeanUtils.copyProperties(request, entity);
  }

  public void setRequestFromEntity(S request, T entity) throws BaseException {
    BeanUtils.copyProperties(entity, request);
  }

  public void emptyCollection() {
    repository.emptyCollection();
  }

  protected void processAfterSave(T entity, String userId) throws BaseException {}

  public abstract T getNewEntity();

  public abstract S getNewRequest();

  protected abstract BaseException notFoundException();
}
