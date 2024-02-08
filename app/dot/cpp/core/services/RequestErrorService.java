package dot.cpp.core.services;

import static play.mvc.Results.redirect;

import com.typesafe.config.Config;
import dot.cpp.core.exceptions.BaseException;
import dot.cpp.core.exceptions.FormException;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Call;
import play.mvc.Http;
import play.mvc.Result;

@Singleton
public final class RequestErrorService {

  public static final String DANGER = "alert-danger";

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final MessagesApi messagesApi;
  private final boolean logFormExceptions;

  @Inject
  public RequestErrorService(MessagesApi messagesApi, Config config) {
    this.messagesApi = messagesApi;
    this.logFormExceptions = config.getBoolean("log.form.exceptions");
  }

  /**
   * Handle generic errors.
   *
   * @param call Call
   * @param request Request
   */
  public Result handleGenericErrors(Call call, Http.Request request) {
    var messages = messagesApi.preferred(request);
    return getResult(call, messages.apply("general.session.expired"));
  }

  /**
   * Handle form errors.
   *
   * @param call Call
   * @param request Request
   * @param webForm Form
   */
  public Result handleFormErrors(Call call, Http.Request request, Form<?> webForm) {
    var messages = messagesApi.preferred(request);
    return getResult(call, getErrorMessage(webForm.errors(), messages));
  }

  /**
   * Handle form errors staying on the same page.
   *
   * @param request Request
   * @param webForm Form
   */
  public Result handleFormErrorWithRefresh(Http.Request request, Form<?> webForm) {
    var messages = messagesApi.preferred(request);
    return redirect(request.uri()).flashing(DANGER, getErrorMessage(webForm.errors(), messages));
  }

  public Result handleExceptionErrors(Http.Request request, Call call, Exception e) {
    final String errorMessage;

    if (e instanceof FormException) {
      if (logFormExceptions) {
        logger.error("", e);
      }

      errorMessage =
          getErrorMessage(((FormException) e).getFormErrors(), messagesApi.preferred(request));
    } else if (e instanceof BaseException) {
      final var errorCode = ((BaseException) e).getErrorCode();
      errorMessage = messagesApi.preferred(request).apply(errorCode.getMessage());
      errorCode.setDetails(errorMessage);

      logger.error("errorResponse {}", errorCode);
      logger.error("", e);
    } else {
      logger.error("", e);
      errorMessage = messagesApi.preferred(request).apply(e.getMessage());
    }

    return redirect(call).flashing(DANGER, errorMessage);
  }

  private String getErrorMessage(List<ValidationError> validationErrors, Messages messages) {
    return validationErrors.stream()
        .map(
            validationError -> {
              final var message = messages.apply(validationError.message());
              return validationError.key().isEmpty()
                  ? message
                  : validationError.key() + ": " + message;
            })
        .collect(Collectors.joining("; "));
  }

  private Result getResult(Call call, String message) {
    return redirect(call).flashing(DANGER, message);
  }
}
