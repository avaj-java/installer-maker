package com.jaemisseo.hoya.task

import com.jaemisseo.hoya.util.FileFinderUtil
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.util.Util

/**
 * Created by sujkim on 2017-03-18.
 */
@Undoable
@Task
@TerminalValueProtocol(['find.root.path', 'find.file.name'])
class FindFile extends TaskHelper{

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

        FileFinderUtil.File fileFinder = new FileFinderUtil.File(
                searchRootPath: searchRootPath,
                searchFileName: searchFileName,
                resultDefaultList: resultDefaultList,
                searchIf: searchIf,
                editResultPath: editResultPath,
                editResultRefactoringPattern: editResultRefactoringPattern,
                editResultRefactoringResult: editResultRefactoringResult
        )

        /** (New Thread) Finding Files... **/
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
        try{
            waitUntilThreadIsDestroyed(threadSearcher)

        }catch(e){
            throw e
        }finally{
            //Thread-Searcher - STOP
            threadSearcher.interrupt()
        }

        /** After **/
//        int status = afterQuestion(answerFromUser, itemList)

        return STATUS_TASK_DONE
    }




    private static void waitUntilAllOfResultDefaultListIsPrinted(List<String> resultDefaultList, List<File> itemList){
        while (resultDefaultList && itemList.size() < resultDefaultList.size()){}
    }

    private static void waitUntilThreadIsDestroyed(Thread thread){
        while (thread.isAlive()){}
    }



}
