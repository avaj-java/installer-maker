package com.jaemisseo.hoya.task


import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.TerminalIgnore
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.QuestionMan
import com.jaemisseo.hoya.bean.QuestionSetup

/**
 * Created by sujkim on 2017-03-18.
 */
@Undoable
@Task
@TerminalIgnore
class QuestionYN extends TaskHelper{

    @Value
    QuestionSetup opt



    @Override
    Integer run(){

        /** Before **/
        QuestionMan qman = beforeQuestion()

        /** Ask Question **/
        String answerFromUser = qman.question(opt, QuestionMan.QUESTION_TYPE_YN)

        /** After **/
        String value = qman.getValue()
        int status = afterQuestion(answerFromUser, value)

        return status
    }



    private QuestionMan beforeQuestion(){
        QuestionMan qman = new QuestionMan().setValidAnswer([undoSign, redoSign])
        if (opt.questionColor){
            qman.setBeforeQuestionClosure{
                config.logGen.setupConsoleLoggerColorPattern(opt.questionColor)
            }
            qman.setAfterQuestionClosure{
                config.logGen.setupBeforeConsoleLoggerPattern()
            }
        }

        return qman
    }

    private int afterQuestion(String answerFromUser, String value){
        //Check undo & redo command
        if (checkUndoQuestion(answerFromUser))
            return STATUS_UNDO_QUESTION

        if (checkRedoQuestion(answerFromUser))
            return STATUS_REDO_QUESTION

        //Remeber Answer
        rememberAnswer(answerFromUser)

        //Set 'answer' and 'value' Property
        set('answer', answerFromUser)
        set('value', value)

        //If 'answer' is Y
        if (answerFromUser?.toUpperCase()?.equals(QuestionMan.Y))
            setPropValue()

        return STATUS_TASK_DONE
    }



    /**
     * BUILD FORM
     */
    List<String> buildForm(String propertyPrefix){
        QuestionMan qman = new QuestionMan().setValidAnswer([undoSign, redoSign])
        return (!opt.modeOnlyInteractive) ? qman.genQuestionAndSelection(opt) : []
    }

}
