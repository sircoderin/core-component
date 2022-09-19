package dot.cpp.core.services;

import static play.mvc.Results.redirect;

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

  @Inject
  public RequestErrorService(MessagesApi messagesApi) {
    this.messagesApi = messagesApi;
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

  public Result handleErrorFromException(Http.Request request, Call call, Exception e) {
    logger.error("", e);
    return redirect(call).flashing(DANGER, messagesApi.preferred(request).apply(e.getMessage()));
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
