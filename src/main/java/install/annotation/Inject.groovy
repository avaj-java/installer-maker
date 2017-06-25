package install.annotation

import java.lang.annotation.ElementType
import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Created by sujkim on 2017-06-11.
 */
@Target([ElementType.METHOD, ElementType.FIELD])
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@interface Inject {

    String value() default ''

    String property() default ''

    String method() default 'get'

}