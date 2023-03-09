package dot.cpp.core.exceptions;

import java.util.List;
import play.data.validation.ValidationError;

public class FormException extends Exception {

  private final transient List<ValidationError> formErrors;

  public FormException(List<ValidationError> formErrors) {
    this.formErrors = formErrors;
  }

  public List<ValidationError> getFormErrors() {
    return formErrors;
  }
}
