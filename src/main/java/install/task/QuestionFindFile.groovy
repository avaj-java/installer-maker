package install.task

import install.TaskUtil
import jaemisseo.man.FileMan
import jaemisseo.man.QuestionMan
import jaemisseo.man.util.QuestionSetup
import jaemisseo.man.util.Util

/**
 * Created by sujkim on 2017-03-18.
 */
class QuestionFindFile extends TaskUtil{

    @Override
    Integer run(){

        //Get Properties
        qman = new QuestionMan().setValidAnswer([undoSign, redoSign])
        QuestionSetup opt = genMergedQuestionSetup()

        List<File> itemList = []
        String searchRootPath   = get("find.root.path")
        String searchFileName   = get("find.file.name")
        String editResultPath   = get("find.result.edit.relpath")
        def searchIf            = parse("find.if")

        //Thread-Searcher - START
        println " <Searching>"
        Thread threadSearcher = Util.newThread(' <Stoped Searching>      '){
            int i = 0;
            List<File> foundFileList = FileMan.findAll(searchRootPath, searchFileName, searchIf) { File foundFile ->
                String editedPath = (editResultPath) ? FileMan.getFullPath(foundFile.path, editResultPath) : foundFile.path
                println "${++i}) ${editedPath}"
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
        QuestionSetup opt = genMergedQuestionSetup()

        return (!opt.modeOnlyInteractive) ? qman.genQuestion(opt) : []
    }

}
