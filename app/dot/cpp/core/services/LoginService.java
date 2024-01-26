package dot.cpp.core.services;

import dot.cpp.core.enums.ErrorCodes;
import dot.cpp.core.enums.UserRole;
import dot.cpp.core.exceptions.BaseException;
import dot.cpp.core.exceptions.LoginException;
import dot.cpp.core.models.Tokens;
import dot.cpp.core.models.session.entity.Session;
import dot.cpp.core.models.session.repository.SessionRepository;
import dot.cpp.core.models.user.entity.User;
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

@Singleton
public class LoginService {

  public static final long REFRESH_TIME = 86400000L; // one day in milliseconds
  public static final long ACCESS_TIME = 600000L; // 10 minutes in milliseconds

  public final UserService userService;
  private final SecretKey key;
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final ReentrantLock[] locks = new ReentrantLock[10];
  private final UserRepository userRepository;
  private final SessionRepository sessionRepository;

  @Inject
  public LoginService(
      UserService userService, UserRepository userRepository, SessionRepository sessionRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.sessionRepository = sessionRepository;

    Arrays.setAll(locks, index -> new ReentrantLock());
    key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
  }

  /**
   * Login user and return access and refresh tokens.
   *
   * @param username the username of the user
   * @param password the password of the user
   * @return a Tokens object containing the access and refresh tokens
   * @throws LoginException if the login is unsuccessful
   */
  public Tokens login(String username, String password) throws LoginException {
    final var user = userRepository.findByField("userName", username);

    if (user == null) {
      logger.debug("User not found {}", username);
      throw LoginException.from(ErrorCodes.USER_NOT_FOUND);
    } else {
      if (!userService.passwordIsValid(user.getPassword(), password)) {
        logger.debug("Failed authentication by {}", username);
        throw LoginException.from(ErrorCodes.INCORRECT_PASSWORD);
      }

      final var expirationDateRefresh = new Date();
      expirationDateRefresh.setTime(expirationDateRefresh.getTime() + REFRESH_TIME);

      final var session = new Session();
      final var refreshToken = UUID.randomUUID().toString();
      session.setRefreshToken(refreshToken);
      session.setRefreshExpiryTime(expirationDateRefresh.getTime());
      session.setCreateTime(Instant.now().toEpochMilli());
      session.setUserId(user.getRecordId());
      sessionRepository.save(session);
      logger.debug("{}", session);

      return getTokens(session);
    }
  }

  private String getAccessToken(String userId) {
    final var expirationDateAccess = new Date();
    expirationDateAccess.setTime(expirationDateAccess.getTime() + ACCESS_TIME);

    String jws =
        Jwts.builder()
            .setSubject(userId)
            .setExpiration(expirationDateAccess)
            .signWith(key)
            .compact();

    logger.debug("{}", jws);

    return jws;
  }

  public String checkJwtAndGetUserId(String jwtToken) throws LoginException {
    logger.debug("{}", jwtToken);

    final var claims = getJwsClaims(jwtToken).getBody();
    final var expirationDate = claims.getExpiration();

    if (expirationDate.before(new Date())) {
      throw LoginException.from(ErrorCodes.EXPIRED_ACCESS);
    }

    return claims.getSubject();
  }

  private Jws<Claims> getJwsClaims(String jwtToken) throws LoginException {
    try {
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtToken);
    } catch (Exception e) {
      logger.debug("JWT error {}", e.getMessage());
      throw LoginException.from(ErrorCodes.INVALID_JWT);
    }
  }

  public User authorizeRequest(String accessToken, List<UserRole> permittedUserRoles)
      throws LoginException {
    final String userId = checkJwtAndGetUserId(accessToken);
    try {
      final var user = userService.findById(userId);
      logger.debug("{}", user);

      if (!user.isActive()) {
        throw LoginException.from(ErrorCodes.USER_INACTIVE_ACCOUNT);
      }
      if (!permittedUserRoles.isEmpty() && !permittedUserRoles.contains(user.getRole())) {
        throw LoginException.from(ErrorCodes.USER_ROLE_MISMATCH);
      }

      return user;
    } catch (BaseException e) {
      throw LoginException.from(ErrorCodes.USER_NOT_FOUND);
    }
  }

  /**
   * Refreshes the access and refresh tokens for a user.
   *
   * @param refreshToken the refresh token of the user
   * @return a Tokens object containing the new access and refresh tokens
   * @throws LoginException if the refresh token is invalid or expired
   */
  public Tokens refreshTokens(String refreshToken) throws LoginException {
    final var lockIndex = Math.abs(refreshToken.hashCode() % 10);

    locks[lockIndex].lock();
    try {
      final Session session = sessionRepository.findByField("refreshToken", refreshToken);

      if (session == null) {
        final var refreshedSession = sessionRepository.findByField("oldRefreshToken", refreshToken);

        if (refreshedSession.getRefreshExpiryTime()
            > new Date().getTime() + REFRESH_TIME - 10000L) {
          return getTokens(refreshedSession);
        }

        throw LoginException.from(ErrorCodes.SESSION_NOT_FOUND);
      }

      logger.debug("before refresh {}", session);

      if (session.getRefreshExpiryTime() < new Date().getTime()) {
        throw LoginException.from(ErrorCodes.EXPIRED_REFRESH);
      }

      Date expirationDateRefresh = new Date();
      expirationDateRefresh.setTime(expirationDateRefresh.getTime() + REFRESH_TIME);
      final String newRefreshToken = UUID.randomUUID().toString();

      session.setRefreshExpiryTime(expirationDateRefresh.getTime());
      session.setRefreshToken(newRefreshToken);
      session.setOldRefreshToken(refreshToken);
      sessionRepository.save(session);

      logger.debug("after refresh {}", session);

      return getTokens(session);
    } finally {
      locks[lockIndex].unlock();
    }
  }

  private Tokens getTokens(Session session) {
    final var accessToken = getAccessToken(session.getUserId());
    final var tokens = new Tokens(accessToken, session.getRefreshToken());
    logger.debug("refreshed tokens {}", tokens);
    return tokens;
  }

  public void logout(String userId) throws LoginException {
    final var session = sessionRepository.findByField("userId", userId);
    if (session == null) {
      throw LoginException.from(ErrorCodes.SESSION_NOT_FOUND);
    }

    sessionRepository.delete(session);
  }
}
