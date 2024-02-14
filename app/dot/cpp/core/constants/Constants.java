package dot.cpp.core.constants;

import play.libs.typedmap.TypedKey;

public final class Constants {

  public static final String ACCESS_TOKEN = "access_token";
  public static final String REFRESH_TOKEN = "refresh_token";
  public static final String USER_ROLE = "user_role";
  public static final TypedKey<String> USER_ID = TypedKey.create("USER_ID");

  private Constants() {}
}
