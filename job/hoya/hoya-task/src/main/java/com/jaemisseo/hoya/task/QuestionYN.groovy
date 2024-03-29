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
        //Get Properties
        QuestionMan qman = new QuestionMan().setValidAnswer([undoSign, redoSign])
        if (opt.questionColor){
            qman.setBeforeQuestionClosure{
                config.logGen.setupConsoleLoggerColorPattern(opt.questionColor)
            }
            qman.setAfterQuestionClosure{
                config.logGen.setupBeforeConsoleLoggerPattern()
            }
        }
        
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
        QuestionMan qman = new QuestionMan().setValidAnswer([undoSign, redoSign])
        return (!opt.modeOnlyInteractive) ? qman.genQuestionAndSelection(opt) : []
    }

}
