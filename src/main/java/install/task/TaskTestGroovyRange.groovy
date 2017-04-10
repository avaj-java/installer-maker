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

    List<String> getLevelList(String levelNamesProperty){
        List<String> resultList = []
        List<String> list = propman.get(levelNamesProperty).split("\\s*,\\s*")
        //Each Specific Levels
        list.each{ String levelName ->
            if (levelName.contains('-')) {
                resultList += getListWithDashRange(levelName as String)
            }else if (levelName.contains('..')){
                resultList += getListWithDotDotRange(levelName as String)
            }else{
                resultList << levelName
            }
        }
        return resultList
    }


}
