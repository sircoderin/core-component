package dot.cpp.core.models.session.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Transient;
import dot.cpp.repository.models.BaseEntity;
import play.libs.Json;
import play.mvc.Http.Cookie;

@Entity("Session")
public class Session extends BaseEntity {

  private String userId;
  private String accessToken;
  private String refreshToken;
  private Long refreshExpiryTime;
  private String oldRefreshToken;
  private Long createTime;

  @Transient @JsonIgnore private Cookie cookie;

  /**
   * PerformedLogout.
   *
   * @return boolean
   */
  public boolean performedLogout() {
    return refreshExpiryTime == null || refreshExpiryTime == 0;
  }

  @Override
  public String toString() {
    return Json.stringify(Json.toJson(this));
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public Long getRefreshExpiryTime() {
    return refreshExpiryTime;
  }

  public void setRefreshExpiryTime(Long refreshExpiryTime) {
    this.refreshExpiryTime = refreshExpiryTime;
  }

  public String getOldRefreshToken() {
    return oldRefreshToken;
  }

  public void setOldRefreshToken(String oldRefreshToken) {
    this.oldRefreshToken = oldRefreshToken;
  }

  public Long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Long createTime) {
    this.createTime = createTime;
  }
}
