package dot.cpp.core.services;

import com.typesafe.config.Config;
import dot.cpp.repository.models.BaseEntity;
import dev.morphia.query.experimental.filters.Filter;
import dot.cpp.repository.repository.BaseRepository;
import java.util.List;

public class EntityService<T extends BaseEntity> {
  private final BaseRepository<T> repository;
  private final int pageSize;

  public EntityService(BaseRepository<T> repository, Config config) {
    this.repository = repository;
    this.pageSize = config.getInt("list.page.size");
  }

  public T findById(String id) {
    return repository.findById(id);
  }

  public T findByField(String field, String value) {
    return repository.findByField(field, value);
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
    return filter == null ? repository.listAllPaginated(pageSize, pageNum - 1) : repository.listWithFilterPaginated(filter, pageSize, pageNum - 1);
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
    //TODO exceptions
  }

  public void delete(T entity) {
    repository.delete(entity);
    //TODO exceptions
  }
}
