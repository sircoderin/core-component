package dot.cpp.core.services;

import com.typesafe.config.Config;
import dev.morphia.query.Sort;
import dev.morphia.query.filters.Filter;
import dot.cpp.core.builders.FilterBuilder;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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

  public T findById(String id) throws EntityNotFoundException {
    if (ValidationHelper.isEmpty(id)) {
      throw new EntityNotFoundException();
    }

    final var entity = repository.findById(id);
    if (entity == null) {
      throw new EntityNotFoundException();
    }

    return entity;
  }

  public T findByHistoryId(String id) throws EntityNotFoundException {
    if (ValidationHelper.isEmpty(id)) {
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

  public List<T> listByIds(List<String> ids, Sort... sortBy) {
    return listByFieldWithPossibleValues(
        "_id", ids.stream().map(ObjectId::new).collect(Collectors.toList()), sortBy);
  }

  public List<T> listByField(String field, String value, Sort... sortBy) {
    return repository.listByField(field, value, sortBy);
  }

  public List<T> listByFieldWithPossibleValues(String field, List<?> values, Sort... sortBy) {
    return repository.listWithFilter(
        FilterBuilder.newInstance().orEq(field, values).build(), sortBy);
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

  // sending an entityId and an entity at the same time is redundant
  // use findByIdOrGetNewEntity where needed and replace this behaviour
  public void save(String entityId, T entity, S request, Consumer<T>... consumers) {

    BeanUtils.copyProperties(request, entity);
    for (Consumer<T> consumer : consumers) {
      consumer.accept(entity);
    }

    if (ValidationHelper.isNotEmpty(entityId)) {
      entity.setId(new ObjectId(entityId));
    }

    saveWithHistory(entity, request.getUserId());
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

  public S getRequest(String id, BiConsumer<S, T>... consumers) throws EntityNotFoundException {
    final var request = getNewRequest();
    if (ValidationHelper.isEmpty(id)) {
      return request;
    }

    final var dbEntity = findById(id);
    BeanUtils.copyProperties(dbEntity, request);
    for (BiConsumer<S, T> consumer : consumers) {
      consumer.accept(request, dbEntity);
    }

    return request;
  }

  public T findByIdOrGetNewEntity(String id) throws EntityNotFoundException {
    return ValidationHelper.isNotEmpty(id) ? findById(id) : getNewEntity();
  }

  public List<HistoryEntry> getHistoryEntriesById(String id) throws EntityNotFoundException {
    final var entity = findById(id);
    return getHistoryEntries(entity);
  }

  public List<HistoryEntry> getHistoryEntriesByTrackingId(String trackingId)
      throws EntityNotFoundException {
    final var entity = findByField("trackingId", trackingId);
    return getHistoryEntries(entity);
  }

  @NotNull
  private List<HistoryEntry> getHistoryEntries(BaseEntity currentState) {
    final var historyEntries = new ArrayList<HistoryEntry>();
    final var historyStates = listHistory(currentState.getTrackingId());

    final var modifiedByUserIdSet =
        historyStates.stream().map(BaseEntity::getModifiedBy).collect(Collectors.toSet());
    modifiedByUserIdSet.add(currentState.getModifiedBy());

    final var users =
        userRepository
            .listWithFilter(
                FilterBuilder.newInstance()
                    .orEq(
                        "_id",
                        new ArrayList<>(modifiedByUserIdSet)
                            .stream().map(ObjectId::new).collect(Collectors.toList()))
                    .build())
            .stream()
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

  public abstract T getNewEntity();

  public abstract S getNewRequest();
}
