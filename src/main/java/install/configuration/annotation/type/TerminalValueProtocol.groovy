package install.configuration.annotation.type

import java.lang.annotation.*

/**
 * Created by sujkim on 2017-06-11.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface TerminalValueProtocol {

    String[] value() default []

}