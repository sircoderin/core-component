package dot.cpp.core.models.user.request;

import dot.cpp.core.constants.Patterns;
import dot.cpp.core.models.BaseRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import play.data.validation.Constraints;
import play.libs.Json;

@Constraints.Validate
public class UserRequest extends BaseRequest implements Constraints.Validatable<String>{
  @NotBlank(message = "constraints.field.mandatory")
  private String username;

  @Pattern(regexp = Patterns.PASSWORD, message = "constraints.field.invalid")
  @Size(min = 1, message = "constraints.field.invalid")
  private String password;

  @Pattern(regexp = Patterns.PASSWORD, message = "constraints.field.invalid")
  @Size(min = 1, message = "constraints.field.invalid")
  private String confirmPassword;

  @NotBlank(message = "constraints.field.mandatory")
  private String fullName;

  @NotBlank(message = "constraints.field.mandatory")
  private String idNumber;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

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

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getIdNumber() {
    return idNumber;
  }

  public void setIdNumber(String idNumber) {
    this.idNumber = idNumber;
  }

  @Override
  public String validate() {
    if (!password.equals(confirmPassword)) {
      return "general.passwords.not.match";
    }
    return null;
  }
}
