package dot.cpp.core.models;

public class HistoryEntry {

  public final String username;

  public final Long timestamp;

  public final String comment;

  public HistoryEntry(String username, Long timestamp, String comment) {
    this.username = username;
    this.timestamp = timestamp;
    this.comment = comment;
  }
}
