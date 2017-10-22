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

    String name() default ''

    String filter() default ''

    String prefix() default ''

    boolean modeRenderJansi() default false



    boolean required() default false

    boolean englishOnly() default false

    boolean numberOnly() default false

    boolean charOnly() default false

    int minLength() default 0

    int maxLength() default 0
    
    String[] validList() default []

    String[] contains() default []

    String[] caseIgnoreValidList() default []

    String[] caseIgnoreContains() default []

    String regexp() default ''


}