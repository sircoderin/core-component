package dot.cpp.core.models;

import play.libs.Json;

public abstract class BaseRequest {

  protected String userId;

  protected String modifiedComment;

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  public String getModifiedComment() {
    return modifiedComment;
  }

  public void setModifiedComment(String modifiedComment) {
    this.modifiedComment = modifiedComment;
  }

  @Override
  public String toString() {
    return Json.toJson(this).toString();
  }
}
