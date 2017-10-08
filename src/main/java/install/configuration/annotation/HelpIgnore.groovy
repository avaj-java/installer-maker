package install.configuration.annotation

import java.lang.annotation.*

/**
 * Created by sujkim on 2017-06-11.
 */
@Inherited
@Target([ElementType.TYPE, ElementType.FIELD, ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
@interface HelpIgnore {

}