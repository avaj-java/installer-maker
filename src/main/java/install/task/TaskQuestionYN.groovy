package install.task

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.QuestionMan
import com.jaemisseo.man.util.QuestionSetup

/**
 * Created by sujkim on 2017-03-18.
 */
class TaskQuestionYN extends TaskUtil{

    TaskQuestionYN(PropMan propman, List rememberAnswerLineList){
        this.propman = propman
        this.qman = new QuestionMan()
        this.rememberAnswerLineList = rememberAnswerLineList
    }



    void run(String propertyPrefix) {

        //Get Properties
        def conditionIfObj      = propman.parse("${propertyPrefix}if")
        QuestionSetup opt       = genQuestionSetup(propertyPrefix)

        //Ask Question
        if (propman.match(conditionIfObj)){
            //Get Answer
            String yourAnswer = qman.question(opt, QuestionMan.QUESTION_TYPE_YN)

            //Remeber Answer
            rememberAnswerLineList.add("${propertyPrefix}answer=${yourAnswer}")

            //Set 'answer' and 'value' Property
            String value = qman.getValue()
            propman.set("${propertyPrefix}answer", yourAnswer)
            propman.set("${propertyPrefix}value", value)

            //If 'answer' is Y
            if (yourAnswer?.toUpperCase()?.equals(QuestionMan.Y)){

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


}
