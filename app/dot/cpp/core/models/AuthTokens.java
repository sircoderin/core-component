package dot.cpp.core.models;

import play.libs.Json;

public class AuthTokens {

  public final String accessToken;
  public final String refreshToken;

  public AuthTokens(String accessToken, String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }

  @Override
  public String toString() {
    return Json.stringify(Json.toJson(this));
  }
}
