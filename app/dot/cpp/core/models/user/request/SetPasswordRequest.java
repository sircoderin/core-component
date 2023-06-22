package dot.cpp.core.models.user.request;

import dot.cpp.core.constants.Patterns;
import dot.cpp.core.models.BaseRequest;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class SetPasswordRequest extends BaseRequest {
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
}
