package dot.cpp.core.services;

import static dot.cpp.repository.models.BaseEntity.RECORD_ID;

import com.password4j.Argon2Function;
import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.types.Argon2;
import com.typesafe.config.Config;
import dev.morphia.query.filters.Filters;
import dot.cpp.core.enums.ErrorCodes;
import dot.cpp.core.exceptions.BaseException;
import dot.cpp.core.models.user.entity.User;
import dot.cpp.core.models.user.repository.UserRepository;
import dot.cpp.core.models.user.request.SetPasswordRequest;
import dot.cpp.core.models.user.request.UserRequest;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class UserService extends EntityService<User, UserRequest> {

  public static final String SYSTEM = "system";
  public static final String EMAIL = "email";
  public static final String ACTIVE = "active";
  public static final String USER_NAME = "userName";
  public static final String DEACTIVATE_USER = "Deactivate user";
  public static final String ACTIVATE_USER = "Activate user";
  private static final String RESET_PASSWORD_UUID = "resetPasswordUuid";
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final String passwordPepper;
  private final Argon2Function argon2 = Argon2Function.getInstance(1000, 4, 2, 32, Argon2.ID, 19);

  @Inject
  public UserService(UserRepository userRepository, Config config) {
    super(userRepository, config);
    this.passwordPepper = config.getString("password.pepper");
  }

  @Override
  public void setEntityFromRequest(User entity, UserRequest request) throws BaseException {
    if (emailExists(request.getEmail(), entity.getRecordId())) {
      throw BaseException.from(ErrorCodes.USER_EMAIL_EXISTS);
    }

    if (userNameExists(request.getUserName(), entity.getRecordId())) {
      throw BaseException.from(ErrorCodes.USER_NAME_EXISTS);
    }

    super.setEntityFromRequest(entity, request);

    if (entity.getRecordId() == null) {
      entity.setActive(true);
    }
  }

  @Override
  public User getNewEntity() {
    return new User();
  }

  @Override
  public UserRequest getNewRequest() {
    return new UserRequest();
  }

  @Override
  protected BaseException notFoundException() {
    return BaseException.from(ErrorCodes.USER_NOT_FOUND);
  }

  @Override
  protected UserRepository getRepository() {
    return (UserRepository) super.getRepository();
  }

  public User setPassword(
      SetPasswordRequest request, String resetPasswordUuid, String modifiedComment)
      throws BaseException {
    final var user = findByField(RESET_PASSWORD_UUID, resetPasswordUuid);
    final var hashedPassword = getHashedPassword(request.getPassword());
    user.setPassword(hashedPassword.getResult());
    user.setResetPasswordUuid("");
    user.setModifiedComment(modifiedComment != null ? modifiedComment : "Set password");

    return saveWithHistory(user, user.getRecordId());
  }

  public String generateResetPasswordUuid(String email, String modifiedComment)
      throws BaseException {
    final var user = findByField(EMAIL, email);
    if (!user.isActive()) {
      throw BaseException.from(ErrorCodes.USER_INACTIVE_ACCOUNT);
    }

    final var resetPasswordUuid = UUID.randomUUID().toString();
    user.setResetPasswordUuid(resetPasswordUuid);
    user.setModifiedComment(modifiedComment != null ? modifiedComment : "Reset password");

    saveWithHistory(user, user.getRecordId());
    return resetPasswordUuid;
  }

  public void setActive(String id) throws BaseException {
    final var user = findById(id);
    if (user == null) {
      throw notFoundException();
    }

    user.setActive(true);
    user.setModifiedComment(ACTIVATE_USER);
    saveWithHistory(user);
  }

  public void setInactive(String id) throws BaseException {
    final var user = findById(id);
    if (user == null) {
      throw notFoundException();
    }

    user.setActive(false);
    user.setEmail(String.format("inactive-%s@terra.ro", user.getUserName()));
    user.setModifiedComment(DEACTIVATE_USER);
    saveWithHistory(user);
  }

  public boolean passwordIsValid(String actualPassword, String inputPassword) {
    return Password.check(inputPassword, actualPassword).addPepper(passwordPepper).with(argon2);
  }

  private boolean emailExists(String email, String id) {
    return findFirst(Filters.and(Filters.ne(RECORD_ID, id), Filters.eq(EMAIL, email))) != null;
  }

  private boolean userNameExists(String userName, String id) {
    return findFirst(Filters.and(Filters.ne(RECORD_ID, id), Filters.eq(USER_NAME, userName)))
        != null;
  }

  private Hash getHashedPassword(String password) {
    return Password.hash(password).addRandomSalt(16).addPepper(passwordPepper).with(argon2);
  }
}
