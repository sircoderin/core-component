package dot.cpp.core.models;

import dot.cpp.repository.models.BaseEntity;

public class HistoryEntry {

  public final String userName;

  public final String dateTime;

  public final String comment;

  public final String id;

  public HistoryEntry(String userName, String dateTime, String comment, String id) {
    this.userName = userName;
    this.dateTime = dateTime;
    this.comment = comment;
    this.id = id;
  }

  public static HistoryEntry fromBaseEntity(BaseEntity entity) {
    return new HistoryEntry(
        entity.getModifiedBy(),
        entity.getModifiedAt().toString(),
        entity.getModifiedComment(),
        entity.getStrId());
  }
}
