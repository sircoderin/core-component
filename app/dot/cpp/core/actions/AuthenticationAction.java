package dot.cpp.core.actions;

import static dot.cpp.core.constants.Constants.ACCESS_TOKEN;
import static dot.cpp.core.constants.Constants.REFRESH_TOKEN;
import static dot.cpp.core.helpers.CookieHelper.getCookie;

import com.google.gson.JsonObject;
import com.typesafe.config.Config;
import dot.cpp.core.annotations.Authentication;
import dot.cpp.core.constants.Constants;
import dot.cpp.core.constants.Patterns;
import dot.cpp.core.enums.UserRole;
import dot.cpp.core.exceptions.BaseException;
import dot.cpp.core.exceptions.LoginException;
import dot.cpp.core.helpers.CookieHelper;
import dot.cpp.core.models.user.entity.User;
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
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;

public class AuthenticationAction extends Action<Authentication> {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final Object lock = new Object();

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

    synchronized (lock) {
      final var messages = languageService.preferred(request);
      final var accessToken = CookieHelper.getCookieString(request, ACCESS_TOKEN);
      final var refreshToken = CookieHelper.getCookieString(request, REFRESH_TOKEN);
      final var authHeader = request.header(Http.HeaderNames.AUTHORIZATION).orElse("");
      final var clientIp = request.header(Http.HeaderNames.X_FORWARDED_FOR).orElse("");

      logger.debug("Authentication");
      logger.debug("request: {}", request);
      logger.debug("authHeader: {}", authHeader);
      logger.debug("accessToken: {}", accessToken);
      logger.debug("refreshToken: {}", refreshToken);

      final var constructedAccessToken = constructToken(authHeader, accessToken);
      if (isEmpty(constructedAccessToken) || isInvalidJwt(constructedAccessToken)) {
        logger.warn("Token invalid {} for client with ip {}", constructedAccessToken, clientIp);
        return statusIfPresentOrResult(redirectWithError(messages));
      }
      logger.debug("{}", constructedAccessToken);

      try {
        final var user = loginService.authorizeRequest(accessToken, getConfigUserRoles());
        return delegate.call(request.addAttr(Constants.USER, user));
      } catch (LoginException loginEx) {
        logger.debug("{}", loginEx.getMessage());

        try {
          final var tokens = loginService.refreshTokens(refreshToken);
          final var user =
              loginService.authorizeRequest(
                  tokens.get(ACCESS_TOKEN).getAsString(), getConfigUserRoles());
          return getSuccessfulResult(request, user, tokens);
        } catch (BaseException exception) {
          return getCompletableFutureResultOnError(messages, exception);
        }
      }
    }
  }

  private List<UserRole> getConfigUserRoles() {
    return Arrays.stream(configuration.userRoles()).collect(Collectors.toList());
  }

  private CompletableFuture<Result> getCompletableFutureResultOnError(
      Messages messages, Exception ex) {
    logger.debug("", ex);
    return statusIfPresentOrResult(redirectWithError(messages));
  }

  private CompletionStage<Result> getSuccessfulResult(
      Request request, User user, JsonObject tokens) {
    final var secureCookie = config.getBoolean("play.http.session.secure");
    return delegate
        .call(request.addAttr(Constants.USER, user))
        .thenApply(
            result ->
                result.withCookies(
                    getCookie(ACCESS_TOKEN, tokens, secureCookie),
                    getCookie(REFRESH_TOKEN, tokens, secureCookie)));
  }

  private String constructToken(
      final String headerAuthorization, final String cookieAuthorization) {
    return isEmpty(headerAuthorization)
        ? cookieAuthorization
        : headerAuthorization.replace("Bearer", "").trim();
  }

  private boolean isInvalidJwt(String token) {
    return !token.matches(Patterns.JWT_TOKEN);
  }

  private CompletableFuture<Result> statusIfPresentOrResult(Result result) {
    final var status = configuration.status();
    if (status != -1) {
      return CompletableFuture.completedFuture(status(status));
    }
    return CompletableFuture.completedFuture(result);
  }

  private Result redirectWithError(Messages messages) {
    logger.debug("Session expired");
    return redirect(configuration.redirectUrl())
        .flashing("alert-danger", messages.apply("general.session.expired"));
  }

  private boolean isEmpty(String string) {
    return string == null || string.isBlank();
  }
}
