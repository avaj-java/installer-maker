package install.job

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.VariableMan
import com.jaemisseo.man.util.FileSetup
import install.bean.ReceptionistGlobalOption
import install.task.TaskUtil

/**
 * Created by sujkim on 2017-02-17.
 */
class JobReceptionist extends JobUtil{

    JobReceptionist(PropMan propman){
        //Job Setup
        levelNamesProperty = 'ask.level'
        validTaskList = [TASK_NOTICE, TASK_Q, TASK_Q_CHOICE, TASK_Q_YN, TASK_SET]

        this.propman = propman
        this.varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman, levelNamesProperty)
        setBeforeGetProp(propman, varman)
        this.gOpt = new ReceptionistGlobalOption().merge(new ReceptionistGlobalOption(
                modeRemember        : propman.get("mode.remember.answer"),
                rememberFilePath    : propman.get("remember.answer.file.path"),
                rememberFileSetup   : genMergedFileSetup("remember.answer.")
        ))
    }



    /**
     * ASK
     */
    void ask(){
        //1. READ ANSWER
        readRemeber()

        //2. Each level by level
        eachLevel(levelNamesProperty){ String levelName ->
            String propertyPrefix = "${levelNamesProperty}.${levelName}."
            String taskName = getString(propertyPrefix, 'task')?.trim()?.toUpperCase()
            runTask(taskName, propertyPrefix)
        }

        //3. WRITE ANSWER
        writeRemember()
    }



    /**
     * Read Remeber File
     */
    private void readRemeber(){
        Boolean modeRemember = gOpt.modeRemember
        String rememberFilePath = gOpt.rememberFilePath

        if (modeRemember){
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
