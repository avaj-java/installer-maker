package install.task

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.QuestionMan
import com.jaemisseo.man.util.QuestionSetup

/**
 * Created by sujkim on 2017-03-18.
 */
class TaskQuestionYN extends TaskUtil{

    @Override
    void run(){

        //Get Properties
        qman = new QuestionMan()
        QuestionSetup opt = genQuestionSetup()

        //Ask Question
        //Get Answer
        String yourAnswer = qman.question(opt, QuestionMan.QUESTION_TYPE_YN)

        //Remeber Answer
        rememberAnswerLineList.add("${propertyPrefix}answer.default=${yourAnswer}")

        //Set 'answer' and 'value' Property
        set('answer', yourAnswer)
        set('value', qman.getValue())

        //If 'answer' is Y
        if (yourAnswer?.toUpperCase()?.equals(QuestionMan.Y))
            setPropValue()

    }


}
