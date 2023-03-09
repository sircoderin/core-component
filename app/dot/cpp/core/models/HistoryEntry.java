package dot.cpp.core.models;

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
}
