package dot.cpp.core.constants;

import dot.cpp.core.models.user.entity.User;
import play.libs.typedmap.TypedKey;

public final class Constants {

  public static final String ACCESS_TOKEN = "access_token";
  public static final String REFRESH_TOKEN = "refresh_token";
  public static final TypedKey<User> USER = TypedKey.create("USER");

  private Constants() {}
}
