package install.bean

import jaemisseo.man.ReportMan
import com.jaemisseo.man.annotation.*
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
class ReportFile extends Option{

    @ReportSheetName
    String sqlFileName

    @ReportColumn(index=0, headerName="SEQ")
    Integer seq

    @ReportColumn(index=3, headerName="QUERY", width=13000)
    String query

    @ReportColumnDataStyle(alignment=HSSFCellStyle.ALIGN_CENTER)
//    @ReportColumn(index=1, headerName="EXIST")
    String isExistOnDB

    @ReportColumn(index=2, headerName="OK")
    String isOk

    @ReportColumnHighlightStyle(condition='not($${1}${dataStartRow}="")', range=ReportMan.RANGE_DATA_ALL, background=HSSFColor.RED.index)
    @ReportColumn(index=1, headerName="WARN", width=5000)
    String warnningMessage

    @ReportColumnHighlightStyle(condition='not($${4}${dataStartRow}="")', range=ReportMan.RANGE_DATA_ALL, background=HSSFColor.RED.index)
    @ReportColumn(index=4, headerName="ERROR", width=20000)
    String error
}
