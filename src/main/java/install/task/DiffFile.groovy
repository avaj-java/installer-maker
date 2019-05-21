package install.task

import install.bean.FileSetup
import install.bean.ReportDiffFile
import install.bean.ReportSetup
import install.util.TaskUtil
import jaemisseo.man.CompareMan
import jaemisseo.man.FileMan
import jaemisseo.man.code.ChangeStatusCode
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.util.Util

@Task
class DiffFile extends TaskUtil{

    @Value
    FileSetup opt

    @Value(name="diff.before.root.dir", required=true)
    String beforePath

    @Value(name="diff.after.root.dir", required=true)
    String afterPath

    @Value("diff.result.root.dir")
    String resultPath

    @Value("mode.copy.new")
    Boolean modeCopyNew

    @Value("mode.copy.update")
    Boolean modeCopyUpdate

    @Value("mode.copy.none")
    Boolean modeCopyNone

    @Value("mode.copy.remove")
    Boolean modeCopyRemove

    @Value("mode.report")
    Boolean modeReport

    Map<String, ReportDiffFile> reportDataMap = [:]



    @Override
    Integer run(){
        List<File> itemList = []


        //Thread-Searcher - START
        logger.info " <Searching>"
        logger.info "  - Before Path   : $beforePath"
        logger.info "  - After Path    : $afterPath"
        logger.info "  - Result Path   : $resultPath"
        logger.info ""

        int defaultCount = 0

//        File beforeFile = new File(beforePath)
//        String rootPath = FileMan.getFullPath(beforeFile.path)
//        int rootPathLength = rootPath.length()
//        logger.debug "  - Before Path    : $beforeFile.path"
//        logger.debug "  - Before Name    : $beforeFile.name"
//        List<File> foundBeforeFileList = FileMan.findAllWithProgressBar(beforeFile.path, '**/*') { data ->
//            File foundFile = data.item
//            logger.debug "Before Version)  ${foundFile.path}"
//            return true
//        }
//        List<String> beforeEntries = foundBeforeFileList.collect{ File file -> file.path.substring(rootPathLength) }
//        beforeEntries.each{ logger.debug "  Change>>  ${it}" }
//        logger.info "${foundBeforeFileList.size() + defaultCount} was founded."

//        File afterFile = new File(afterPath)
//        rootPath = FileMan.getFullPath(afterFile.path)
//        rootPathLength = rootPath.length()
//        logger.debug "  - After Path    : $afterFile.path"
//        logger.debug "  - After Name    : $afterFile.name"
//        List<File> foundAfterFileList = FileMan.findAllWithProgressBar(afterFile.path, '**/*') { data ->
//            File foundFile = data.item
//            logger.debug "After Version)  ${foundFile.path}"
//            return true
//        }
//        List<String> afterEntries  = foundAfterFileList.collect{ File file -> file.path.substring(rootPathLength) }
//        afterEntries.each{ logger.debug "  Change>>  ${it}" }
//        logger.info "${foundAfterFileList.size() + defaultCount} was founded."




        /** Making Entiries for Before **/
        logger.info " <Start Collecting entries for BEFORE>"
        String baseRootDirForBefore = FileMan.getFullPath(beforePath)
        String baseRootPathsForBefore = baseRootDirForBefore + '/*'
        List<String> beforeEntryList = FileMan.getEntryList(baseRootPathsForBefore)
        if (logger.isTraceEnabled())
            beforeEntryList.each{ logger.trace "  - BEFORE Entries)  ${it}" }
        logger.info "${beforeEntryList.size()} was founded."
        logger.info " <Finished Collecting entries for BEFORE>\n"

        /** Making Entiries for After  **/
        logger.info " <Start Collecting entries for AFTER>"
        String baseRootDirForAfter = FileMan.getFullPath(afterPath)
        String baseRootPathsForAfter = baseRootDirForAfter + '/*'
        List<String> afterEntryList = FileMan.getEntryList(baseRootPathsForAfter)
        if (logger.isTraceEnabled())
            afterEntryList.each{ logger.trace "  - AFTER Entries)  ${it}" }
        logger.info "${afterEntryList.size()} was founded."
        logger.info " <Finished Collecting entries for AFTER>\n"

        /** Comparing and Recollecting **/
        logger.info " <Start Comparing BEFORE & AFTER>"
        List<String> newEntries = []
        List<String> updatedEntries = []
        List<String> noneUpdatedEntries = []
        List<String> removedEntries = []
        int seq = 0
        CompareMan.eachWIthProgressBar(beforeEntryList, afterEntryList){ Map progressBarData, ChangeStatusCode changeStatusCode ->
            String entryPath = progressBarData.item as String
            String errorMessage = ''
            try{
                if (changeStatusCode == ChangeStatusCode.NEW){
                    newEntries << entryPath
                }
                if (changeStatusCode == ChangeStatusCode.NONE){
                    String beforePath = baseRootDirForBefore + '/' + entryPath
                    String afterPath = baseRootDirForAfter + '/' + entryPath
                    if (FileMan.isChanged(beforePath, afterPath)){
                        updatedEntries << entryPath
                        changeStatusCode = ChangeStatusCode.MODIFIED
                    }else{
                        noneUpdatedEntries << entryPath
                    }
                }
                if (changeStatusCode == ChangeStatusCode.REMOVED){
                    removedEntries << entryPath
                }
//                logger.trace "  [${changeStatusCode}] >>  ${entryPath}"
                progressBarData.stringList << "  [${changeStatusCode}] >>  ${entryPath}"

            }catch(e){
                e.printStackTrace()
                progressBarData.stringList << "  [Error] >>  ${entryPath}"
                errorMessage = e.toString()
            }finally{
                //Make Report
                reportDataMap[entryPath] = new ReportDiffFile(
                        seq: ++seq,
                        sqlFileName: 'TEST',
                        changeStatusCode: changeStatusCode.toString(),
                        entryPath: entryPath,
                        error: errorMessage,
                )
            }
        }
        logger.info " <Finished Comparing BEFORE & AFTER>\n"

        /** Copy with ProgressBar **/
        int totalSize = newEntries.size() + updatedEntries.size() + noneUpdatedEntries.size() + removedEntries.size()
        int totalCopySize = (modeCopyNew ? newEntries.size() : 0) + (modeCopyUpdate ? updatedEntries.size() : 0) + (modeCopyNone ? noneUpdatedEntries.size() : 0) + (modeCopyRemove ? removedEntries.size() : 0)
        int barSize = 20
        logger.info " <Start Copying Changed Entries>   - TOTAL: ${totalCopySize}"
        Map progressBarData = Util.startPrinter(totalCopySize, barSize, true)
        newEntries.each{ String entryPath ->
            progressBarData.count++
            progressBarData.item = entryPath
            doCopy(baseRootDirForAfter, resultPath, entryPath, modeCopyNew, reportDataMap[entryPath])
        }
        updatedEntries.each{ String entryPath ->
            progressBarData.count++
            progressBarData.item = entryPath
            doCopy(baseRootDirForAfter, resultPath, entryPath, modeCopyUpdate, reportDataMap[entryPath])
        }
        noneUpdatedEntries.each{ String entryPath ->
            progressBarData.count++
            progressBarData.item = entryPath
            doCopy(baseRootDirForAfter, resultPath, entryPath, modeCopyNone, reportDataMap[entryPath])
        }
        removedEntries.each{ String entryPath ->
            progressBarData.count++
            progressBarData.item = entryPath
            doCopy(baseRootDirForBefore, resultPath, entryPath, modeCopyRemove, reportDataMap[entryPath])
        }
        Util.endWorker(progressBarData)
        logger.info " <Finished Copying Changed Entries>\n"

        return STATUS_TASK_DONE
    }


    private boolean doCopy(String fromRootPath, String toRootPath, String entryPath, Boolean modeCopy, ReportDiffFile reportObj){
        String from = fromRootPath + '/' + entryPath
        String to = toRootPath + '/' + entryPath
        //Checking File size
        if (!new File(from).isDirectory())
            reportObj.fileSize = new File(from).length()
        //Copy
        if (modeCopy){
            copy(from, to)
            reportObj.copied = true
        }
        //Making Warning Message
        if (reportObj.changeStatusCode == ChangeStatusCode.REMOVED.toString()){
            reportObj.warn += " File is removed."
        }else if (reportObj.fileSize == '0'){
            reportObj.warn += " File size is zero."
        }
        return true
    }


    /**
     * COPY
     */
    private boolean copy(String from, String to){
        if (new File(from).isDirectory()){
            return FileMan.mkdirs(to)
        }else{
            return FileMan.copy(from, to, opt)
        }
    }



    @Override
    void reportWithExcel(ReportSetup reportSetup, List reportMapList){
        List<ReportDiffFile> reportList = reportDataMap.collect{ it.value }
        reportMapList.addAll(reportList)
    }

}

