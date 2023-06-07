package dot.cpp.core.models.user.request;

import dot.cpp.core.constants.Patterns;
import javax.validation.constraints.NotBlank;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Validatable;
import play.data.validation.Constraints.Validate;
import play.libs.Json;

@Validate
public class AcceptInviteRequest implements Validatable<String> {

  @NotBlank private String username;

  @Pattern(value = Patterns.PASSWORD, message = "constraints.field.invalid")
  @Constraints.MinLength(value = 1, message = "constraints.field.invalid")
  private String password;

  @Pattern(value = Patterns.PASSWORD, message = "constraints.field.invalid")
  @Constraints.MinLength(value = 1, message = "constraints.field.invalid")
  private String confirmPassword;

  @NotBlank private String fullName;

  @NotBlank private String documentId;

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

  public String getDocumentId() {
    return documentId;
  }

  public void setDocumentId(String documentId) {
    this.documentId = documentId;
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
