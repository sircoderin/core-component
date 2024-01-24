package dot.cpp.core.services;

import static dot.cpp.core.constants.Constants.ACCESS_TOKEN;
import static dot.cpp.core.constants.Constants.REFRESH_TOKEN;

import javax.inject.Inject;
import javax.inject.Singleton;
import play.cache.AsyncCacheApi;
import play.libs.F;

@Singleton
public class SessionService {
  private final AsyncCacheApi cache;

  @Inject
  public SessionService(AsyncCacheApi cache) {
    this.cache = cache;
  }

  public void addTokensToCache(String id, String accessToken, String refreshToken) {
    final var syncCache = cache.sync();
    syncCache.set(getFormat(id, ACCESS_TOKEN), accessToken);
    syncCache.set(getFormat(id, REFRESH_TOKEN), refreshToken);
  }

  public F.Tuple<String, String> getTokens(String id) {
    final var syncCache = cache.sync();
    final var accessToken = syncCache.get(getFormat(id, ACCESS_TOKEN)).orElse("");
    final var refreshToken = syncCache.get(getFormat(id, REFRESH_TOKEN)).orElse("");
    return F.Tuple((String) accessToken, (String) refreshToken);
  }

  private static String getFormat(String id, String tokenId) {
    return String.format("%s_%s", id, tokenId);
  }
}
