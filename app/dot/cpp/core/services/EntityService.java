package dot.cpp.core.services;

import com.typesafe.config.Config;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import dot.cpp.core.builders.FilterBuilder;
import dot.cpp.core.exceptions.EntityNotFoundException;
import dot.cpp.core.interfaces.BaseRequest;
import dot.cpp.repository.models.BaseEntity;
import dot.cpp.repository.repository.BaseRepository;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;

public class EntityService<T extends BaseEntity> {
  private final BaseRepository<T> repository;
  private final int pageSize;

  public EntityService(BaseRepository<T> repository, Config config) {
    this.repository = repository;
    this.pageSize = config.getInt("list.page.size");
  }

  public T findById(String id) throws EntityNotFoundException {
    if (isEmpty(id)) {
      throw new EntityNotFoundException();
    }

    final var entity = repository.findById(id);
    if (entity == null) {
      throw new EntityNotFoundException();
    }
    return entity;
  }

  public T findByField(String field, String value) throws EntityNotFoundException {
    if (isEmpty(field) || isEmpty(value)) {
      throw new EntityNotFoundException();
    }
    final var entity = repository.findByField(field, value);
    if (entity == null) {
      throw new EntityNotFoundException();
    }
    return entity;
  }

  private static boolean isEmpty(String string) {
    return string == null || string.isBlank();
  }

  public List<T> listByIds(List<String> values) {
    return repository.listWithFilter(
        FilterBuilder.newInstance()
            .or(
                values.stream()
                    .map(value -> Filters.eq("_id", new ObjectId(value)))
                    .toArray(Filter[]::new))
            .build());
  }

  public List<T> listByField(String field, String value) {
    return repository.listByField(field, value);
  }

  public List<T> listAll() {
    return repository.listAll();
  }

  public List<T> listWithFilter(Filter filter) {
    return filter == null ? repository.listAll() : repository.listWithFilter(filter);
  }

  public List<T> listAllPaginated(int pageNum) {
    return repository.listAllPaginated(pageSize, pageNum - 1);
  }

  public List<T> listWithFilterPaginated(Filter filter, int pageNum) {
    return filter == null
        ? repository.listAllPaginated(pageSize, pageNum - 1)
        : repository.listWithFilterPaginated(filter, pageSize, pageNum - 1);
  }

  public long count() {
    return repository.count();
  }

  public long count(Filter filter) {
    return filter == null ? repository.count() : repository.count(filter);
  }

  public int getNumberPages() {
    final var numEntities = count();
    return (int) getNumberPages(numEntities);
  }

  public int getNumberPages(Filter filter) {
    final var numEntities = count(filter);
    return (int) getNumberPages(numEntities);
  }

  private long getNumberPages(long numEntities) {
    return numEntities % pageSize == 0 ? numEntities / pageSize : numEntities / pageSize + 1;
  }

  public void save(T entity) {
    repository.save(entity);
  }

  public void delete(T entity) {
    repository.delete(entity);
  }

  public <S extends BaseRequest> S getRequest(String id, S request, BiConsumer<S, T>... consumers)
      throws EntityNotFoundException {
    if (isEmpty(id)) {
      return request;
    }

    final var dbEntity = findById(id);
    BeanUtils.copyProperties(dbEntity, request);
    for (BiConsumer<S, T> consumer : consumers) {
      consumer.accept(request, dbEntity);
    }

    return request;
  }

  public <S extends BaseRequest> void save(
      String entityId, T entity, S request, Consumer<T>... consumers) {

    BeanUtils.copyProperties(request, entity);
    for (Consumer<T> consumer : consumers) {
      consumer.accept(entity);
    }

    if (entityId != null && !entityId.isEmpty()) {
      entity.setId(new ObjectId(entityId));
    }

    save(entity);
  }
}
