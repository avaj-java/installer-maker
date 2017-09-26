package install.configuration.annotation

import java.lang.annotation.ElementType
import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Created by sujkim on 2017-06-11.
 */
@Inherited
@Target([ElementType.FIELD, ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
@interface Value {

    String value() default ''

    String property() default ''

    String method() default 'get'

    boolean required() default false

    String[] validList() default []

}