package dot.cpp.core.services;

import static dot.cpp.core.constants.Constants.USER_ROLE;
import static dot.cpp.core.helpers.ValidationHelper.isEmpty;

import dot.cpp.core.enums.ErrorCodes;
import dot.cpp.core.enums.UserRole;
import dot.cpp.core.exceptions.LoginException;
import dot.cpp.core.models.AuthTokens;
import dot.cpp.core.models.session.entity.Session;
import dot.cpp.core.models.session.repository.SessionRepository;
import dot.cpp.core.models.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import javax.crypto.SecretKey;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Environment;
import play.mvc.Http;

@Singleton
public class LoginService {

  public static final long REFRESH_TIME = 86400000L; // one day in milliseconds
  public static final long ACCESS_TIME = 1800000L; // 30 minutes in milliseconds

  public final UserService userService;
  private final SecretKey key;
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final ReentrantLock[] locks = new ReentrantLock[10];
  private final UserRepository userRepository;
  private final SessionRepository sessionRepository;
  private final Environment environment;

  @Inject
  public LoginService(
      UserService userService,
      UserRepository userRepository,
      SessionRepository sessionRepository,
      Environment environment) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.sessionRepository = sessionRepository;
    this.environment = environment;

    Arrays.setAll(locks, index -> new ReentrantLock());
    key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
  }

  /**
   * Login user and return access and refresh tokens.
   *
   * @param request the HTTP request
   * @param username the username of the user
   * @param password the password of the user
   * @return a Tokens object containing the access and refresh tokens
   * @throws LoginException if the login is unsuccessful
   */
  public AuthTokens login(Http.Request request, String username, String password)
      throws LoginException {
    final var clientIp = getClientIp(request);
    final var user = userRepository.findByField("userName", username);

    if (user == null) {
      logger.debug("Username not found {}", username);
      throw LoginException.from(ErrorCodes.USER_NOT_FOUND);
    } else {
      if (!userService.passwordIsValid(user.getPassword(), password)) {
        logger.debug("Wrong password for username {}", username);
        throw LoginException.from(ErrorCodes.INCORRECT_PASSWORD);
      }

      if (!user.isActive()) {
        logger.debug("Inactive user account {}", username);
        throw LoginException.from(ErrorCodes.USER_INACTIVE_ACCOUNT);
      }

      final var expirationDateRefresh = new Date();
      expirationDateRefresh.setTime(expirationDateRefresh.getTime() + REFRESH_TIME);

      final var session = new Session();
      final var refreshToken = UUID.randomUUID().toString();
      session.setRefreshToken(refreshToken);
      session.setRefreshExpiryTime(expirationDateRefresh.getTime());
      session.setUserId(user.getRecordId());
      session.setClientIp(clientIp);
      sessionRepository.save(session);

      return getAuthTokens(session, user.getRole());
    }
  }

  public String getClientIp(Http.Request request) throws LoginException {
    final String clientIp;

    if (environment.isDev()) {
      clientIp = request.remoteAddress();

      if (isEmpty(clientIp)) {
        throw LoginException.from(ErrorCodes.IP_NOT_FOUND);
      }
    } else {
      clientIp =
          request
              .header(Http.HeaderNames.X_FORWARDED_FOR)
              .orElseThrow(() -> LoginException.from(ErrorCodes.IP_NOT_FOUND));
    }

    return clientIp;
  }

  private String getAccessToken(String userId, UserRole userRole) {
    final var expirationDateAccess = new Date();
    expirationDateAccess.setTime(expirationDateAccess.getTime() + ACCESS_TIME);

    return Jwts.builder()
        .setSubject(userId)
        .setExpiration(expirationDateAccess)
        .signWith(key)
        .claim(USER_ROLE, userRole)
        .compact();
  }

  private Jws<Claims> getJwsClaims(String jwtToken) throws LoginException {
    try {
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtToken);
    } catch (Exception e) {
      // ! Arc Browser sends another redundant request without cookies which gets logged - IGNORE
      logger.debug("JWT error {}", e.getMessage());
      throw LoginException.from(ErrorCodes.INVALID_JWT);
    }
  }

  /**
   * Authorizes a request using a JWT access token. Verifies token validity and expiration, and
   * user's role.
   *
   * @param accessToken the JWT access token
   * @param permittedUserRoles a list of authorized user roles; if it's empty, all users are allowed
   * @return the user ID associated with the validated JWT access token
   * @throws LoginException If the access token is invalid or expired, or if the user's role does
   *     not match the permitted roles, a LoginException is thrown.
   */
  public String authorizeRequest(String accessToken, List<UserRole> permittedUserRoles)
      throws LoginException {
    final var claims = getJwsClaims(accessToken).getBody();
    final var expirationDate = claims.getExpiration();

    if (expirationDate.before(new Date())) {
      throw LoginException.from(ErrorCodes.EXPIRED_ACCESS);
    }

    final var userRole = claims.get(USER_ROLE, String.class);
    if (!permittedUserRoles.isEmpty() && !permittedUserRoles.contains(UserRole.valueOf(userRole))) {
      throw LoginException.from(ErrorCodes.USER_ROLE_MISMATCH);
    }

    return claims.getSubject(); // userId
  }

  /**
   * Refreshes the access and refresh tokens for a user.
   *
   * @param refreshToken the refresh token of the user
   * @param clientIp the IP of the client
   * @return a Tokens object containing the new access and refresh tokens
   * @throws LoginException if the refresh token is invalid or expired
   */
  public AuthTokens refreshTokens(String refreshToken, String clientIp) throws LoginException {
    final var lockIndex = Math.abs(refreshToken.hashCode() % 10);

    locks[lockIndex].lock();
    try {
      final var session = sessionRepository.findByField("refreshToken", refreshToken);
      return session != null
          ? refreshTokens(refreshToken, clientIp, session)
          : tryRecentlyRefreshedSession(refreshToken, clientIp);
    } finally {
      locks[lockIndex].unlock();
    }
  }

  private AuthTokens refreshTokens(String refreshToken, String clientIp, Session session)
      throws LoginException {
    validateSessionIp(clientIp, session);

    if (session.getRefreshExpiryTime() < new Date().getTime()) {
      throw LoginException.from(ErrorCodes.EXPIRED_REFRESH);
    }

    final var expirationDateRefresh = new Date();
    expirationDateRefresh.setTime(expirationDateRefresh.getTime() + REFRESH_TIME);
    final var newRefreshToken = UUID.randomUUID().toString();

    session.setRefreshExpiryTime(expirationDateRefresh.getTime());
    session.setRefreshToken(newRefreshToken);
    session.setOldRefreshToken(refreshToken);
    sessionRepository.save(session);

    return getAuthTokens(session);
  }

  private AuthTokens tryRecentlyRefreshedSession(String refreshToken, String clientIp)
      throws LoginException {
    final var refreshedSession = sessionRepository.findByField("oldRefreshToken", refreshToken);

    if (refreshedSession != null) {
      validateSessionIp(clientIp, refreshedSession);

      if (refreshedSession.getRefreshExpiryTime() > new Date().getTime() + REFRESH_TIME - 10000L) {
        return getAuthTokens(refreshedSession);
      }
    }

    throw LoginException.from(ErrorCodes.SESSION_NOT_FOUND);
  }

  private void validateSessionIp(String clientIp, Session session) throws LoginException {
    if (!session.getClientIp().equals(clientIp)) {
      logger.error("Client IP {} different from session IP {}", clientIp, session.getClientIp());
      throw LoginException.from(ErrorCodes.IP_INVALID);
    }
  }

  private AuthTokens getAuthTokens(Session session) throws LoginException {
    final var user = userRepository.findById(session.getUserId());

    if (user == null) {
      logger.error("User not found {}", session.getUserId());
      throw LoginException.from(ErrorCodes.USER_NOT_FOUND);
    }

    return getAuthTokens(session, user.getRole());
  }

  private AuthTokens getAuthTokens(Session session, UserRole userRole) {
    final var accessToken = getAccessToken(session.getUserId(), userRole);
    return new AuthTokens(accessToken, session.getRefreshToken());
  }

  public void logout(String userId) throws LoginException {
    final var session = sessionRepository.findByField("userId", userId);
    if (session == null) {
      throw LoginException.from(ErrorCodes.SESSION_NOT_FOUND);
    }

    sessionRepository.delete(session);
  }
}
