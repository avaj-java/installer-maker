package com.jaemisseo.hoya.bean

import jaemisseo.man.ReportMan
import jaemisseo.man.annotation.ReportColumn
import jaemisseo.man.annotation.ReportColumnDataStyle
import jaemisseo.man.annotation.ReportColumnHighlightStyle
import jaemisseo.man.annotation.ReportSheet
import jaemisseo.man.annotation.ReportSheetDataStyle
import jaemisseo.man.annotation.ReportSheetDataTwoToneStyle
import jaemisseo.man.annotation.ReportSheetHeaderStyle
import jaemisseo.man.annotation.ReportSheetName
import jaemisseo.man.annotation.ReportSheetStyle
import jaemisseo.man.util.Option
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.util.HSSFColor

/**
 * Created by sujkim on 2017-02-20.
 */
@ReportSheet(height=230, headerHeight=500, freezePane=ReportMan.RANGE_AUTO, autoFilter=ReportMan.RANGE_AUTO)
@ReportSheetStyle(fontSize=9, border=HSSFCellStyle.BORDER_THIN)
@ReportSheetHeaderStyle(foreground=HSSFColor.LIGHT_YELLOW.index, bold=true, alignment=HSSFCellStyle.ALIGN_CENTER, verticalAlignment=HSSFCellStyle.VERTICAL_CENTER)
@ReportSheetDataStyle(foreground=HSSFColor.LIGHT_GREEN.index)
@ReportSheetDataTwoToneStyle
class ReportSql extends Option{

    @ReportSheetName
    String sqlFileName

    @ReportColumn(index=0, headerName="SEQ", width=1000)
    Integer seq

    @ReportColumnDataStyle(alignment=HSSFCellStyle.ALIGN_CENTER)
    @ReportColumn(index=1, headerName="EXECUTION")
    String isOk

    @ReportColumnDataStyle(wrapText=true)
    @ReportColumn(index=2, headerName="QUERY", width=13000)
    String query

    @ReportColumn(index=3, headerName="EXECUTOR")
    String executor

    @ReportColumn(index=4, headerName="COMMAND")
    String commandType

    @ReportColumn(index=5, headerName="OBJECT TYPE")
    String objectType


    @ReportColumnDataStyle(wrapText=true)
    @ReportColumn(index=6, headerName="SCHEME NAME", width=6000)
    String schemeName

    @ReportColumnDataStyle(wrapText=true)
    @ReportColumn(index=7, headerName="OBJECT NAME", width=6000)
    String objectName

    @ReportColumnDataStyle(alignment=HSSFCellStyle.ALIGN_CENTER)
//    @ReportColumn(index=1, headerName="EXIST")
    String isExistOnDB



    @ReportColumnHighlightStyle(condition='not($${8}${dataStartRow}="")', range=ReportMan.RANGE_DATA_ALL, background=HSSFColor.RED.index)
    @ReportColumnDataStyle(wrapText=true)
    @ReportColumn(index=8, headerName="WARN BEFORE", width=5000)
    String warnningMessage

    @ReportColumnHighlightStyle(condition='not($${9}${dataStartRow}="")', range=ReportMan.RANGE_DATA_ALL, background=HSSFColor.RED.index)
    @ReportColumnDataStyle(wrapText=true)
    @ReportColumn(index=9, headerName="ERROR AFTER", width=10000)
    String error
}
