package dot.cpp.core.models.session.repository;

import dot.cpp.core.models.session.entity.Session;
import dot.cpp.repository.repository.BaseRepository;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SessionRepository extends BaseRepository<Session> {

  @Inject
  public SessionRepository() {}
}
