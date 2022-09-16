package dot.cpp.core.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import play.libs.Json;
import play.mvc.QueryStringBindable;

public class BindableLocalDate implements QueryStringBindable<BindableLocalDate> {

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate date;

  /** Returns a BindableLocalDate from a LocalDate. */
  public static BindableLocalDate from(LocalDate date) {
    final var bindableLocalDate = new BindableLocalDate();
    bindableLocalDate.setDate(date);
    return bindableLocalDate;
  }

  /** Returns a BindableLocalDate from a String. */
  public static BindableLocalDate from(String strDate) {
    final var bindableLocalDate = new BindableLocalDate();
    bindableLocalDate.setDate(LocalDate.parse(strDate, DateTimeFormatter.ISO_DATE));
    return bindableLocalDate;
  }

  public static BindableLocalDate fromNow() {
    return BindableLocalDate.from(LocalDate.now());
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  @Override
  public Optional<BindableLocalDate> bind(String key, Map<String, String[]> data) {
    final var possibleDate = data.get(key);
    if (possibleDate == null) {
      return Optional.empty();
    }
    return Optional.of(BindableLocalDate.from(possibleDate[0]));
  }

  public String unbind(String key) {
    return String.format("%s=%s", key, date);
  }

  public String javascriptUnbind() {
    return Json.stringify(Json.toJson(date));
  }
}
