package dot.cpp.core.services;

import com.password4j.Argon2Function;
import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.types.Argon2;
import com.typesafe.config.Config;
import dot.cpp.core.enums.Error;
import dot.cpp.core.enums.UserRole;
import dot.cpp.core.enums.UserStatus;
import dot.cpp.core.exceptions.EntityNotFoundException;
import dot.cpp.core.exceptions.UserException;
import dot.cpp.core.models.user.entity.User;
import dot.cpp.core.models.user.repository.UserRepository;
import dot.cpp.core.models.user.request.AcceptInviteRequest;
import dot.cpp.core.models.user.request.InviteUserRequest;
import dot.cpp.core.models.user.request.ResetPasswordRequest;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class UserService extends EntityService<User, InviteUserRequest> {

  private static final String TEMPORARY = "temporary";
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final String passwordPepper;
  private final Argon2Function argon2 = Argon2Function.getInstance(1000, 4, 2, 32, Argon2.ID, 19);

  @Inject
  public UserService(UserRepository userRepository, Config config) {
    super(userRepository, config);
    this.passwordPepper = config.getString("password.pepper");
  }

  public String generateUserWithInvitation(String email, UserRole userRole) {
    final var user = new User();
    final var resetPasswordUuid = UUID.randomUUID().toString();

    user.setEmail(email);
    user.setRole(userRole);
    user.setUserName(TEMPORARY);
    user.setPassword(TEMPORARY);
    user.setStatus(UserStatus.INACTIVE);
    user.setResetPasswordUuid(resetPasswordUuid);
    user.setFullName(TEMPORARY);
    user.setDocumentId(TEMPORARY);

    save(user);

    return resetPasswordUuid;
  }

  public String generateResetPasswordUuid(String email) throws UserException {

    try {
      final User user = findByField("email", email);
      if (!user.isActive()) {
        throw new UserException(Error.ACCOUNT_INACTIVE);
      }

      final String resetPasswordUuid = UUID.randomUUID().toString();
      user.setResetPasswordUuid(resetPasswordUuid);
      save(user);

      logger.debug("{}", user);
      return resetPasswordUuid;
    } catch (EntityNotFoundException e) {
      throw new UserException(Error.USER_EMAIL_NOT_FOUND);
    }
  }

  public User resetPassword(ResetPasswordRequest resetPasswordRequest, String resetPasswordUuid)
      throws EntityNotFoundException {
    logger.debug("{}", resetPasswordRequest);
    logger.debug("{}", resetPasswordUuid);

    final var user = findByField("resetPasswordUuid", resetPasswordUuid);

    final Hash hashedPassword = getHashedPassword(resetPasswordRequest.getPassword());
    logger.debug("{}", hashedPassword);
    user.setPassword(hashedPassword.getResult());
    user.setResetPasswordUuid("");

    logger.debug("{}", user);
    save(user);
    return user;
  }

  public boolean checkPassword(String hashedPassword, String password) {
    boolean verified =
        Password.check(password, hashedPassword).addPepper(passwordPepper).with(argon2);
    logger.debug("verified {}", verified);
    return verified;
  }

  public User userIsActiveAndHasRole(String userId, List<UserRole> userRoles)
      throws UserException, EntityNotFoundException {

    final var user = findById(userId);
    logger.debug("{}", user);

    if (!user.isActive()) {
      throw new UserException(Error.ACCOUNT_INACTIVE);
    }
    if (!userRoles.isEmpty() && !userRoles.contains(user.getRole())) {
      throw new UserException(Error.USER_ROLE_MISMATCH);
    }

    return user;
  }

  public User acceptInvitation(AcceptInviteRequest request, String resetPasswordUuid)
      throws EntityNotFoundException {
    logger.debug("{}\n{}", request, resetPasswordUuid);

    final var user = findByField("resetPasswordUuid", resetPasswordUuid);

    final var hashedPassword = getHashedPassword(request.getPassword());

    user.setPassword(hashedPassword.getResult());
    user.setUserName(request.getUsername());
    user.setFullName(request.getFullName());
    user.setDocumentId(request.getDocumentId());
    user.setResetPasswordUuid("");
    user.setStatus(UserStatus.ACTIVE);

    logger.debug("{}", user);
    save(user);
    return user;
  }

  private Hash getHashedPassword(String password) {
    return Password.hash(password).addRandomSalt(16).addPepper(passwordPepper).with(argon2);
  }

  @Override
  protected UserRepository getRepository() {
    return (UserRepository) super.getRepository();
  }

  @Override
  public User getNewEntity() {
    return new User();
  }

  @Override
  public InviteUserRequest getNewRequest() {
    return new InviteUserRequest();
  }
}
