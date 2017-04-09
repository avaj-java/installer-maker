package install.task

import com.jaemisseo.man.PropMan

/**
 * Created by sujkim on 2017-03-18.
 */
class TaskTestGroovyRange extends TaskUtil{

    TaskTestGroovyRange(PropMan propman){
        this.propman = propman
    }



    void run(String propertyPrefix){

        List<String> levelList = getLevelList("${propertyPrefix}range")
        println levelList.join(', ')

    }

}
