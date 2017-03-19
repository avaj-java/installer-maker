package install.task

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.QuestionMan
import com.jaemisseo.man.util.QuestionSetup

/**
 * Created by sujkim on 2017-03-18.
 */
class TaskQuestion extends TaskUtil{

    TaskQuestion(PropMan propman, List rememberAnswerLineList){
        this.propman = propman
        this.qman = new QuestionMan()
        this.rememberAnswerLineList = rememberAnswerLineList
    }



    void run(String propertyPrefix){

        //Get Properties
        def conditionIfObj      = propman.parse("${propertyPrefix}if")
        QuestionSetup opt       = genQuestionSetup(propertyPrefix)

        //Ask Question
        if (propman.match(conditionIfObj)){
            //Get Answer
            String yourAnswer = qman.question(opt)

            //Remeber 'answer'
            rememberAnswerLineList.add("${propertyPrefix}answer=${yourAnswer}")

            //Set 'answer' and 'value'
            String value = qman.getValue()
            propman.set("${propertyPrefix}answer", yourAnswer)
            propman.set("${propertyPrefix}value", value)

            //Set Some Property
            def property = propman.parse("${propertyPrefix}property")
            if (property instanceof String){
                propman.set(property, value)
            }else if (property instanceof Map){
                (property as Map).each{ String propName, def propValue ->
                    propman.set(propName, propValue)
                }
            }
        }

    }

}
