package dot.cpp.core.exceptions;

import dot.cpp.core.enums.ErrorCodes;
import java.util.List;
import play.data.validation.ValidationError;

public class FormException extends BaseException {

  private final transient List<ValidationError> formErrors;

  public FormException(List<ValidationError> formErrors) {
    super(ErrorCodes.FORM_VALIDATION_FAILED.getCode());
    this.formErrors = formErrors;
  }

  public List<ValidationError> getFormErrors() {
    return formErrors;
  }
}
