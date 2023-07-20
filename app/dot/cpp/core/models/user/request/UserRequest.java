package dot.cpp.core.models.user.request;

import dot.cpp.core.enums.UserRole;
import dot.cpp.core.models.BaseRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class UserRequest extends BaseRequest {

  @NotNull(message = "constraints.field.mandatory")
  private UserRole role;

  @NotBlank(message = "constraints.field.mandatory")
  private String userName;

  @NotBlank(message = "constraints.field.mandatory")
  private String email;

  @NotBlank(message = "constraints.field.mandatory")
  private String fullName;

  @NotBlank(message = "constraints.field.mandatory")
  private String idNumber;

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
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
