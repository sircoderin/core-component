package dot.cpp.core.interfaces;

public abstract class BaseRequest {

  protected String userId;

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }
}
