package com.jaemisseo.hoya.task

import com.jaemisseo.hoya.util.FileFinderUtil
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.QuestionMan
import com.jaemisseo.hoya.bean.QuestionSetup
import jaemisseo.man.util.Util

/**
 * Created by sujkim on 2017-03-18.
 */
@Undoable
@Task
class QuestionFindFile extends TaskHelper{

    @Value
    QuestionSetup opt

    @Value("find.root.path")
    String searchRootPath

    @Value(name="find.file.name", required=true)
    String searchFileName

    @Value(name='find.if', filter='parse')
    def searchIf

    @Value("find.result.default")
    List<String> resultDefaultList

    @Value("find.result.edit.relpath")
    String editResultPath

    @Value("find.result.edit.refactoring.pattern")
    String editResultRefactoringPattern

    @Value("find.result.edit.refactoring.result")
    String editResultRefactoringResult

    @Value("mode.recursive")
    Boolean modeRecursive


    @Override
    Integer run(){

        /** Before **/
        //- Setup
        List<File> itemList = []
        modeRecursive = (modeRecursive != null) ? modeRecursive : false
        QuestionMan qman = beforeQuestion()

        /** (New Thread) Finding Files... **/
        FileFinderUtil.File fileFinder = new FileFinderUtil.File(
                searchRootPath: searchRootPath,
                searchFileName: searchFileName,
                resultDefaultList: resultDefaultList,
                searchIf: searchIf,
                editResultPath: editResultPath,
                editResultRefactoringPattern: editResultRefactoringPattern,
                editResultRefactoringResult: editResultRefactoringResult
        )

        Thread threadSearcher = Util.newThread(' <Stoped Searching>      '){
            if (modeRecursive){
                FileFinderUtil.collectFileRecursivley(itemList, fileFinder)
            }else{
                FileFinderUtil.collectFile(itemList, fileFinder)
            }
            int defaultCount = resultDefaultList?.size() ?: 0
            logger.info "${itemList.size() + defaultCount} was founded."
            logger.info " <Finished Searching>"
        }

        /** (Main Thread) Ask Question **/
        String answerFromUser = null
        try{
            //Wait that all resultDefaultList is printed
            waitUntilAllOfResultDefaultListIsPrinted(resultDefaultList, itemList)

            //Question
            answerFromUser = qman.question(opt){ String answer, jaemisseo.man.bean.QuestionSetup option ->
                if (answer.isNumber()){
                    int answerNum = Integer.parseInt(answer)
                    return (itemList.size() >= answerNum && answerNum >= 1)
                }
                return false
            }

        }catch(e){
            throw e
        }finally{
            //Thread-Searcher - STOP
            threadSearcher.interrupt()
        }

        /** After **/
        int seletedIndex = (Integer.parseInt(answerFromUser) -1)
        String value = itemList[seletedIndex].path
        int status = afterQuestion(answerFromUser, value)

        return status
    }



    private QuestionMan beforeQuestion(){
        /** Get Properties **/
        QuestionMan qman = new QuestionMan().setValidAnswer([undoSign, redoSign])
        if (opt.questionColor){
            qman.setBeforeQuestionClosure{
                config.logGen.setupConsoleLoggerColorPattern(opt.questionColor)
            }
            qman.setAfterQuestionClosure{
                config.logGen.setupBeforeConsoleLoggerPattern()
            }
        }

        /** Log - START **/
        logger.info " <Searching>"
        logger.info "  - Root Path      : $searchRootPath"
        logger.info "  - File Name      : $searchFileName"
        logger.info "  - Condition      : $searchIf"
        logger.info "  - Mode Recursive : $modeRecursive"
        logger.info "  - Result Path    : $editResultPath"
        logger.info "  - Result Refactoring Pattern : $editResultRefactoringPattern"
        logger.info "  - Result Refactoring Result  : $editResultRefactoringResult"
        logger.info ""

        return qman
    }

    private int afterQuestion(String answerFromUser, String value){
        //Check undo & redo command
        if (checkUndoQuestion( answerFromUser ))
            return STATUS_UNDO_QUESTION

        if (checkRedoQuestion( answerFromUser ))
            return STATUS_REDO_QUESTION

        //Remeber 'answer'
        rememberAnswer( answerFromUser )

        //Set 'answer' and 'value' Property
        set('answer', answerFromUser)
        set('value', value)

        //Set Some Property
        setPropValue()

        return STATUS_TASK_DONE
    }



    private static waitUntilAllOfResultDefaultListIsPrinted(List<String> resultDefaultList, List<File> itemList){
        while (resultDefaultList && itemList.size() < resultDefaultList.size()){}
    }


    /**
     * BUILD FORM
     */
    List<String> buildForm(String propertyPrefix){
        QuestionMan qman = new QuestionMan().setValidAnswer([undoSign, redoSign])
        return (!opt.modeOnlyInteractive) ? qman.genQuestionAndSelection(opt) : []
    }

}
