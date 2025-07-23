package dot.cpp.core.actions;

import static dot.cpp.core.constants.Constants.ACCESS_TOKEN;
import static dot.cpp.core.constants.Constants.REFRESH_TOKEN;
import static dot.cpp.core.helpers.CookieHelper.getCookie;
import static dot.cpp.core.helpers.ValidationHelper.isEmpty;
import static dot.cpp.core.services.LoginService.ACCESS_TIME;
import static dot.cpp.core.services.LoginService.REFRESH_TIME;

import com.typesafe.config.Config;
import dot.cpp.core.annotations.Authentication;
import dot.cpp.core.constants.Constants;
import dot.cpp.core.enums.ErrorCodes;
import dot.cpp.core.enums.UserRole;
import dot.cpp.core.exceptions.LoginException;
import dot.cpp.core.helpers.CookieHelper;
import dot.cpp.core.models.AuthTokens;
import dot.cpp.core.services.LoginService;
import dot.cpp.repository.services.RepositoryService;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Action;
import play.mvc.Http.Request;
import play.mvc.Result;

public class AuthenticationAction extends Action<Authentication> {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final MessagesApi languageService;
  private final LoginService loginService;
  private final RepositoryService repositoryService;
  private final Config config;

  @Inject
  public AuthenticationAction(
      MessagesApi languageService,
      LoginService loginService,
      RepositoryService repositoryService,
      Config config) {
    this.languageService = languageService;
    this.loginService = loginService;
    this.repositoryService = repositoryService;
    this.config = config;
  }

  public void setConfiguration(Authentication authenticationConfig) {
    this.configuration = authenticationConfig;
  }

  @Override
  public CompletionStage<Result> call(Request request) {
    if (!repositoryService.isDatabaseInitialized()) {
      return delegate.call(request);
    }

    final var messages = languageService.preferred(request);
    final var accessToken = CookieHelper.getCookieString(request, ACCESS_TOKEN);
    final var refreshToken = CookieHelper.getCookieString(request, REFRESH_TOKEN);

    try {
      final var userId = loginService.authorizeRequest(accessToken, getConfigUserRoles());
      return delegate.call(request.addAttr(Constants.USER_ID, userId));
    } catch (LoginException loginException) {
      logger.debug("Authorize request exception {}", loginException.getMessage());

      try {
        if (isEmpty(refreshToken)) {
          throw LoginException.from(ErrorCodes.MISSING_REFRESH_TOKEN);
        }

        final var clientIp = loginService.getClientIp(request);
        final var tokens = loginService.refreshTokens(refreshToken, clientIp);
        final var userId = loginService.authorizeRequest(tokens.accessToken, getConfigUserRoles());
        logger.debug("Successfully refreshed tokens for user {}", userId);

        return getSuccessfulResult(request, userId, tokens);
      } catch (LoginException refreshException) {
        logger.debug("Refresh exception {}", refreshException.getMessage());
        return getLogoutRedirect(messages);
      }
    }
  }

  private List<UserRole> getConfigUserRoles() {
    return Arrays.stream(configuration.userRoles()).toList();
  }

  private CompletionStage<Result> getSuccessfulResult(
      Request request, String userId, AuthTokens authTokens) {
    final var isSecure = config.getBoolean("play.http.session.secure");
    return delegate
        .call(request.addAttr(Constants.USER_ID, userId))
        .thenApply(
            result ->
                result.withCookies(
                    getCookie(ACCESS_TOKEN, authTokens.accessToken, isSecure, ACCESS_TIME),
                    getCookie(REFRESH_TOKEN, authTokens.refreshToken, isSecure, REFRESH_TIME)));
  }

  private CompletableFuture<Result> getLogoutRedirect(Messages messages) {
    final var status = configuration.status();
    if (status != -1) {
      return CompletableFuture.completedFuture(status(status));
    }

    return CompletableFuture.completedFuture(
        CookieHelper.discardAuthorizationCookies(redirect(configuration.redirectUrl()))
            .flashing("alert-danger", messages.apply("general.session.expired")));
  }
}
