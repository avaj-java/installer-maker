package com.jaemisseo.hoya.bean

import jaemisseo.man.ReportMan
import jaemisseo.man.annotation.*
import jaemisseo.man.util.Option
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.util.HSSFColor

/**
 * Created by sujkim on 2017-02-20.
 */
@ReportSheet(height=230, headerHeight=500, freezePane=ReportMan.RANGE_AUTO, autoFilter=ReportMan.RANGE_AUTO)
@ReportSheetStyle(fontSize=9, border=HSSFCellStyle.BORDER_THIN)
@ReportSheetHeaderStyle(foreground=HSSFColor.LIGHT_YELLOW.index, bold=true, alignment=HSSFCellStyle.ALIGN_CENTER, verticalAlignment=HSSFCellStyle.VERTICAL_CENTER)
//@ReportSheetDataStyle(foreground=HSSFColor.LIGHT_GREEN.index)
//@ReportSheetDataTwoToneStyle
class ReportDiffFile extends Option{

    @ReportSheetName
    String sqlFileName



    @ReportColumn(index=0, headerName="SEQ", width=1000)
    Integer seq

    @ReportColumnHighlightStyle(condition='AND($${1}${dataStartRow}=TRUE, $${4}${dataStartRow}<>"0")', range=ReportMan.RANGE_DATA_ALL, background=HSSFColor.BRIGHT_GREEN.index)
    @ReportColumn(index=1, headerName="COPIED", width=2000)
    Boolean copied

    @ReportColumn(index=2, headerName="CHANGE", width=2000)
    String changeStatusCode

    @ReportColumn(index=3, headerName="ENTRY", width=25000)
    String entryPath

    @ReportColumn(index=4, headerName="SIZE", width=3000)
    String fileSize

    @ReportColumnHighlightStyle(condition='NOT($${5}${dataStartRow}="")', range=ReportMan.RANGE_DATA_ALL, background=HSSFColor.PINK.index)
    @ReportColumnDataStyle(wrapText=true)
    @ReportColumn(index=5, headerName="WARN", width=10000)
    String warn = ''

    @ReportColumnHighlightStyle(condition='NOT($${6}${dataStartRow}="")', range=ReportMan.RANGE_DATA_ALL, background=HSSFColor.RED.index)
    @ReportColumnDataStyle(wrapText=true)
    @ReportColumn(index=6, headerName="ERROR", width=10000)
    String error = ''
}
