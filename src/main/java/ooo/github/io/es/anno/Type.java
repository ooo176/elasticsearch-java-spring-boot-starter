package ooo.github.io.es.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kaiqin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Type {

    String[] type() default {"text", "keyword"};

    String copyTo() default "";

    Class<?> clazz() default Object.class;

    String analyzer() default "";


}
