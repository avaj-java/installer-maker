package com.jaemisseo.man.annotation

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created with IntelliJ IDEA.
 * User: sujkim
 * Date: 10/27/16
 * Time: 11:26 PM
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ReportSheet{

    int width() default -1

    int height() default -1

    int headerHeight() default -1

    String headerPosition() default "";

    String dataPosition() default "";

    String freezePane() default "";

    String autoFilter() default "";


}