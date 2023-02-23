package dot.cpp.core.models;

public class HistoryEntry {

  public final String userName;

  public final Long time;

  public final String comment;

  public final String id;

  public HistoryEntry(String userName, Long time, String comment, String id) {
    this.userName = userName;
    this.time = time;
    this.comment = comment;
    this.id = id;
  }
}
