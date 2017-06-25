package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.type.Undoable
import install.configuration.annotation.Value
import install.util.TaskUtil
import jaemisseo.man.QuestionMan
import jaemisseo.man.util.QuestionSetup

/**
 * Created by sujkim on 2017-03-18.
 */
@Undoable
@Task
class QuestionYN extends TaskUtil{

    @Value(method='genMergedQuestionSetup')
    QuestionSetup opt



    @Override
    Integer run(){
        //Get Properties
        qman = new QuestionMan().setValidAnswer([undoSign, redoSign])

        //Ask Question
        //Get Answer
        String yourAnswer = qman.question(opt, QuestionMan.QUESTION_TYPE_YN)

        //Check undo & redo command
        if (checkUndoQuestion(yourAnswer))
            return STATUS_UNDO_QUESTION

        if (checkRedoQuestion(yourAnswer))
            return STATUS_REDO_QUESTION

        //Remeber Answer
        rememberAnswer(yourAnswer)

        //Set 'answer' and 'value' Property
        set('answer', yourAnswer)
        set('value', qman.getValue())

        //If 'answer' is Y
        if (yourAnswer?.toUpperCase()?.equals(QuestionMan.Y))
            setPropValue()

        return STATUS_TASK_DONE
    }



    /**
     * BUILD FORM
     */
    List<String> buildForm(String propertyPrefix){
        this.propertyPrefix = propertyPrefix
        qman = new QuestionMan().setValidAnswer([undoSign, redoSign])
        return (!opt.modeOnlyInteractive) ? qman.genQuestion(opt) : []
    }

}
