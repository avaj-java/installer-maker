package install.job

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.VariableMan
import install.bean.ReceptionistGlobalOption
import install.task.TaskUtil

/**
 * Created by sujkim on 2017-02-17.
 */
class JobReceptionist extends TaskUtil{

    JobReceptionist(PropMan propman){
        //Job Setup
        levelNamesProperty = 'ask.level'
        validTaskList = [TASK_NOTICE, TASK_Q, TASK_Q_CHOICE, TASK_Q_YN]

        this.propman = propman
        this.varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman, levelNamesProperty)
        setBeforeGetProp(propman, varman)
        this.gOpt = new ReceptionistGlobalOption().merge(new ReceptionistGlobalOption(
            modeRemember        : propman.get("mode.remember.answer"),
            remeberFilePath     : propman.get("remember.answer.file.path"),
            rememberFileSetup   : genFileSetup("remember.answer.")
        ))
    }



    /**
     * RUN
     */
    void run(){

        //1. READ ANSWER
        if (gOpt.modeRemember){
            try{
                PropMan rememberAnswerPropman = new PropMan().readFile(gOpt.remeberFilePath).properties
                propman.merge(rememberAnswerPropman)
            }catch(Exception e){
            }
        }

        //2. Each level by level
        eachLevel(levelNamesProperty){ String levelName ->
            String propertyPrefix = "${levelNamesProperty}.${levelName}."
            String taskName = getString(propertyPrefix, 'task')?.trim()?.toUpperCase()
            runTask(taskName, propertyPrefix)
        }

        //3. WRITE ANSWER
        if (gOpt.modeRemember){
            FileMan fileman = new FileMan(gOpt.remeberFilePath).set(gOpt.rememberFileSetup)
            try{
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
