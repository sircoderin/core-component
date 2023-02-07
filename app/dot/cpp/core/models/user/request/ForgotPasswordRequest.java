package dot.cpp.core.models.user.request;

import dot.cpp.core.constants.Patterns;
import javax.validation.constraints.Pattern;
import play.data.validation.Constraints.Validatable;
import play.data.validation.Constraints.Validate;
import play.libs.Json;

@Validate
public class ForgotPasswordRequest implements Validatable<String> {

  @Pattern(regexp = Patterns.EMAIL, message = "constraints.field.invalid")
  private String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String validate() {
    return null;
  }

  @Override
  public String toString() {
    return Json.stringify(Json.toJson(this));
  }
}
