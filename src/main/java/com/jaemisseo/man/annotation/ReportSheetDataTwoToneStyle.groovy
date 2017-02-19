package com.jaemisseo.man.annotation

import org.apache.poi.ss.usermodel.FillPatternType

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by sujkim on 2017-02-09.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ReportSheetDataTwoToneStyle {

    String pk() default ""

    int apply() default -1

    //POI's STYLE
    short color() default (short) -1
    int fontHeightInPoints() default -1
    boolean bold() default false
    boolean italic() default false
    short alignment() default (short) -1
    short verticalAlignment() default (short) -1
    short fillForegroundColor() default (short) -1
    short fillBackgroundColor() default (short) -1
    FillPatternType fillPattern() default FillPatternType.NO_FILL
    short borderTop() default (short) -1
    short borderBottom() default (short) -1
    short borderLeft() default (short) -1
    short borderRight() default (short) -1

    //ReportMan CUSTOM STYLE
    int fontSize() default -1
    short border() default (short) -1
    short foreground() default (short) -1
    short background() default (short) -1
}