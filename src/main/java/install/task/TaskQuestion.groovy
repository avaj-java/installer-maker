package install.task

import com.jaemisseo.man.QuestionMan
import com.jaemisseo.man.util.QuestionSetup

/**
 * Created by sujkim on 2017-03-18.
 */
class TaskQuestion extends TaskUtil{

    @Override
    Integer run(){

        //Get Properties
        qman = new QuestionMan().setValidAnswer([undoSign, redoSign])
        QuestionSetup opt = genQuestionSetup()

        //Ask Question
        //Get Answer
        String yourAnswer = qman.question(opt)

        //Check undo & redo command
        if (checkUndoQuestion(yourAnswer))
            return STATUS_UNDO_QUESTION
        else if (checkRedoQuestion(yourAnswer))
            return STATUS_REDO_QUESTION

        //Remeber 'answer'
        rememberAnswerLineList.add("${propertyPrefix}answer.default=${yourAnswer}")

        //Set 'answer' and 'value' Property
        set('answer', yourAnswer)
        set('value', qman.getValue())

        //Set Some Property
        setPropValue()

        return STATUS_TASK_DONE
    }

}
