package install.task

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.QuestionMan
import com.jaemisseo.man.util.QuestionSetup

/**
 * Created by sujkim on 2017-03-18.
 */
class TaskQuestionYN extends TaskUtil{

    TaskQuestionYN(PropMan propman){
        this.propman = propman
        this.qman = new QuestionMan()
    }



    void run(String propertyPrefix) {

        //Get Properties
        QuestionSetup opt       = genQuestionSetup(propertyPrefix)

        //Ask Question
        //Get Answer
        String yourAnswer = qman.question(opt, QuestionMan.QUESTION_TYPE_YN)

        //Remeber Answer
        rememberAnswerLineList.add("${propertyPrefix}answer.default=${yourAnswer}")

        //Set 'answer' and 'value' Property
        String value = qman.getValue()
        propman.set("${propertyPrefix}answer", yourAnswer)
        propman.set("${propertyPrefix}value", value)

        //If 'answer' is Y
        if (yourAnswer?.toUpperCase()?.equals(QuestionMan.Y))
            setPropValue(propertyPrefix)

    }


}
