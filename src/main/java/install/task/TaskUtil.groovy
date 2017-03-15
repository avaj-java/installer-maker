package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.SqlMan
import com.jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-03-11.
 */
class TaskUtil {

    static final String TASK_SQL = "SQL"
    static final String TASK_TAR = "TAR"
    static final String TASK_ZIP = "ZIP"
    static final String TASK_UNTAR = "UNTAR"
    static final String TASK_UNZIP = "UNZIP"
    static final String TASK_UNJAR = "UNJAR"
    static final String TASK_MKDIR = "MKDIR"
    static final String TASK_COPY = "COPY"
    static final String TASK_REPLACE = "REPLACE"

    PropMan propman
    SqlMan sqlman
    FileMan fileman

    String levelNamesProperty = ''
    List beforeReportList = []
    List afterReportMapList = []
    List validTaskList = []
    List invalidTaskList = []

    def gOpt



    void run(){
        run('')
    }

    void run(String levelName){
        println "It is Empty Task."
    }

    void runTask(String taskName, String propertyPrefix){
        //Check Valid Task
        if ( !taskName || (validTaskList && !validTaskList.contains(taskName)) || (invalidTaskList && invalidTaskList.contains(taskName)) )
            return
        //Run Task
        switch (taskName){
            case TASK_TAR:
                new TaskFileTar(propman).run(propertyPrefix)
                break
            case TASK_ZIP:
                new TaskFileZip(propman).run(propertyPrefix)
                break
            case TASK_UNTAR:
                new TaskFileUntar(propman).run(propertyPrefix)
                break
            case TASK_UNZIP:
                new TaskFileUnzip(propman).run(propertyPrefix)
                break
            case TASK_UNJAR:
                new TaskFileUnjar(propman).run(propertyPrefix)
                break
            case TASK_REPLACE:
                new TaskFileReplace(propman).run(propertyPrefix)
                break
            case TASK_COPY:
                new TaskFileCopy(propman).run(propertyPrefix)
                break
            case TASK_MKDIR:
                new TaskFileMkdir(propman).run(propertyPrefix)
                break
            case TASK_SQL:
                new TaskSql(sqlman, propman, gOpt).setBeforeReporter(beforeReportList).setAfterReporter(afterReportMapList).run(propertyPrefix)
                break
            default :
                break
        }
    }



    protected void logBigTitle(String title){
        println ""
        println ""
        println ""
        println "///////////////////////////////////////////////////////////////////////////"
        println "///// ${title}"
        println "///////////////////////////////////////////////////////////////////////////"
    }

    protected void logMiddleTitle(String title){
        println '\n=================================================='
        println " - ${title} -"
        println '=================================================='
    }

    //Do level by level
    protected void eachLevel(String levelNamesProperty, Closure closure){
        getLevelList(levelNamesProperty).each{ String levelName ->
            closure(levelName)
        }
    }

    protected List<String> getLevelList(String levelNamesProperty){
        List<String> resultList = []
        List<String> list = propman.get(levelNamesProperty).split("\\s*,\\s*")
        list.each{ String levelName ->
            if (levelName.contains('-')){
                resultList += getListWithDashRange(levelName as String)
            }else{
                resultList << levelName
            }
        }
        return resultList
    }

    protected List<String> getListWithDashRange(String dashRnage){
        List<String> resultList = []
        List<String> split = dashRnage.split('[-]')
        String rangeStart = split[0]
        String rangeEnd = split[1]
        if (rangeStart.isNumber() && rangeEnd.isNumber())
            (Integer.parseInt(rangeStart)..Integer.parseInt(rangeEnd)).each{
                resultList << it.toString()
            }
        else{
            (rangeStart..rangeEnd).each{
                resultList << it.toString()
            }
        }
        return resultList
    }

    protected def getValue(String propertyName){
        return getValue("", propertyName)
    }

    protected def getValue(String propertyPrefix, String propertyName){
        return propman.get("${propertyPrefix}${propertyName}") ?: ''
    }

    protected String getString(String propertyName){
        return getString("", propertyName)
    }

    protected String getString(String propertyPrefix, String propertyName){
        return propman.get("${propertyPrefix}${propertyName}") ?: ''
    }

    protected List<String> getFilePathList(String propertyPrefix, String propertyName){
        return getFilePathList(propertyPrefix, propertyName, '')
    }

    protected List<String> getFilePathList(String propertyPrefix, String propertyName, String extention){
        String filePath = propman.get("${propertyPrefix}${propertyName}")
        return FileMan.getFilePathList(filePath, extention)
    }

    protected String getFilePath(String propertyPrefix, String propertyName){
        String filePath = propman.get("${propertyPrefix}${propertyName}")
        return FileMan.getFullPath(filePath)
    }

    protected Map getMap(String propertyName){
        return getMap("", propertyName)
    }

    protected Map getMap(String propertyPrefix, String propertyName){
        Map map = propman.parse("${propertyPrefix}${propertyName}")
//        if (map){
//            VariableMan varman = new VariableMan(map)
//            map.each{ String propNm, String value ->
//                map[propNm] = varman.parse(value)
//            }
//        }
        return map
    }

    protected FileSetup getFileSetup(){
        return getFileSetup('')
    }

    protected FileSetup getFileSetup(String propertyPrefix){
        return new FileSetup(
                encoding    : propman.get("${propertyPrefix}file.encoding"),
                modeBackup  : propman.get("${propertyPrefix}file.backup.use"),
                backupPath  : propman.get("${propertyPrefix}file.backup.path")
        )
    }
}
