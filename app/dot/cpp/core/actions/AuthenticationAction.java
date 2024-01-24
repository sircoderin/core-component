package dot.cpp.core.actions;

import static dot.cpp.core.constants.Constants.SESSION_ID;
import static dot.cpp.core.helpers.ValidationHelper.isEmpty;

import dot.cpp.core.annotations.Authentication;
import dot.cpp.core.constants.Constants;
import dot.cpp.core.constants.Patterns;
import dot.cpp.core.enums.UserRole;
import dot.cpp.core.exceptions.BaseException;
import dot.cpp.core.exceptions.LoginException;
import dot.cpp.core.services.LoginService;
import dot.cpp.core.services.SessionService;
import dot.cpp.repository.services.RepositoryService;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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

  private static final Lock lock = new ReentrantLock(true);

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final MessagesApi languageService;
  private final LoginService loginService;
  private final RepositoryService repositoryService;
  private final SessionService sessionService;

  @Inject
  public AuthenticationAction(
      MessagesApi languageService,
      LoginService loginService,
      RepositoryService repositoryService,
      SessionService sessionService) {
    this.languageService = languageService;
    this.loginService = loginService;
    this.repositoryService = repositoryService;
    this.sessionService = sessionService;
  }

  public void setConfiguration(Authentication authenticationConfig) {
    this.configuration = authenticationConfig;
  }

  @Override
  public CompletionStage<Result> call(Request request) {

    logger.debug("request: {}", request);

    if (!repositoryService.isDatabaseInitialized()) {
      return delegate.call(request);
    }

    final var messages = languageService.preferred(request);
    final var sessionId = request.session().get(SESSION_ID).orElse("");

    try {
      if (!isEmpty(sessionId) && lock.tryLock(15, TimeUnit.SECONDS)) {
        final var tokens = sessionService.getTokens(sessionId);
        final var accessToken = tokens._1;
        final var refreshToken = tokens._2;
        logger.debug("refresh token after lock {}", refreshToken);

        final var authHeader = request.header(Http.HeaderNames.AUTHORIZATION).orElse("");
        final var clientIp = request.header(Http.HeaderNames.X_FORWARDED_FOR).orElse("");
        final var constructedAccessToken = constructToken(authHeader, accessToken);
        if (isEmpty(constructedAccessToken) || isInvalidJwt(constructedAccessToken)) {
          lock.unlock();

          logger.warn("Token invalid {} for client with ip {}", constructedAccessToken, clientIp);
          return getLogoutRedirect(messages);
        }

        return authorizeUser(request, sessionId, accessToken, refreshToken, messages)
            .whenComplete(
                (result, throwable) -> {
                  lock.unlock();
                  logger.debug("unlocked");
                });
      }
    } catch (InterruptedException e) {
      logger.error("", e);
      Thread.currentThread().interrupt();
    }

    return getLogoutRedirect(messages);
  }

  private CompletionStage<Result> authorizeUser(
      Request request,
      String sessionId,
      String accessToken,
      String refreshToken,
      Messages messages) {
    try {
      final var user = loginService.authorizeRequest(accessToken, getConfigUserRoles());
      return delegate.call(request.addAttr(Constants.USER, user));
    } catch (LoginException loginEx) {
      try {
        final var tokens = loginService.refreshTokens(refreshToken);
        final var newAccessToken = tokens._1;
        final var newRefreshToken = tokens._2;
        logger.info("new tokens {}", tokens);

        final var user = loginService.authorizeRequest(newAccessToken, getConfigUserRoles());
        sessionService.addTokensToCache(sessionId, newAccessToken, newRefreshToken);

        return delegate.call(request.addAttr(Constants.USER, user));
      } catch (BaseException exception) {
        logger.debug("", exception);
        return getLogoutRedirect(messages);
      }
    }
  }

  private List<UserRole> getConfigUserRoles() {
    return Arrays.stream(configuration.userRoles()).collect(Collectors.toList());
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

  private CompletableFuture<Result> getLogoutRedirect(Messages messages) {
    final var status = configuration.status();
    if (status != -1) {
      return CompletableFuture.completedFuture(status(status));
    }

    logger.debug("Session expired");
    return CompletableFuture.completedFuture(
        redirect(configuration.redirectUrl())
            .flashing("alert-danger", messages.apply("general.session.expired"))
            .withNewSession());
  }
}
