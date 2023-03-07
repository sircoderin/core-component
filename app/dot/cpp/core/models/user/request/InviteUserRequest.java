package dot.cpp.core.models.user.request;

import dot.cpp.core.constants.Patterns;
import dot.cpp.core.enums.UserRole;
import dot.cpp.core.interfaces.BaseRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import play.data.validation.Constraints.Validatable;
import play.data.validation.Constraints.Validate;
import play.libs.Json;

@Validate
public class InviteUserRequest implements BaseRequest, Validatable<String> {

  @Pattern(regexp = Patterns.EMAIL, message = "constraints.field.invalid")
  private String email;

  @NotNull private UserRole userRole;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public UserRole getUserRole() {
    return userRole;
  }

  public void setUserRole(UserRole userRole) {
    this.userRole = userRole;
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
