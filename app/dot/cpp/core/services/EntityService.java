package dot.cpp.core.services;

import dot.cpp.repository.models.BaseEntity;
import dot.cpp.repository.repository.BaseRepository;
import java.util.List;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityService<T extends BaseEntity> {
  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected final BaseRepository<T> repository;

  @Inject
  public EntityService(BaseRepository<T> repository) {
    this.repository = repository;
  }

  public T findById(Class<T> clazz, String id) {
    return repository.findById(clazz, id);
  }

  public T findByField(Class<T> clazz, String field, String value) {
    return repository.findByField(clazz, field, value);
  }

  public List<T> listByField(Class<T> clazz, String field, String value) {
    return repository.listByField(clazz, field, value);
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
