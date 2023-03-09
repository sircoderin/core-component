package dot.cpp.core.services;

import dot.cpp.core.exceptions.EntityNotFoundException;
import dot.cpp.core.helpers.ValidationHelper;
import dot.cpp.core.models.HistoryEntry;
import dot.cpp.core.models.user.entity.User;
import dot.cpp.repository.models.BaseEntity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
public class HistoryService {

  public static final String INVALID = "invalid";
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd-MM-yyyy");
  private final UserService userService;

  @Inject
  public HistoryService(UserService userService) {
    this.userService = userService;
  }

  public List<HistoryEntry> getHistoryEntriesById(EntityService<?, ?> entityService, String id)
      throws EntityNotFoundException {
    if (ValidationHelper.isEmpty(id)) {
      throw new EntityNotFoundException();
    }

    final var entity = entityService.findById(id);
    return getHistoryEntries(entityService, entity);
  }

  public List<HistoryEntry> getHistoryEntriesByTrackingId(
      EntityService<?, ?> entityService, String trackingId) throws EntityNotFoundException {
    if (ValidationHelper.isEmpty(trackingId)) {
      throw new EntityNotFoundException();
    }

    final var entity = entityService.findByField("trackingId", trackingId);
    return getHistoryEntries(entityService, entity);
  }

  @NotNull
  private List<HistoryEntry> getHistoryEntries(
      EntityService<?, ?> entityService, BaseEntity currentState) {
    final var historyEntries = new ArrayList<HistoryEntry>();
    final var historyStates = entityService.listHistory(currentState.getTrackingId());

    final var modifiedByUserIdSet =
        historyStates.stream().map(BaseEntity::getModifiedBy).collect(Collectors.toSet());
    modifiedByUserIdSet.add(currentState.getModifiedBy());

    final var users =
        userService.listByIds(new ArrayList<>(modifiedByUserIdSet)).stream()
            .collect(Collectors.toMap(User::getStrId, User::getUserName));

    // current entity is added first
    historyEntries.add(getHistoryEntry(users, currentState));
    historyStates.forEach(
        historyEntity -> historyEntries.add(getHistoryEntry(users, historyEntity)));

    return historyEntries;
  }

  @NotNull
  private HistoryEntry getHistoryEntry(Map<String, String> users, BaseEntity historyEntity) {
    return new HistoryEntry(
        users.getOrDefault(historyEntity.getModifiedBy(), INVALID),
        dateFormat.format(new Date(historyEntity.getModifiedAt() * 1000L)),
        historyEntity.getModifiedComment(),
        historyEntity.getStrId());
  }
}
