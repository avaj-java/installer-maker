package install.task

import install.annotation.Task
import install.annotation.Value
import install.util.TaskUtil
import jaemisseo.man.FileMan
import jaemisseo.man.QuestionMan
import jaemisseo.man.util.QuestionSetup
import jaemisseo.man.util.Util

/**
 * Created by sujkim on 2017-03-18.
 */
@Task
class QuestionFindFile extends TaskUtil{

    @Value(method='genMergedQuestionSetup')
    QuestionSetup opt

    @Value("find.root.path")
    String searchRootPath

    @Value("find.file.name")
    String searchFileName

    @Value("find.result.edit.relpath")
    String editResultPath

    @Value(property='find.if', method='parse')
    def searchIf



    @Override
    Integer run(){

        //Get Properties
        qman = new QuestionMan().setValidAnswer([undoSign, redoSign])

        List<File> itemList = []


        //Thread-Searcher - START
        println "FIND Root Path: $searchRootPath"
        println "FIND File Name: $searchFileName"
        println "FIND Condition: $searchIf"

        println " <Searching>"
        Thread threadSearcher = Util.newThread(' <Stoped Searching>      '){
            List<File> foundFileList = FileMan.findAllWithProgressBar(searchRootPath, searchFileName, searchIf) { data ->
                File foundFile = data.item
                int count = data.count
                String editedPath = (editResultPath) ? FileMan.getFullPath(foundFile.path, editResultPath) : foundFile.path
                data.stringList << "  ${count}) ${editedPath}"
                itemList << new File(editedPath)
                return true
            }
            println "${foundFileList.size()} was founded."
            println " <Finished Searching>"
        }

        //Ask Question
        //Get Answer
        String yourAnswer
        try{
            yourAnswer = qman.question(opt){ String answer, QuestionSetup option ->
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
        else if (checkRedoQuestion(yourAnswer))
            return STATUS_REDO_QUESTION

        //Remeber 'answer'
        rememberAnswerLineList.add("${propertyPrefix}answer.default=${yourAnswer}")

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
        this.propertyPrefix = propertyPrefix
        qman = new QuestionMan().setValidAnswer([undoSign, redoSign])
        return (!opt.modeOnlyInteractive) ? qman.genQuestion(opt) : []
    }

}
