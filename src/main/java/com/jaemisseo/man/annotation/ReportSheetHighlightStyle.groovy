package com.jaemisseo.man.annotation

import org.apache.poi.ss.usermodel.FillPatternType

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
public @interface ReportSheetHighlightStyle {

    String condition() default ""

    String range() default ""

    //POI's STYLE
    short color() default (short) -1
    short fillForegroundColor() default (short) -1
    short fillBackgroundColor() default (short) -1
    FillPatternType fillPattern() default FillPatternType.NO_FILL
    short borderTop() default (short) -1
    short borderBottom() default (short) -1
    short borderLeft() default (short) -1
    short borderRight() default (short) -1

    //ReportMan CUSTOM STYLE
    short border() default (short) -1
    short foreground() default (short) -1
    short background() default (short) -1


}