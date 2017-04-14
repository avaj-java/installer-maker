package install.task

import com.jaemisseo.man.PropMan

/**
 * Created by sujkim on 2017-03-18.
 */
class TaskTestGroovyRange extends TaskUtil{

    @Override
    Integer run(){
        println getLevelList("range").join(', ')
        return STATUS_TASK_DONE
    }



    List<String> getLevelList(String rangePropertyName){
        List<String> resultList = []
        List<String> list = get(rangePropertyName).split("\\s*,\\s*")
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
