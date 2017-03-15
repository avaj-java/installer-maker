package install.task

import com.jaemisseo.man.*
import com.jaemisseo.man.util.FileSetup
import install.bean.BuildGlobalOption

/**
 * Created by sujkim on 2017-02-17.
 */
class TaskBuild extends TaskUtil {

    TaskBuild(PropMan propman) {
        this.propman = propman
        this.gOpt = new BuildGlobalOption().merge(new BuildGlobalOption(
            modeGenerateReportText     : propman.get('report.text'),
            modeGenerateReportExcel    : propman.get('report.excel'),
            modeGenerateReportSql      : propman.get('report.sql'),
            modeExcludeExecuteSql      : propman.get('x.execute.sql'),
            modeExcludeCheckBefore     : propman.get('x.check.before'),
            modeExcludeReport          : propman.get('x.report'),
            modeExcludeReportConsole   : propman.get('x.report.console'),
            reportFileEncoding         : propman.get('report.file.encoding'),
            reportFileLineBreak        : propman.get('report.file.linebreak'),
            reportFileLastLineBreak    : propman.get('report.file.last.linebreak'),
        ))
    }

    String levelNamesProperty = 'build.level'
    List invalidTaskList = [TASK_SQL]
    BuildGlobalOption gOpt


    /**
     * RUN
     */
    void run(){
        FileSetup fileSetup = generateFileSetup()

        //1. Each level by level
        eachLevel(levelNamesProperty){ String levelName ->
            String propertyPrefix = "${levelNamesProperty}.${levelName}."
            String taskName = getString(propertyPrefix, 'task')?.trim()?.toUpperCase()
            logBigTitle("${levelName}")

            runTask(taskName, propertyPrefix)
        }

    }



    private FileSetup generateFileSetup(){
        FileSetup fileSetup = new FileSetup()
        if (gOpt.reportFileEncoding)
            fileSetup.encoding = gOpt.reportFileEncoding
        if (gOpt.reportFileLineBreak)
            fileSetup.lineBreak = gOpt.reportFileLineBreak
        if (gOpt.reportFileLastLineBreak)
            fileSetup.lastLineBreak = gOpt.reportFileLastLineBreak
        return fileSetup
    }

}
