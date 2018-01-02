package install.task

import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.configuration.annotation.Value
import install.util.TaskUtil
import jaemisseo.man.FileMan
import jaemisseo.man.QuestionMan
import install.bean.QuestionSetup
import jaemisseo.man.util.Util

/**
 * Created by sujkim on 2017-03-18.
 */
@Undoable
@Task
class QuestionFindFile extends TaskUtil{

    @Value
    QuestionSetup opt

    @Value("find.root.path")
    String searchRootPath

    @Value(name="find.file.name", required=true)
    String searchFileName

    @Value("find.result.default")
    List<String> resultDefaultList

    @Value("find.result.edit.relpath")
    String editResultPath

    @Value(name='find.if', filter='parse')
    def searchIf



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

        List<File> itemList = []


        //Thread-Searcher - START
        logger.info " <Searching>"
        logger.info "  - Root Path   : $searchRootPath"
        logger.info "  - File Name   : $searchFileName"
        logger.info "  - Condition   : $searchIf"
        logger.info "  - Result Path : $editResultPath"
        logger.info ""

        Thread threadSearcher = Util.newThread(' <Stoped Searching>      '){
            int defaultCount = 0
            List<File> foundFileList = FileMan.findAllWithProgressBar(searchRootPath, searchFileName, searchIf) { data ->
                File foundFile = data.item
                if (!data.stringList && resultDefaultList){
                    resultDefaultList.each{ String defaultPath ->
                        data.stringList << "  ${++defaultCount}) ${defaultPath}"
                        itemList << new File(defaultPath)
                    }
                }
                int count = data.count + defaultCount
                String editedPath = (editResultPath) ? FileMan.getFullPath(foundFile.path, editResultPath) : foundFile.path
                data.stringList << "  ${count}) ${editedPath}"
                itemList << new File(editedPath)
                return true
            }
            logger.info "${foundFileList.size() + defaultCount} was founded."
            logger.info " <Finished Searching>"
        }

        //Ask Question
        //Get Answer
        String yourAnswer
        try{
            yourAnswer = qman.question(opt){ String answer, jaemisseo.man.bean.QuestionSetup option ->
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

        //Check undo & redo command
        if (checkUndoQuestion(yourAnswer))
            return STATUS_UNDO_QUESTION

        if (checkRedoQuestion(yourAnswer))
            return STATUS_REDO_QUESTION

        //Remeber 'answer'
        rememberAnswer(yourAnswer)

        //Set 'answer' and 'value' Property
        int seletedIndex = (Integer.parseInt(yourAnswer) -1)
        set('answer', yourAnswer)
        set('value', itemList[seletedIndex].path)

        //Set Some Property
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
