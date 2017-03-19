package com.jaemisseo.man

import com.jaemisseo.man.util.FileSetup
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream

import java.nio.channels.FileChannel
import java.util.logging.Logger
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Created by sujkim on 2017-02-19.
 */
class FileMan {

    FileMan(){}

    FileMan(String filePath){
        init()
        setSource(filePath)
    }

    FileMan(File file){
        init()
        setSource(file)
    }

    void init(){
        nowPath = System.getProperty('user.dir')
    }

    private static final Logger log = Logger.getLogger(FileMan.class.getName());

    boolean directoryAutoCreateUse = true

    final static int BUFFER = 2048
    static String nowPath = System.getProperty('user.dir')

    File sourceFile

    FileSetup globalOption = new FileSetup()

    String originalContent
    String content



    FileMan set(FileSetup fileSetup){
        globalOption.merge(fileSetup)
        return this
    }

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
     * Read File
     */
    private boolean loadFileContent(String filePath){
        return loadFileContent(new File(filePath))
    }

    private boolean loadFileContent(String filePath, FileSetup opt){
        return loadFileContent(new File(filePath), opt)
    }

    private boolean loadFileContent(File f){
        return loadFileContent(f, globalOption)
    }

    private List<String> loadFileContent(File f, FileSetup opt){
        opt = globalOption.clone().merge(opt)
        String encoding = opt.encoding
        List<String> lineList = new ArrayList<String>()
        String line
        try {
//            FileReader fr = new FileReader(f)
            InputStreamReader isr = new InputStreamReader(new FileInputStream(f), encoding)
            BufferedReader br = new BufferedReader(isr)
            while ((line = br.readLine()) != null) {
                lineList.add(line)
            }
//            fr.close()
            isr.close()
            br.close()

        } catch (Exception ex) {
            ex.printStackTrace()
        }

        return lineList
    }


    /**
     * Create New File By LineList
     */
    boolean createNewFile(String dirPath, String fileName, List fileContentLineList){
        createNewFile(dirPath, fileName, fileContentLineList, new FileSetup())
    }

    boolean createNewFile(String dirPath, String fileName, List fileContentLineList, FileSetup fileSetup){
        createNewFile(new File(dirPath, fileName), fileContentLineList, fileSetup)
    }

    boolean createNewFile(String filePath, List fileContentLineList){
        createNewFile(filePath, fileContentLineList, new FileSetup())
    }

    boolean createNewFile(String filePath, List fileContentLineList, FileSetup fileSetup){
        createNewFile(new File(filePath), fileContentLineList, fileSetup)
    }

    boolean createNewFile(File file, List fileContentLineList){
        createNewFile(file, fileContentLineList, new FileSetup())
    }

    boolean createNewFile(File newFile, List fileContentLineList, FileSetup opt){
        opt = globalOption.clone().merge(opt)
        // 1) Set Values
        File dir = new File(newFile.getParent())
        String encoding = opt.encoding
        Boolean autoLineBreakUse = opt.modeAutoLineBreak
        String lineBreak = opt.lineBreak
        String lastLineBreak = opt.lastLineBreak
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





    /***************
     ***************
     *
     * CASE STATIC
     *
     ***************
     ***************/



    /**
     * MKDIRS
     */
    static boolean mkdirs(String path){
        boolean isOk = false
        File dir = new File(path)
        if (!dir.exists()){
            isOk = new File(path).mkdirs()
            println "Created Directory: ${dir.path}"
        }
        return isOk
    }

    static boolean mkdirs(String path, Map buildStructureMap){
        buildStructureMap.each{
            //Make Directory
            String directoryName = it.key
            String dirPath = getFullPath("${path}/${directoryName}")
            mkdirs(dirPath)
            //Make Sub Directory
            Map subDirectoryMap = it.value
            mkdirs(dirPath, subDirectoryMap)
        }
    }

    static boolean autoMkdirs(String destPath){
        String destDirPath
        //Define Last Directory
        // - 끝이 구분자로 끝나면 => 폴더로 인식
        if (destPath.endsWith('/')|| destPath.endsWith('\\'))
            destDirPath = destPath
        // - 확장자가 존재하면 => 부모를 폴더르 인식
        else if (new File(destPath).getName().contains('.'))
            destDirPath = new File(destPath).getParentFile().getPath()
        // - 그외에는 무조건 폴더로 인식
        else
            destDirPath = destPath
        //Do AUTO MKDIR
        FileMan.mkdirs(destDirPath)
        //Check Directory
        if (!new File(destDirPath).isDirectory() && !new File(destDirPath).exists())
            throw new Exception('There is No Directory. Maybe, It Failed To Create Directory.')
    }


    /**
     * COPY
     * 파일 => 파일 (파일명변경)
     * *   => 폴더   (자동파일명)
     * 파일 => 폴더  (자동파일명)
     * 폴더 => 폴더  (자동파일명)
     */
    static boolean copy(String sourcePath, String destPath){
        sourcePath = getFullPath(sourcePath)
        destPath = getFullPath(destPath)
        if (!sourcePath || !destPath)
            return false
        File originalSourceFile = new File(sourcePath)
        File originalDestFile = new File(destPath)
        boolean isFileSource = originalSourceFile.getName().contains('.')
        boolean isFileDest = originalDestFile.getName().contains('.')
        String sourceRootPath = originalSourceFile.getParentFile().getPath()
        String destRootPath = (isFileDest) ? originalDestFile.getParentFile().getPath() : originalDestFile.getPath()
        List<String> filePathList = getFilePathList(sourcePath)
        List entryList = []
        //Generate EntryList
        filePathList.each{ String oneFilePath ->
            generateFileList(entryList, sourceRootPath, oneFilePath)
        }
        //Copy To Dest
        if (isFileSource && isFileDest){
            File sourceFile = new File(sourcePath)
            File destFile = new File(destPath)
            copy(sourceFile, destFile)
        }else{
            for (String fileRelPath : entryList){
                File sourceFile = new File(sourceRootPath, fileRelPath)
                File destFile = new File(destRootPath, fileRelPath)
                copy(sourceFile, destFile)
            }
        }
        System.out.println("<Done>")
        return true
    }

    static boolean copy(File sourceFile, File destFile){
        if (sourceFile.isDirectory()){
            mkdirs(destFile.path)
        }else{
            FileChannel sourceChannel = null
            FileChannel destChannel = null
            try{
                sourceChannel = new FileInputStream(sourceFile.path).getChannel()
                destChannel = new FileOutputStream(destFile.path).getChannel()
                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size())
                println "Copied: ${sourceFile.path}"
                println "     => ${destFile.path}"
            }catch (Exception e){
                e.printStackTrace()
                throw e
            }finally{
                if (sourceChannel)
                    sourceChannel.close()
                if (destChannel)
                    destChannel.close()
            }
        }
        return true
    }

    static boolean copy(String sourcePath, String destPath, boolean modeAutoMkdir){
        if (modeAutoMkdir)
            autoMkdirs(destPath)
        copy(sourcePath, destPath)
    }

    /**
     * COMPRESSING
     */
    static void compress(String sourcePath, String destPath){
        String fileName = new File(destPath).getName()
        String extension = fileName.substring(fileName.lastIndexOf('.'))
        if (extension){
            if (extension == 'tar')
                tar(sourcePath, destPath)
            else
                zip(sourcePath, destPath)
        }
    }

    static boolean compress(String sourcePath, String destPath, boolean modeAutoMkdir){
        if (modeAutoMkdir)
            autoMkdirs(destPath)
        compress(sourcePath, destPath)
    }

    static void zip(String sourcePath, String destPath){
        sourcePath = getFullPath(sourcePath)
        destPath = getFullPath(destPath)
        String sourceRootPath = new File(sourcePath).getParentFile().getPath()
        List<String> filePathList = getFilePathList(sourcePath)
        byte[] buffer = new byte[BUFFER]
        List entryList = []
        //Generate EntryList
        filePathList.each{ String oneFilePath ->
            generateFileList(entryList, sourceRootPath, oneFilePath)
        }
        //Compress Zip
        try{
            FileOutputStream fos = new FileOutputStream(destPath)
            ZipOutputStream zos = new ZipOutputStream(fos)
            for (String file : entryList){
                println("Compressing: ${file}")
                zos.putNextEntry(new ZipEntry(file))
                String path = sourceRootPath + File.separator + file
                if (new File(path).isFile()){
                    FileInputStream fin = new FileInputStream(path)
                    int len
                    while ((len = fin.read(buffer)) > 0){
                        zos.write(buffer, 0, len)
                    }
                    fin.close()
                }
            }
            zos.closeEntry()
            zos.close()
            System.out.println("<Done>")
        }catch(IOException ex){
            ex.printStackTrace()
        }
    }

    static boolean zip(String sourcePath, String destPath, boolean modeAutoMkdir){
        if (modeAutoMkdir)
            autoMkdirs(destPath)
        zip(sourcePath, destPath)
    }

    static List<String> generateFileList(List<String> entryList, String sourcePath, String path){
        File node = new File(path)
        String oneFilePath = node.getPath().substring(sourcePath.length()+1, path.length())
        if(node.isDirectory()){
            String[] subNote = node.list()
            entryList.add(oneFilePath + System.getProperty('file.separator'))
            for (String filename : subNote){
                generateFileList(entryList, sourcePath, new File(node, filename).getPath())
            }
        }else{
            entryList.add(oneFilePath)
        }
        return entryList
    }

    static void tar(String filePath, String destPath){
        filePath = getFullPath(filePath)
        destPath = getFullPath(destPath)
    }

    static boolean tar(String sourcePath, String destPath, boolean modeAutoMkdir){
        if (modeAutoMkdir)
            autoMkdirs(destPath)
        tar(sourcePath, destPath)
    }



    /**
     * EXTRACTING
     */
    static void extract(String filePath, String destPath){
        String fileName = new File(filePath).getName()
        String extension = fileName.substring(fileName.lastIndexOf('.'))
        if (extension){
            if (extension == 'tar')
                untar(filePath, destPath)
            if (extension == 'jar')
                unjar(filePath, destPath)
            else
                unzip(filePath, destPath)
        }
    }

    static boolean extract(String sourcePath, String destPath, boolean modeAutoMkdir){
        if (modeAutoMkdir)
            autoMkdirs(destPath)
        extract(sourcePath, destPath)
    }



    /**
     * Extract Tar File
     * @param sourcePath
     * @param destPath
     */
    static void untar(String sourcePath, String destPath){
        sourcePath = getFullPath(sourcePath)
        destPath = getFullPath(destPath)
        List<String> filePathList = getFilePathList(sourcePath)

        filePathList.each {
            byte[] buffer = new byte[BUFFER]
            try {
                /** Ready **/
                FileInputStream fin = new FileInputStream(sourcePath)
                BufferedInputStream bis = new BufferedInputStream(fin)
                GzipCompressorInputStream gzIn = new GzipCompressorInputStream(bis)
                TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn)
                TarArchiveEntry entry
                /** Read the tar entries using the getNextEntry method **/
                while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                    File file = new File(destPath, entry.getName())
                    println "Extracting: ${file.getAbsolutePath()}"
                    if (entry.isDirectory()) {
                        file.mkdirs()
                    } else {
                        file.parentFile.mkdirs()
                        int len
                        FileOutputStream fos = new FileOutputStream(file)
                        BufferedOutputStream destOs = new BufferedOutputStream(fos, BUFFER)
                        while ((len = tarIn.read(buffer, 0, BUFFER)) != -1) {
                            destOs.write(buffer, 0, len)
                        }
                        destOs.close()
                    }
                }
                /** Close the input stream **/
                tarIn.close()
                println "Untar Completed Successfully!!"

            } catch (IOException ex) {
                ex.printStackTrace()
            }
        }
    }

    static boolean untar(String sourcePath, String destPath, boolean modeAutoMkdir){
        if (modeAutoMkdir)
            autoMkdirs(destPath)
        untar(sourcePath, destPath)
    }

    /**
     * Extract Zip File
     * @param sourcePath
     * @param destPath
     */
    static void unzip(String sourcePath, String destPath){
        sourcePath = getFullPath(sourcePath)
        destPath = getFullPath(destPath)
        List<String> filePathList = getFilePathList(sourcePath)

        filePathList.each{
            byte[] buffer = new byte[BUFFER]
            try{
                /** Ready **/
                ZipInputStream zis = new ZipInputStream(new FileInputStream(sourcePath))
                ZipEntry entry
                /** Read the zip entries using the getNextEntry method **/
                while ((entry = zis.getNextEntry()) != null){
                    File file = new File(destPath + File.separator + entry.getName())
                    println "Extracting: ${file.getAbsolutePath()}"
                    if (entry.isDirectory()){
                        file.mkdirs()
                    }else{
                        file.parentFile.mkdirs()
                        FileOutputStream fos = new FileOutputStream(file)
                        int len
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len)
                        }
                        fos.close()
                    }
                }
                /** Close the input stream **/
                zis.closeEntry()
                zis.close()
                println "Unzip Completed Successfully!!"

            }catch(IOException ex){
                ex.printStackTrace()
            }
        }
    }

    static boolean unzip(String sourcePath, String destPath, boolean modeAutoMkdir){
        if (modeAutoMkdir)
            autoMkdirs(destPath)
        unzip(sourcePath, destPath)
    }

    /**
     * Extract Jar File
     * @param sourcePath
     * @param destPath
     */
    static void unjar(String sourcePath, String destPath){
        sourcePath = getFullPath(sourcePath)
        destPath = getFullPath(destPath)
        List<String> filePathList = getFilePathList(sourcePath)

        filePathList.each {
            byte[] buffer = new byte[BUFFER]
            try {
                /** Ready **/
                java.util.jar.JarFile jar = new java.util.jar.JarFile(sourcePath)
                java.util.Enumeration enumEntries = jar.entries()
                /** Read the jar entries using the nextElement method **/
                while (enumEntries.hasMoreElements()) {
                    java.util.jar.JarEntry entry = (java.util.jar.JarEntry) enumEntries.nextElement()
                    java.io.File file = new java.io.File(destPath + java.io.File.separator + entry.getName())
                    println "Extracting: ${file.getAbsolutePath()}"
                    if (entry.isDirectory()) {
                        file.mkdirs()
                    } else {
                        file.parentFile.mkdirs()
                        java.io.InputStream is = jar.getInputStream(entry)
                        java.io.FileOutputStream fos = new java.io.FileOutputStream(file)
                        int len
                        while ((len = is.read(buffer)) > 0) {
                            fos.write(buffer, 0, len)
                        }
                        fos.close()
                        is.close()
                    }
                }
                /** Close the input stream **/
                jar.close()
                println "Unjar Completed Successfully!!"

            } catch (IOException ex) {
                ex.printStackTrace()
            }
        }
    }

    static boolean unjar(String sourcePath, String destPath, boolean modeAutoMkdir){
        if (modeAutoMkdir)
            autoMkdirs(destPath)
        unjar(sourcePath, destPath)
    }


    static List<String> getFilePathList(String filePath){
        return getFilePathList(filePath, '')
    }

    static List<String> getFilePathList(String filePath, String extension){
        def filePathList = []
        String fullPath = getFullPath(filePath)
        File file = new File(fullPath)
        // check files (new)
        if (!fullPath){
        }else if (file.getName().endsWith('*')){
            new File(fullPath).getParentFile().listFiles().each{ File f -> filePathList << f.path }
        }else{
            filePathList << new File(fullPath).path
        }
        // check extension
        if (extension){
            filePathList = filePathList.findAll{
                int lastDotIdx = it.lastIndexOf('.')
                String itExtension = it.substring(lastDotIdx+1).toUpperCase()
                String acceptExtension = extension.toUpperCase()
                return ( itExtension.equals(acceptExtension) )
            }
        }
        return filePathList
    }





    /***************
     ***************
     *
     * CASE INSTANCE
     *
     ***************
     ***************/



    FileMan setSource(String filePath){
        filePath = getFullPath(filePath)
        return setSource(new File(filePath))
    }
    FileMan setSource(File file){
        this.sourceFile = file
        return this
    }
    
    FileMan backup(){
        return backup(globalOption)
    }
    FileMan backup(FileSetup opt){
        opt = globalOption.clone().merge(opt)
        if (opt.modeAutoBackup){
            String filePath = sourceFile.getPath()
            String backupPath = (opt.backupPath) ?: "${filePath}.bak_${new Date().format('yyyyMMdd_HHmmss')}"
            return backup(backupPath)
        }else{
            return this
        }
    }

    FileMan backup(String destPath){
        return copy(destPath)
    }

    FileMan copy(String destPath){
        copy(sourceFile.path, destPath)
        return this
    }

    FileMan move(String destPath){
        destPath = getFullPath(destPath)
        try{
            sourceFile.renameTo(destPath)
        }finally{
        }
        return this
    }

    FileMan read(){
        return read(sourceFile, globalOption)
    }

    FileMan read(FileSetup fileSetup){
        return read(sourceFile, fileSetup)
    }

    FileMan read(File file, FileSetup fileSetup){
        List<String> lineList = loadFileContent(file, fileSetup)
        read(lineList)
        return this
    }

    FileMan read(String text){
        originalContent = text
        content = "${originalContent}"
        return this
    }

    FileMan read(List lineList){
        originalContent = lineList.join(System.getProperty("line.separator"))
        content = "${originalContent}"
        return this
    }

    FileMan write(){
        return write(globalOption)
    }

    FileMan write(FileSetup fileSetup){
        List<String> lineList = []
        content.eachLine{ lineList << it }
        createNewFile(sourceFile, lineList, fileSetup)
        return this
    }

    def analysis(){
        return ""
    }

    def report(){
        return ""
    }


    FileMan replace(Map replaceMap){
        replaceMap.each{ String target, String replacement ->
            replace(target, replacement)
        }
        return this
    }
    FileMan replace(String target, String replacement){
        replacement = getRightReplacement(replacement)
//        //PRINT
//        if (matchedList.size()){
//            matchedList.each{
//                println "${it} => ${replacement}"
//            }
//        }
//        //REPLACE
        content = content.replaceAll(target, replacement)
        return this
    }

    FileMan replaceLine(Map replaceLineMap){
        replaceLineMap.each{ String target, String replacement ->
            replaceLine(target, replacement)
        }
        return this
    }
    FileMan replaceLine(String target, String replacement){
        String targetPattern = target.replace('.','\\.').replace('$','\\$').replace('#','\\#')
        String patternToGetProperty = ".*" + targetPattern + ".*"
        Matcher matchedList = Pattern.compile(patternToGetProperty, Pattern.MULTILINE).matcher(content)
        replacement = getRightReplacement(replacement)
        //PRINT
        if (matchedList.size()){
            matchedList.each{
                println "${it} \n => ${replacement}"
            }
        }
        //REPLACE
        content = matchedList.replaceAll(replacement)
        return this
    }

    FileMan replaceProperty(Map replacePropertyMap){
        replacePropertyMap.each{ String target, String replacement ->
            replaceProperty(target, replacement)
        }
        return this
    }
    FileMan replaceProperty(String target, String replacement){
        String targetPattern = target.replace('.','[.]').replace('$','\\$')
        String patternToGetProperty = "^\\s*" + targetPattern + "\\s*=.*\$"
        Matcher matchedList = Pattern.compile(patternToGetProperty, Pattern.MULTILINE).matcher(content)
        replacement = getRightReplacement("${target}=${replacement}")
//        println "${matchedList.size()} ${replacement}"
        //PRINT
        if (matchedList.size()){
            matchedList.each{
                println "${it} \n => ${replacement}"
            }
        }
        //REPLACE
        content = matchedList.replaceAll(replacement)
        return this
    }


    String getRightReplacement(String replacement){
        // This Condition's Logic prevent disapearance \
        if (replacement.indexOf('\\') != -1)
            replacement = replacement.replaceAll('\\\\','\\\\\\\\')
        // This Condition's Logic prevent Error - java.lang.IllegalArgumentException: named capturing group has 0 length name
        if (replacement.indexOf('$') != -1)
            replacement = replacement.replaceAll('\\$','\\\\\\$')
        return replacement
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



    static String getFullPath(String path){
        return getFullPath(nowPath, path)
    }

    static String getFullPath(String nowPath, String relativePath){
        if (isItStartsWithRootPath(relativePath))
            return relativePath
        if (!nowPath || !relativePath)
            return ''
        relativePath.split(/[\/\\]/).each{ String next ->
            if (next.equals('..')){
                nowPath = new File(nowPath).getParent()
            }else if (next.equals('.')){
                // ignore
            }else if (next.equals('~')){
                nowPath = System.getProperty("user.home")
            }else{
                nowPath = "${nowPath}/${next}"
            }
        }
        return new File(nowPath).path
    }

    static boolean isItStartsWithRootPath(String path){
        boolean isRootPath = false
        path = new File(path).path
        if (path.startsWith('/') || path.startsWith('\\'))
            return true
        File.listRoots().each{
            String rootPath = new File(it.path).path
            if (path.startsWith(rootPath) || path.startsWith(rootPath.toLowerCase()))
                isRootPath = true
        }
        return isRootPath
    }

}
