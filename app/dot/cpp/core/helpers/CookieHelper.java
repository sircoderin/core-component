package dot.cpp.core.helpers;

import dot.cpp.core.constants.Constants;
import play.mvc.Http;
import play.mvc.Result;

public class CookieHelper {

  public static Http.Cookie getCookie(String cookieName, String cookieValue, boolean isSecure) {
    return Http.Cookie.builder(cookieName, cookieValue)
        .withHttpOnly(true)
        .withSecure(isSecure)
        .build();
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

  private CookieHelper() {}
}
