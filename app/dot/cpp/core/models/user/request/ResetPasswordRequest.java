package dot.cpp.core.models.user.request;

import dot.cpp.core.constants.Patterns;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import play.data.validation.Constraints.Validatable;
import play.data.validation.Constraints.Validate;
import play.libs.Json;

@Validate
public class ResetPasswordRequest implements Validatable<String> {

  @Pattern(regexp = Patterns.PASSWORD, message = "constraints.field.invalid")
  @Size(min = 1, message = "constraints.field.invalid")
  private String password;

  @Pattern(regexp = Patterns.PASSWORD, message = "constraints.field.invalid")
  @Size(min = 1, message = "constraints.field.invalid")
  private String confirmPassword;

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

  @Override
  public String validate() {
    if (!password.equals(confirmPassword)) {
      return "general.passwords.not.match";
    }
    return null;
  }

  @Override
  public String toString() {
    return Json.toJson(this).toString();
  }
}
