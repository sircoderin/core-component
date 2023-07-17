package dot.cpp.core.helpers;

import dot.cpp.core.constants.Constants;
import dot.cpp.core.models.session.entity.Session;
import play.mvc.Http;
import play.mvc.Result;

public class CookieHelper {

  /** Get access cookie for an application. */
  public static Http.Cookie getAccessCookie(Session session) {
    return getCookie(Constants.ACCESS_TOKEN, session.getAccessToken());
  }

  /** Get refresh cookie for an application. */
  public static Http.Cookie getRefreshCookie(Session session) {
    return getCookie(Constants.REFRESH_TOKEN, session.getRefreshToken());
  }

  /**
   * Get cookie for an application.
   *
   * @param session {@link Session}
   * @return {@link Http.Cookie} the cookie containing authorization
   */
  public static Http.Cookie getCookie(String cookieName, String session) {
    return Http.Cookie.builder(cookieName, session).withHttpOnly(false).withSecure(true).build();
  }

  /**
   * Discard authorization cookie for an application.
   *
   * @param result {@link Result}
   * @return {@link Http.Cookie} the result without the authorization cookie
   */
  public static Result discardAuthorizationCookies(Result result) {
    return result
        .discardingCookie(Constants.ACCESS_TOKEN)
        .discardingCookie(Constants.REFRESH_TOKEN);
  }

  public static String getCookieString(Http.Request request, String cookieName) {
    final var cookie = request.getCookie(cookieName).orElse(null);

    if (cookie == null) {
      return null;
    }

    return cookie.value();
  }
}
