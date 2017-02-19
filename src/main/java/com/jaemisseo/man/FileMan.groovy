package com.jaemisseo.man

import com.jaemisseo.man.util.FileSetup

import java.util.logging.Logger

/**
 * Created by sujkim on 2017-02-19.
 */
class FileMan {

    FileMan(){}

    private static final Logger log = Logger.getLogger(FileMan.class.getName());

    boolean directoryAutoCreateUse = true





    /**
     * Process - Backup
     */
    boolean backupFiles(String dirPathToSave, String dirPathToBackup) throws Exception{
        backupFiles(dirPathToSave, dirPathToBackup, [], [])
    }

    boolean backupFiles(String dirPathToSave, String dirPathToBackup, List deleteExcludeList, List moveExcludeList) throws Exception{
        File dirForSave = new File(dirPathToSave)
        File dirForBackup = new File(dirPathToBackup)
        // 3-1. 기존 백업파일 지우기
        if (dirForBackup.exists())
            emptyDirectory(dirPathToBackup, deleteExcludeList)
        log.info('\n < FINISHED Delete > Backup Files')
        // 3-2. 디렉토리 체크 or 자동생성
        if (directoryAutoCreateUse){
            if ( !dirForSave.exists() && dirForSave.mkdirs() )
                log.info('\n < AUTO CREATE Directory > Save Directory')
            if ( !dirForBackup.exists() && dirForBackup.mkdirs() )
                log.info('\n < AUTO CREATE Directory > Backup Directory')
        }
        // 3-3. 기존 파일들을 -> 백업 디렉토리로 이동
        if ( dirForSave.exists() && dirForBackup.exists() ){
            log.info('\n < CHECK Directory > OK')
            if ( !moveAllInDirectory(dirPathToSave, dirPathToBackup, moveExcludeList) )
                throw new Exception('\n[MOVE Failed] Move Exist FIles To Backup Directory', new Throwable("You Need To Check Permission Check And... Some... "))
        }else{
            throw new Exception('\n[CHECK Failed] Some Directory Does Not Exists', new Throwable("Directory Exist Check - SaveDirectory:${dirForSave.exists()} / BackupDirectory:${dirForBackup.exists()}"))
        }
        log.info('\n < FINISHED Move > Move Exist FIles To Backup Directory')
        return true
    }

    /**
     * Create New File By LineList
     */
    boolean createNewFile(String dirPath, String fileName, List fileContentLineList){
        createNewFile(dirPath, fileName, fileContentLineList, new FileSetup())
    }

    boolean createNewFile(String dirPath, String fileName, List fileContentLineList, FileSetup fileSetup){
        // 1) Set Values
        File newFile = new File(dirPath, fileName)
        File dir = new File(newFile.getParent())
        String encoding = fileSetup.encoding
        Boolean autoLineBreakUse = fileSetup.autoLineBreakUse
        String lineBreak = fileSetup.lineBreak
        String lastLineBreak = fileSetup.lastLineBreak
        // 2) Check Directory And Try To Create Directory
        if ( !dir.exists() ){
            dir.mkdirs()
            if ( !dir.exists() )
                throw new Exception("\n < CREATE Failed > Directory To Save File Could Not be Created", new Throwable("You Need To Check Permission Check And... Some... "))
        }
        // 3) Generate File
        try{
            newFile.withWriter(encoding){ out ->
                // METHOD A. Auto LineBreak
                if (autoLineBreakUse)
                    fileContentLineList.each{ String oneLine -> out.println oneLine }
                // METHOD B. Custom LineBreak
                else
                    out.print ( fileContentLineList.join(lineBreak) + ((lastLineBreak) ? lastLineBreak:'') )
            }
            log.info("\n - Complete - Create ${newFile.path}")
        }catch(Exception e){
            log.info("\n - Failed - To Create ${newFile.path}")
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * 특정 디렉토리의 모든 파일들을 -> 지우기
     */
    boolean emptyDirectory(String dirPathToDelete, List excludePathList) throws Exception{
        File dirToDelete = new File(dirPathToDelete)
        dirToDelete.listFiles().each{ File fileToDelete ->
            // Delete
            if ( !isExcludeFile(fileToDelete, excludePathList) ){
                if (fileToDelete.isDirectory()){
                    rmdir(fileToDelete, excludePathList)
                }else{
                    if (fileToDelete.delete())
                        log.info("\n - Complete - Delete ${fileToDelete.path}")
                    else
                        log.info("\n - Failed - To Delete ${fileToDelete.path}")
                }
            }
        }
        return true
    }

    /**
     * 특정 디렉토리을 -> 완전 지우기
     */
    boolean rmdir(final File dirToDelete, List excludePathList) {
        if (dirToDelete.isDirectory() && !isExcludeFile(dirToDelete, excludePathList) ){
            // Delete Sub
            dirToDelete.listFiles().each{ File fileToDelete ->
                // Delete File
                if ( !isExcludeFile(fileToDelete, excludePathList) ){
                    if (fileToDelete.isDirectory()){
                        rmdir(fileToDelete, excludePathList)
                    }else{
                        if (fileToDelete.delete())
                            log.info("\n - Complete - Delete ${fileToDelete.path}")
                        else
                            log.info("\n - Failed - To Delete ${fileToDelete.path}")
                    }
                }
            }
            // Delete Empty Directory
            if (dirToDelete.delete())
                log.info("\n - Complete - Delete ${dirToDelete.path}")
            else
                log.info("\n - Failed - To Delete ${dirToDelete.path}")
        }
        return true
    }

    /**
     * 특정 디렉토리의 모든 파일들을 -> 다른 특정 디렉토리로 옴기기
     */
    boolean moveAllInDirectory(String beforeDirPath, String afterDirPath, List excludePathList) throws Exception{
        File beforeDir = new File(beforeDirPath)
        beforeDir.listFiles().each{ File fileToMove ->
            // Move
            if ( !isExcludeFile(fileToMove, excludePathList) ){
                File afterFile  = new File(afterDirPath, fileToMove.name)
                if (fileToMove.renameTo(afterFile)){
                    log.info("\n - Complete - Move ${fileToMove.path} To ${afterFile.path}")
                }
            }
        }
        return true
    }

    /**
     * Check Exclude File
     */
    boolean isExcludeFile(File targetFile, List<String> excludePathList){
        List excludeCheckList = excludePathList.findAll{ String excludeItem ->
            File excludeFile = new File(excludeItem)
            return targetFile.path.equals(excludeFile.path)
        }
        return excludeCheckList;
    }

    /**
     * Seperator with comma or space
     */
    List<String> toList(String itemsSeperatedWithCommaOrSpace){
        List<String> itemList = (itemsSeperatedWithCommaOrSpace) ? itemsSeperatedWithCommaOrSpace.replaceAll('[,]', ' ').split(/\s{1,}/) : []
        return itemList
    }

    /**
     * 특정 필드로 LIST 조각내서 Map에 담기
     * List -> Map<String, List> by Key
     */
    Map toMap(List dataList, String keyName){
        Map<String, List> fileMapForSeperator = [:]
        // 4-1. fileMapForSeperator에 특정 필드로 분류해서 List로서 저장
        dataList.each{
            String seperator = it[keyName]
            if (!fileMapForSeperator[seperator])
                fileMapForSeperator[seperator] = []
            fileMapForSeperator[seperator] << it
        }
        return fileMapForSeperator
    }

    /**
     * 특정 필드로 LIST 조각내서 Map에 담기
     * List -> Map<String, List> by Key
     */
    Map toMap(List dataList, String keyName, List<String> validKeyList){
        Map<String, List> fileMapForSeperator = [:]
        // 4-1. fileMapForSeperator에 특정 필드로 분류해서 List로서 저장
        validKeyList.each{ String validKey ->
            fileMapForSeperator[validKey] = dataList.findAll{ it[keyName] == validKey }
        }
        return fileMapForSeperator
    }

}
