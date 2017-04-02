package install.task

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.VariableMan

/**
 * Created by sujkim on 2017-03-18.
 */
class TaskNotice extends TaskUtil{

    TaskNotice(PropMan propman){
        this.propman = propman
    }



    void run(String propertyPrefix){

        //Get Message
        String msg = propman.get("${propertyPrefix}msg")

        //Show You Welcome Message
        println msg

    }
}
