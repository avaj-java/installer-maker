package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.type.Undoable
import install.configuration.annotation.Value
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

    @Value("find.result.edit.relpath")
    String editResultPath

    @Value(name='find.if', filter='parse')
    def searchIf



    @Override
    Integer run(){
        //Get Properties
        qman = new QuestionMan().setValidAnswer([undoSign, redoSign])

        List<File> itemList = []


        //Thread-Searcher - START
        println " <Searching>"
        println "Root Path: $searchRootPath"
        println "File Name: $searchFileName"
        println "Condition: $searchIf"
        println ""

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
        this.propertyPrefix = propertyPrefix
        qman = new QuestionMan().setValidAnswer([undoSign, redoSign])
        return (!opt.modeOnlyInteractive) ? qman.genQuestion(opt) : []
    }

}
