package dot.cpp.core.models.user.request;

import dot.cpp.core.constants.Patterns;
import javax.validation.constraints.NotBlank;
import play.data.validation.Constraints.Pattern;
import play.libs.Json;

public class LoginRequest {

  @NotBlank(message = "constraints.field.not.empty")
  private String password;

  @NotBlank(message = "constraints.field.invalid")
  @Pattern(value = Patterns.NAME_OR_EMAIL, message = "constraints.field.invalid")
  private String username;

  @Override
  public String toString() {
    return Json.stringify(Json.toJson(this));
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username.trim();
  }
}
