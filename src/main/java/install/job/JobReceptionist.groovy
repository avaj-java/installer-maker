package install.job

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.VariableMan
import com.jaemisseo.man.util.FileSetup
import install.bean.ReceptionistGlobalOption

/**
 * Created by sujkim on 2017-02-17.
 */
class JobReceptionist extends JobUtil{

    JobReceptionist(PropMan propman){
        //Job Setup
        levelNamesProperty = 'a.level'
        levelNamePrefix = 'a'

        validTaskList = [TASK_NOTICE, TASK_Q, TASK_Q_CHOICE, TASK_Q_YN, TASK_SET]

        this.propman = propman
        this.varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman, levelNamePrefix)
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
        eachLevel(levelNamesProperty, levelNamePrefix, 'receptionist.properties'){ String levelName ->
            String propertyPrefix = "${levelNamePrefix}.${levelName}."
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
