package dot.cpp.core.models.user.request;

import dot.cpp.core.models.BaseRequest;
import javax.validation.constraints.NotBlank;

public class UserRequest extends BaseRequest {
  @NotBlank(message = "constraints.field.mandatory")
  private String username;

  @NotBlank(message = "constraints.field.mandatory")
  private String email;

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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
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
}
