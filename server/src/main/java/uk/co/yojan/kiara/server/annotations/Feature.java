package uk.co.yojan.kiara.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Feature {
  boolean array() default false;
  int dims() default 1;
  int[] size() default {1};
  double weight() default 1.0;
}
