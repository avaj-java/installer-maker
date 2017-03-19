package install.task

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.VariableMan

/**
 * Created by sujkim on 2017-03-18.
 */
class TaskWelcome extends TaskUtil{

    TaskWelcome(PropMan propman){
        this.propman = propman
    }



    void run(String propertyPrefix){

        //Get Message
        String msg = propman.get("${propertyPrefix}msg")

        //Show You Welcome Message
        println msg

    }
}
