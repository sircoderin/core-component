package dot.cpp.core.exceptions;

import java.util.List;
import play.data.validation.ValidationError;

public class FormException extends BaseException {

  private final transient List<ValidationError> formErrors;

  public FormException(List<ValidationError> formErrors) {
    super("form.validation.errors");
    this.formErrors = formErrors;
  }

  public List<ValidationError> getFormErrors() {
    return formErrors;
  }
}
