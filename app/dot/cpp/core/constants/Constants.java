package dot.cpp.core.constants;

import dot.cpp.core.models.user.entity.User;
import play.libs.typedmap.TypedKey;

public class Constants {
  public static final int EMAIL_FIELD_LENGTH = 140;

  public static final int COMMON_FIELD_LENGTH = 70;

  public static final int NAME_FIELD_LENGTH = 120;

  public static final int LONG_FIELD_LENGTH = 200;

  public static final int DESCRIPTION_FIELD_LENGTH = 2000;

  public static final String ACCESS_TOKEN = "access_token";
  public static final String REFRESH_TOKEN = "refresh_token";

  public static final TypedKey<User> USER = TypedKey.create("USER");
}
