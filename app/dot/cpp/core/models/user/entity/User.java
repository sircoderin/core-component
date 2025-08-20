package dot.cpp.core.models.user.entity;

import dev.morphia.annotations.Entity;
import dot.cpp.core.constants.Patterns;
import dot.cpp.core.enums.UserRole;
import dot.cpp.repository.models.BaseEntity;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Entity("User")
public class User extends BaseEntity {

  @NotBlank private String userName;

  @NotBlank private String password = UUID.randomUUID().toString();

  @NotBlank private String fullName;

  @NotBlank private String idNumber;

  @NotNull private UserRole role;

  private List<String> groups;

  private boolean active;

  @NotNull
  @Pattern(regexp = Patterns.EMAIL, message = "constraints.field.invalid")
  private String email;

  @Pattern(regexp = Patterns.UUID, message = "constraints.field.invalid")
  private String resetPasswordUuid = UUID.randomUUID().toString();

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
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

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public List<String> getGroups() {
    return groups;
  }

  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getResetPasswordUuid() {
    return resetPasswordUuid;
  }

  public void setResetPasswordUuid(String resetPasswordUuid) {
    this.resetPasswordUuid = resetPasswordUuid;
  }
}
