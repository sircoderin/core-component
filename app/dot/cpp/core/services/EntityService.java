package dot.cpp.core.services;

import com.typesafe.config.Config;
import dev.morphia.query.Sort;
import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;
import dot.cpp.core.exceptions.BaseException;
import dot.cpp.core.exceptions.EntityNotFoundException;
import dot.cpp.core.helpers.ValidationHelper;
import dot.cpp.core.models.BaseRequest;
import dot.cpp.core.models.HistoryEntry;
import dot.cpp.core.models.user.entity.User;
import dot.cpp.core.models.user.repository.UserRepository;
import dot.cpp.repository.models.BaseEntity;
import dot.cpp.repository.repository.BaseRepository;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

public abstract class EntityService<T extends BaseEntity, S extends BaseRequest> {

  private static final String INVALID = "invalid";
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd-MM-yyyy");

  private final BaseRepository<T> repository;
  private final int pageSize;

  @Inject private UserRepository userRepository;

  protected EntityService(BaseRepository<T> repository, Config config) {
    this.repository = repository;
    this.pageSize = config.getInt("list.page.size");
  }

  protected BaseRepository<T> getRepository() {
    return repository;
  }

  private static boolean isInvalidId(String id) {
    return ValidationHelper.isEmpty(id) || !ObjectId.isValid(id);
  }

  public T findById(String id) throws EntityNotFoundException {
    if (isInvalidId(id)) {
      throw new EntityNotFoundException();
    }

    final var entity = repository.findById(id);
    if (entity == null) {
      throw new EntityNotFoundException();
    }

    return entity;
  }

  public T findByHistoryId(String id) throws EntityNotFoundException {
    if (isInvalidId(id)) {
      throw new EntityNotFoundException();
    }

    final var entity = repository.findByHistoryId(id);
    if (entity == null) {
      throw new EntityNotFoundException();
    }

    return entity;
  }

  public T findByField(String field, String value) throws EntityNotFoundException {
    if (ValidationHelper.isEmpty(field) || ValidationHelper.isEmpty(value)) {
      throw new EntityNotFoundException();
    }
    final var entity = repository.findByField(field, value);
    if (entity == null) {
      throw new EntityNotFoundException();
    }
    return entity;
  }

  public List<T> list(int skip, int length, Sort... sortBy) {
    return repository.list(null, skip, length, sortBy);
  }

  public List<T> list(Filter filter, int skip, int length, Sort... sortBy) {
    return repository.list(filter, skip, length, sortBy);
  }

  public List<T> listByIds(List<String> ids, Sort... sortBy) {
    return listByFieldWithPossibleValues(
        "_id", ids.stream().map(ObjectId::new).collect(Collectors.toList()), sortBy);
  }

  public List<T> listByField(String field, String value, Sort... sortBy) {
    return repository.listByField(field, value, sortBy);
  }

  public List<T> listByFieldWithPossibleValues(String field, List<?> values, Sort... sortBy) {
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

  public List<T> listHistory(String trackingId) {
    return repository.listHistory(trackingId);
  }

  public <U> List<U> getEntitiesByPage(List<U> entities, int pageNum) {
    final var toIndex = Math.min(entities.size(), pageNum * pageSize);
    return entities.subList((pageNum - 1) * pageSize, toIndex);
  }

  public T getEntityWithMaxFieldValue(String field) {
    return repository.getFirstSorted(Sort.descending(field));
  }

  public T getEntityWithMinFieldValue(String field) {
    return repository.getFirstSorted(Sort.ascending(field));
  }

  public long count() {
    return repository.count();
  }

  public long count(Filter filter) {
    return filter == null ? repository.count() : repository.count(filter);
  }

  public long sum(String field) {
    return repository.sum(field);
  }

  public long sum(String field, Filter filter) {
    return filter == null ? repository.sum(field) : repository.sum(field, filter);
  }

  public int getNumberOfPages() {
    final var numEntities = count();
    return getNumberOfPages(numEntities);
  }

  public int getNumberOfPages(Filter filter) {
    final var numEntities = count(filter);
    return getNumberOfPages(numEntities);
  }

  public int getNumberOfPages(long numEntities) {
    final var numberOfPages =
        numEntities % pageSize == 0 ? numEntities / pageSize : numEntities / pageSize + 1;
    return (int) numberOfPages;
  }

  public void save(T entity) {
    repository.save(entity);
  }

  public void save(String id, S request) throws BaseException {
    final var entity = findByIdOrGetNewEntity(id);
    setEntityFromRequest(entity, request);

    saveWithHistory(entity, request.getUserId());
    processAfterSave(entity);
  }

  public void saveWithHistory(T entity) {
    repository.saveWithHistory(entity);
  }

  public void saveWithHistory(T entity, String userId) {
    entity.setModifiedBy(userId);
    repository.saveWithHistory(entity);
  }

  public void delete(T entity) {
    repository.delete(entity);
  }

  public S getRequest(String id) throws BaseException {
    final var request = getNewRequest();
    if (ValidationHelper.isEmpty(id)) {
      return request;
    }

    final var entity = findById(id);
    setRequestFromEntity(request, entity);

    return request;
  }

  public T findByIdOrGetNewEntity(String id) throws EntityNotFoundException {
    return ValidationHelper.isEmpty(id) ? getNewEntity() : findById(id);
  }

  public List<HistoryEntry> getHistoryEntriesById(String id) throws EntityNotFoundException {
    if (ValidationHelper.isEmpty(id)) {
      return List.of();
    }

    final var entity = findById(id);
    return getHistoryEntries(entity);
  }

  public List<HistoryEntry> getHistoryEntriesByTrackingId(String trackingId)
      throws EntityNotFoundException {
    if (ValidationHelper.isEmpty(trackingId)) {
      return List.of();
    }

    final var entity = findByField("trackingId", trackingId);
    return getHistoryEntries(entity);
  }

  @NotNull
  private List<HistoryEntry> getHistoryEntries(BaseEntity currentState) {
    final var historyEntries = new ArrayList<HistoryEntry>();
    final var historyStates = listHistory(currentState.getTrackingId());

    final var userIdSet =
        historyStates.stream()
            .map(entity -> new ObjectId(entity.getModifiedBy()))
            .collect(Collectors.toSet());
    userIdSet.add(new ObjectId(currentState.getModifiedBy()));

    final var users =
        userRepository.listWithFilter(Filters.in("_id", userIdSet)).stream()
            .collect(Collectors.toMap(User::getStrId, User::getUserName));

    historyEntries.add(getHistoryEntry(users, currentState));
    historyStates.forEach(
        historyEntity -> historyEntries.add(getHistoryEntry(users, historyEntity)));

    return historyEntries;
  }

  @NotNull
  private HistoryEntry getHistoryEntry(Map<String, String> users, BaseEntity entityState) {
    return new HistoryEntry(
        users.getOrDefault(entityState.getModifiedBy(), INVALID),
        dateFormat.format(new Date(entityState.getModifiedAt() * 1000L)),
        entityState.getModifiedComment(),
        entityState.getStrId());
  }

  public void setEntityFromRequest(T entity, S request) throws BaseException {
    BeanUtils.copyProperties(request, entity);
  }

  public void setRequestFromEntity(S request, T entity) throws BaseException {
    BeanUtils.copyProperties(entity, request);
  }

  protected void processAfterSave(T entity) throws BaseException {}

  public abstract T getNewEntity();

  public abstract S getNewRequest();
}
