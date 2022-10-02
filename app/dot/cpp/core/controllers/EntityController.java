package dot.cpp.core.controllers;

import com.typesafe.config.ConfigFactory;
import dot.cpp.core.services.RequestErrorService;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Call;
import play.mvc.Controller;

public class EntityController extends Controller {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @Inject protected FormFactory formFactory;
  @Inject protected MessagesApi messagesApi;
  @Inject protected RequestErrorService requestErrorService;

  public play.mvc.Result getSuccessfulRedirect(Call call) {
    return redirect(call)
        .flashing("alert-success", ConfigFactory.load().getString("action.success"));
  }
}
