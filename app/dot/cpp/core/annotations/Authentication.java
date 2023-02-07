package dot.cpp.core.annotations;

import dot.cpp.core.actions.AuthenticationAction;
import dot.cpp.core.enums.UserRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import play.mvc.With;

@With(AuthenticationAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authentication {

  /** This parameter will redirect to the URL. */
  String redirectUrl() default "/login";

  UserRole[] userRoles() default {};

  int status() default -1;
}
