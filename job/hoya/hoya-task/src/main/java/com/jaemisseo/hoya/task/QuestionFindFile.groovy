package com.jaemisseo.hoya.task


import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.FileMan
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

    @Value("find.result.default")
    List<String> resultDefaultList

    @Value("find.result.edit.relpath")
    String editResultPath

    @Value(name='find.if', filter='parse')
    def searchIf

    @Value("mode.recursive")
    Boolean modeRecursive

    @Override
    Integer run(){
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

        List<File> itemList = []

        modeRecursive = (modeRecursive != null) ? modeRecursive : false

        /** Log - START **/
        logger.info " <Searching>"
        logger.info "  - Root Path      : $searchRootPath"
        logger.info "  - File Name      : $searchFileName"
        logger.info "  - Condition      : $searchIf"
        logger.info "  - Mode Recursive : $modeRecursive"
        logger.info "  - Result Path    : $editResultPath"
        logger.info ""

        /** New Thread - Finding Files... **/
        Thread threadSearcher = Util.newThread(' <Stoped Searching>      '){

            int defaultCount = 0
            if (modeRecursive){
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
            }else{
                //TODO: Not Good.. ==> must be updated with more more nice logic
                if (resultDefaultList){
                    resultDefaultList.each{ String defaultPath ->
                        println "  ${++defaultCount}) ${defaultPath}"
                        itemList << new File(defaultPath)
                    }
                }
                if (!searchRootPath)
                    searchRootPath = ''
                List<String> foundFilePathList = FileMan.getSubFilePathList(searchRootPath + '/*')
                foundFilePathList.eachWithIndex{ String foundFilePath, int index ->
                    int count = index + 1 + defaultCount
                    String editedPath = (editResultPath) ? FileMan.getFullPath(foundFilePath, editResultPath) : foundFilePath
                    println "  ${count}) ${editedPath}"
                    itemList << new File(editedPath)
                }
                logger.info "${foundFilePathList.size() + defaultCount} was founded."
                logger.info " <Finished Searching>"
            }
        }

        /** Ask Question **/
        //Get Answer
        String yourAnswer
        try{
            //Wait that all resultDefaultList is printed
            while (resultDefaultList && itemList.size() < resultDefaultList.size()){}
            //Question
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
