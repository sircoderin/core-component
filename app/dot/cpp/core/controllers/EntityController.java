package dot.cpp.core.controllers;

import com.typesafe.config.ConfigFactory;
import dot.cpp.core.constants.Constants;
import dot.cpp.core.exceptions.FormException;
import dot.cpp.core.models.BaseRequest;
import dot.cpp.core.services.HistoryService;
import dot.cpp.core.services.RequestErrorService;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Http;

public class EntityController extends Controller {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @Inject protected FormFactory formFactory;
  @Inject protected MessagesApi messagesApi;
  @Inject protected RequestErrorService requestErrorService;
  @Inject protected HistoryService historyService;

  public play.mvc.Result getSuccessfulRedirect(Call call) {
    return redirect(call)
        .flashing("alert-success", ConfigFactory.load().getString("action.success"));
  }

  protected <T extends BaseRequest> T getRequest(Class<T> clazz, Http.Request request)
      throws FormException {
    final var userId = request.attrs().get(Constants.USER).getStrId();
    final var form = formFactory.form(clazz).bindFromRequest(request);

    if (form.hasErrors()) {
      throw new FormException(form.errors());
    }

    final var formRequest = form.get();
    formRequest.setUserId(userId);
    return formRequest;
  }
}
