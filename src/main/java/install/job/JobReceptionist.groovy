package install.job

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.VariableMan
import com.jaemisseo.man.util.FileSetup
import install.bean.ReceptionistGlobalOption
import install.task.TaskQuestion
import install.task.TaskQuestionChoice
import install.task.TaskQuestionYN
import install.task.TaskUtil

/**
 * Created by sujkim on 2017-02-17.
 */
class JobReceptionist extends JobUtil{

    JobReceptionist(PropMan propman){
        //Job Setup
        levelNamesProperty = 'a.level'
        executorNamePrefix = 'a'
        propertiesFileName = 'receptionist.properties'
        validTaskList = [TASK_NOTICE, TASK_Q, TASK_Q_CHOICE, TASK_Q_YN, TASK_SET]

        this.propman = propman
        this.varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman, executorNamePrefix)
        setBeforeGetProp(propman, varman)
        this.gOpt = new ReceptionistGlobalOption().merge(new ReceptionistGlobalOption(
                modeRemember        : getBoolean("mode.remember.answer"),
                rememberFilePath    : getString("remember.answer.file.path"),
                rememberFileSetup   : genOtherFileSetup("remember.answer."),
                responseFilePath    : getString("response.file.path"),
        ))
    }

    /**
     * BUILD FORM
     */
    /**
     * Generate Response File
     * 1. Load receptionist.properties
     * 2. Gen install.rsp
     */
    void buildForm(){
        if (!propman.getBoolean('mode.auto.rsp'))
            return

        List<String> allLineList = []

        logBigTitle('AUTO CREATE RESPONSE FILE')

        //Each level by level
        eachLevel{ String propertyPrefix ->
            String taskName = getTaskName(propertyPrefix)
            //Check Valid Task
            if (!taskName)
                throw new Exception(" 'No Task Name. ${propertyPrefix}task=???. Please Check Task.' ")
            if ( (validTaskList && !validTaskList.contains(taskName)) || (invalidTaskList && invalidTaskList.contains(taskName)) )
                throw new Exception(" 'Sorry, This is Not my task, [${taskName}]. I Can Not do this.' ")

            //Run Task
            List<String> lineList = []
            switch (taskName){
                case TaskUtil.TASK_Q:
                    lineList.addAll( new TaskQuestion().setPropman(propman).buildForm(propertyPrefix) )
                    break
                case TaskUtil.TASK_Q_CHOICE:
                    lineList.addAll( new TaskQuestionChoice().setPropman(propman).buildForm(propertyPrefix) )
                    break
                case TaskUtil.TASK_Q_YN:
                    lineList.addAll( new TaskQuestionYN().setPropman(propman).buildForm(propertyPrefix) )
                    break

                default :
                    break
            }

            //Add One Question into Form
            if (lineList){
                List editingList = lineList.collect{ "# $it" }
                editingList.add(0, "#########################")
                editingList << "#########################"
                editingList << "${propertyPrefix}answer="
                editingList << ""
                allLineList.addAll(editingList)
            }
        }

        //Make a Response File
        String buildInstallerHome = getString('build.installer.home')
        String homeToRspRelPath = getString('installer.home.to.rsp.relpath')
        String rspDestPath = FileMan.getFullPath(buildInstallerHome, homeToRspRelPath)
        String rspInstallRspDestPath = "${rspDestPath}/install.rsp"
        FileMan.write(rspInstallRspDestPath, allLineList, true)
    }

    /**
     * ASK
     */
    void ask(){
        //0. Check Response File
        if (checkResponseFile()){
            propman.merge(getResponsePropMan())
            propman.set('answer.repeat.limit', 0)
            logBigTitle('Add Response File Answer')
        }


        //1. READ ANSWER
        readRemeber()

        //2. Each level by level
        eachLevelForTask{ String propertyPrefix ->
            return runTaskByPrefix("${propertyPrefix}")
        }

        //3. WRITE ANSWER
        writeRemember()
    }



    boolean checkResponseFile(){
        String responseFilePath = gOpt.responseFilePath
        if (responseFilePath){
            if (new File(responseFilePath).exists()){
                return true
            }else{
                println " < Failed > Load Response File, Does not exists file - ${responseFilePath}"
                System.exit(0)
            }
        }
        return false
    }

    PropMan getResponsePropMan(){
        String responseFilePath = gOpt.responseFilePath
        return new PropMan(responseFilePath)
    }


    /**
     * Read Remeber File
     */
    private void readRemeber(){
        Boolean modeRemember = gOpt.modeRemember
        String rememberFilePath = gOpt.rememberFilePath

        if (modeRemember){
            logBigTitle('LOAD Remembered Your Answer ')
            try{
                PropMan rememberAnswerPropman = new PropMan().readFile(rememberFilePath).properties
                propman.merge(rememberAnswerPropman)
            }catch(Exception e){
                println "No Remember File"
            }
        }
    }

    /**
     * Backup & Write Remeber File
     */
    private void writeRemember(){
        Boolean modeRemember = gOpt.modeRemember
        String rememberFilePath = gOpt.rememberFilePath
        FileSetup fileSetup = gOpt.rememberFileSetup

        if (modeRemember){
            logBigTitle('SAVE Your Answer')
            FileMan fileman = new FileMan(rememberFilePath).set(fileSetup)
            try{
                if (fileman.exists())
                    fileman.backup()
            }catch(Exception e){
                e.printStackTrace()
            }
            try{
                fileman.read(rememberAnswerLineList).write()
            }catch(Exception e){
                e.printStackTrace()
            }
        }
    }


}
