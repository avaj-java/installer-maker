package com.jaemisseo.man

import com.jaemisseo.man.util.FileSetup
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream

import java.nio.channels.FileChannel
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
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
        opt = getMergedOption(opt)
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
            throw ex
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
        write(newFile, fileContentLineList, getMergedOption(opt))
    }

    /**
     * 특정 디렉토리의 모든 파일들을 -> 지우기
     */
    static boolean emptyDirectory(String dirPathToDelete, List excludePathList) throws Exception{
        File dirToDelete = new File(dirPathToDelete)
        dirToDelete.listFiles().each{ File fileToDelete ->
            // Delete
            if ( !isExcludeFile(fileToDelete, excludePathList) ){
                if (fileToDelete.isDirectory()){
                    rmdir(fileToDelete, excludePathList)
                }else{
                    log.info("Deleting ${fileToDelete.path}")
                    if (!fileToDelete.delete())
                        log.info(" - Failed - To Delete ${fileToDelete.path}")
                }
            }
        }
        return true
    }

    /**
     * 특정 디렉토리을 -> 완전 지우기
     */
    static boolean rmdir(final File dirToDelete, List excludePathList) {
        if (dirToDelete.isDirectory() && !isExcludeFile(dirToDelete, excludePathList) ){
            // Delete Sub
            dirToDelete.listFiles().each{ File fileToDelete ->
                // Delete File
                if ( !isExcludeFile(fileToDelete, excludePathList) ){
                    if (fileToDelete.isDirectory()){
                        rmdir(fileToDelete, excludePathList)
                    }else{
                        log.info("Deleting ${fileToDelete.path}")
                        if (!fileToDelete.delete())
                            log.info(" - Failed - To Delete ${fileToDelete.path}")
                    }
                }
            }
            // Delete Empty Directory
            log.info("Deleting ${dirToDelete.path}")
            if (!dirToDelete.delete())
                log.info(" - Failed - To Delete ${dirToDelete.path}")
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
        String destDirPath = getLastDirectoryPath(destPath)
        //Do AUTO MKDIR
        FileMan.mkdirs(destDirPath)
        //Check Directory
        if (!new File(destDirPath).isDirectory() && !new File(destDirPath).exists())
            throw new Exception('There is No Directory. Maybe, It Failed To Create Directory.')
    }

    /**
     * Get Relative Path
     */
    static String getRelativePath(String from, String to){
//        new File(libPath).toURI().relativize(new File(installerHome).toURI())
        String relPath
        //0. Ready for diff
        List toDepthList = getLastDirectoryPath(to).split(/[\/\\]/)
        List fromDepthList = getLastDirectoryPath(from).split(/[\/\\]/)
        String fromFileName = getLastFileName(from)
        String toFileName = getLastFileName(to)
        String addFileName = (fromFileName && !toFileName) ? fromFileName : (fromFileName && toFileName) ? toFileName : ''
        int sameDepthLevel = -1
        //1. Get Same Depth
        for (int i=0; i<fromDepthList.size(); i++){
            String fromDirName = fromDepthList[i]
            String toDirName = toDepthList[i]
            if (!fromDirName || !toDirName || !fromDirName.equals(toDirName)){
                sameDepthLevel = i - 1
                break
            }
        }
        //2. Gen Relative Dir Path
        int diffStartIndex = sameDepthLevel + 1
        int fromLastIndex = fromDepthList.size() - 1
        int diffDepthCount = sameDepthLevel - fromLastIndex
        if (diffDepthCount == 0){
            relPath = '.'
        }else if (diffDepthCount < 0){
            relPath = fromDepthList[diffStartIndex..fromLastIndex].collect{ '..' }.join('/')
        }else if (diffDepthCount > 0){
            relPath = fromDepthList[diffStartIndex..fromLastIndex].join('/')
        }
        return "${relPath}/${addFileName}"
    }

    //Get Last Directory
    static String getLastDirectoryPath(String filePath){
        return (isFile(filePath)) ? new File(filePath).getParentFile().getPath() : filePath
    }

    /**
     * Check
     */
    static boolean checkPath(String sourcePath, String destPath){
        //Check Source Path
        if (!sourcePath)
            throw new IOException('No Source Path, Please Set Source Path.')
        if (!sourcePath.contains('*') && !new File(sourcePath).exists())
            throw new IOException("Does not exist Source Path, ${sourcePath}")
        if (isRootPath(sourcePath))
            throw new IOException('Source Path naver be seted rootPath on FileSystem.')
        //Check Dest Path
        if (!destPath)
            throw new IOException('No Dest Path, Please Set Dest Path.')
        if (isRootPath(destPath))
            throw new IOException('Dest Path naver be seted set rootPath on FileSystem.')
    }

    static boolean checkDir(String path, boolean modeAutoMkdir){
//        File baseDir = new File(path).getParentFile()
        File baseDir = new File(getLastDirectoryPath(path))
        if (modeAutoMkdir){
            autoMkdirs(baseDir.path)
            if (!baseDir.exists())
                throw new Exception("\n < Failed to CREATE Directory > Directory To Save File Could Not be Created", new Throwable("You Need To Check Permission Check And... Some... "))
        }else{
            if (!baseDir.exists())
                throw new Exception("\n < Failed to WRITE File> No Directory To Save File ", new Throwable("Check Please."))
        }
        return true
    }

    static boolean checkFile(String path, boolean modeAutoOverWrite){
        if (modeAutoOverWrite && new File(path).exists())
            throw new Exception("\n < Failed to WRITE File > File Already Exists", new Throwable("Check Please."))
        return true
    }

    static boolean checkFiles(List<String> entry, boolean modeAutoOverWrite){
        entry.each{ String path ->
            checkFile(new File(path).path, modeAutoOverWrite)
        }
        return true
    }

    static boolean checkFiles(String destPath, List<String> entry, boolean modeAutoOverWrite){
        String rootPath = getLastDirectoryPath(destPath)
        entry.each{ String relPath ->
            checkFile(new File(rootPath, relPath).path, modeAutoOverWrite)
        }
        return true
    }

    //Check File? or Directory?
    static boolean isFile(String filePath){
        // - 끝이 구분자로 끝나면 => 폴더로 인식
        if (filePath.endsWith('/')|| filePath.endsWith('\\'))
            return false
        // - 확장자가 존재하면 => 부모를 폴더로 인식
        else if (new File(filePath).getName().contains('.'))
            return true
        // - 그외에는 무조건 폴더로 인식
        else
            return false
    }

    static boolean isRootPath(String filePath){
        try{
            new File(getFullPath(filePath)).getParentFile()
            return false
        }catch(e){
            return true
        }
    }

    // File Path -> FileName String
    // Directory Path -> Empty String
    static String getLastFileName(String filePath){
        List fromOriginDepthList = filePath.split(/[\/\\]/)
        return (isFile(filePath)) ? fromOriginDepthList[fromOriginDepthList.size()-1] : null
    }

    /**
     * WRITE
     */
    static boolean write(String newFilePath, String content){
        return write(newFilePath, content, new FileSetup())
    }

    static boolean write(String newFilePath, String content, boolean modeAutoMkdir){
        return write(newFilePath, content, new FileSetup(modeAutoMkdir:modeAutoMkdir))
    }

    static boolean write(String newFilePath, String content, FileSetup opt){
        List<String> fileContentLineList = []
        content.eachLine{ fileContentLineList << it }
        return write(newFilePath, fileContentLineList, opt)
    }

    static boolean write(String newFilePath, List<String> fileContentLineList, FileSetup opt){
        return write(new File(getFullPath(newFilePath)), fileContentLineList, opt)
    }

    static boolean write(File newFile, List<String> fileContentLineList, boolean modeAutoMkdir){
        return write(newFile, fileContentLineList, new FileSetup(modeAutoMkdir:modeAutoMkdir))
    }

    static boolean write(File newFile, List<String> fileContentLineList, FileSetup opt){
        //Check Path Parameter
        checkPath('dummy*', newFile.path)
        //Check Dest
        String lastDirPath = newFile.getParentFile().getPath() //name not contains dot can be file on Here
        checkDir(lastDirPath, opt.modeAutoMkdir)
        checkFile(newFile.path, opt.modeAutoOverWrite)
        //Write File to Dest
        try{
            newFile.withWriter(opt.encoding){ out ->
                // METHOD A. Auto LineBreak
                if (opt.modeAutoLineBreak)
                    fileContentLineList.each{ String oneLine -> out.println oneLine }
                // METHOD B. Custom LineBreak
                else
                    out.print ( fileContentLineList.join(opt.lineBreak) + ((opt.lastLineBreak)?:'') )
            }
            log.info(" - Complete - Create ${newFile.path} \n")
        }catch(Exception e){
            log.info(" - Failed - To Create ${newFile.path} \n")
            throw new Exception(" < Failed to WRITE File >", new Throwable("You Need To Check Permission Check And... Some... "))
        }
        return true
    }

    /**
     * COPY
     * 파일 => 파일 (파일명변경)
     * *   => 폴더   (자동파일명)
     * 파일 => 폴더  (자동파일명)
     * 폴더 => 폴더  (자동파일명)
     */
    static boolean copy(String sourcePath, String destPath){
        return copy(sourcePath, destPath, new FileSetup())
    }

    static boolean copy(String sourcePath, String destPath, boolean modeAutoMkdir){
        copy(sourcePath, destPath, new FileSetup(modeAutoMkdir: modeAutoMkdir))
    }

    static boolean copy(String sourcePath, String destPath, FileSetup opt){
        //Check Path Parameter
        sourcePath = getFullPath(sourcePath)
        destPath = getFullPath(destPath)
        checkPath(sourcePath, destPath)
        //Make Entry
        List entryList = genFileEntryList(sourcePath)
        String sourceRootPath = new File(sourcePath).getParentFile().getPath()
        String destRootPath = getLastDirectoryPath(destPath)
        //Check Dest
        checkDir(destPath, opt.modeAutoMkdir)
        checkFiles(destPath, entryList, opt.modeAutoOverWrite)
        //Copy File to Dest
        for (String fileRelPath : entryList){
            File sourceFile = new File(sourceRootPath, fileRelPath)
            File destFile = new File(destRootPath, fileRelPath)
            copy(sourceFile, destFile)
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


    /**
     * MOVE
     * 파일 => 파일 (파일명변경)
     * *   => 폴더   (자동파일명)
     * 파일 => 폴더  (자동파일명)
     * 폴더 => 폴더  (자동파일명)
     */
    static boolean move(String sourcePath, String destPath){
        return move(sourcePath, destPath, new FileSetup())
    }

    static boolean move(String sourcePath, String destPath, boolean modeAutoMkdir){
        return move(sourcePath, destPath, new FileSetup(modeAutoMkdir:modeAutoMkdir))
    }

    static boolean move(String sourcePath, String destPath, FileSetup opt){
        //Check Path Parameter
        sourcePath = getFullPath(sourcePath)
        destPath = getFullPath(destPath)
        checkPath(sourcePath, destPath)
        //Make Entry
        List entryList = genFileEntryList(sourcePath)
        String sourceRootPath = new File(sourcePath).getParentFile().getPath()
        String destRootPath = getLastDirectoryPath(destPath)
        //Check Dest
        checkDir(destPath, opt.modeAutoMkdir)
        checkFiles(destPath, entryList, opt.modeAutoOverWrite)
        //Move File to Dest
        try{
            new File(sourcePath).renameTo(destPath)
        }finally{
        }
        return true
    }

    /**
     * DELETE
     */
    static boolean delete(String sourcePath){
        return delete(sourcePath, new FileSetup())
    }

    static boolean delete(String sourcePath, FileSetup opt){
        //Check Path Parameter
        sourcePath = getFullPath(sourcePath)
        checkPath(sourcePath, 'dummy*')
        //Make Entry
        List entryList = genFileEntryList(sourcePath)
        String sourceRootPath = new File(sourcePath).getParentFile().getPath()
        //Delete File
        return delete(entryList, sourceRootPath)
    }

    static boolean delete(List entryList, String sourceRootPath){
        println "\nStart Deleting File"
        try{
            for (String file : entryList){
                String path = sourceRootPath + File.separator + file
                File fileToDelete = new File(path)
                println "Deleting: ${file}"
                if (fileToDelete.isFile()){
                    fileToDelete.delete()
                }else{
                    rmdir(fileToDelete, [])
                }
            }
            println "<Done>"

        }catch(IOException ex){
            println "<Error>"
            ex.printStackTrace()
            throw ex
        }finally{
        }
        return true
    }


    /**
     * COMPRESSING
     */
    static boolean compress(String sourcePath){
        compress(sourcePath, new File(sourcePath).getParentFile().getPath())
    }

    static boolean compress(String sourcePath, String destPath){
        compress(sourcePath, destPath, new FileSetup())
    }

    static boolean compress(String sourcePath, String destPath, boolean modeAutoMkdir){
        compress(sourcePath, destPath, new FileSetup(modeAutoMkdir:modeAutoMkdir))
    }

    static boolean compress(String sourcePath, String destPath, FileSetup opt){
        String fileName = new File(destPath).getName()
        String extension = fileName.substring(fileName.lastIndexOf('.'))?.toLowerCase()
        if (extension){
            if (extension == 'tar')
                return tar(sourcePath, destPath, opt)
            else if (extension == 'jar')
                return jar(sourcePath, destPath, opt)
            else
                return zip(sourcePath, destPath, opt)
        }
    }


    /**
     * COMPRESSING - ZIP
     */
    static boolean zip(String sourcePath){
        zip(sourcePath, null)
    }

    static boolean zip(String sourcePath, String destPath){
        zip(sourcePath, destPath, new FileSetup())
    }

    static boolean zip(String sourcePath, String destPath, boolean modeAutoMkdir){
        zip(sourcePath, destPath, new FileSetup(modeAutoMkdir:modeAutoMkdir))
    }

    static boolean zip(String sourcePath, String destPath, FileSetup opt){
        //Auto DestPath
        destPath = getAutoDestPath(sourcePath, destPath, 'zip')
        //Check Path Parameter
        sourcePath = getFullPath(sourcePath)
        destPath = getFullPath(destPath)
        checkPath(sourcePath, destPath)
        //Make Entry
        List entryList = genFileEntryList(sourcePath)
        String sourceRootPath = new File(sourcePath).getParentFile().getPath()
        String destRootPath = getLastDirectoryPath(destPath)
        //Check Dest
        checkDir(destPath, opt.modeAutoMkdir)
        checkFile(destPath, opt.modeAutoOverWrite)
        //Zip Files to Dest
        return zip(entryList, sourceRootPath, destPath)
    }

    static boolean zip(List entryList, String sourceRootPath, String destPath){
        //Compress Zip
        byte[] buffer = new byte[BUFFER]
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destPath))
        println "\nStart Compressing ZIP"
        try{
            for (String file : entryList){
                String path = sourceRootPath + File.separator + file
                println "Compressing: ${file}"
                zos.putNextEntry(new ZipEntry(file))
                if (new File(path).isFile()){
                    FileInputStream fis = new FileInputStream(path)
                    int len
                    while ((len = fis.read(buffer)) > 0){
                        zos.write(buffer, 0, len)
                    }
                    fis.close()
                }
            }
            System.out.println("<Done>")

        }catch(IOException ex){
            System.out.println("<Error>")
            ex.printStackTrace()
            throw ex
        }finally{
            if (zos){
                zos.closeEntry()
                zos.close()
            }
        }
        return true
    }

    /**
     * COMPRESSING - JAR
     */
    static boolean jar(String sourcePath){
        jar(sourcePath, null)
    }

    static boolean jar(String sourcePath, String destPath){
        jar(sourcePath, destPath, new FileSetup())
    }

    static boolean jar(String sourcePath, String destPath, boolean modeAutoMkdir){
        jar(sourcePath, destPath, new FileSetup(modeAutoMkdir:modeAutoMkdir))
    }

    static boolean jar(String sourcePath, String destPath, FileSetup opt) throws IOException{
        //Auto DestPath
        destPath = getAutoDestPath(sourcePath, destPath, 'zip')
        //Check Path Parameter
        sourcePath = getFullPath(sourcePath)
        destPath = getFullPath(destPath)
        checkPath(sourcePath, destPath)
        //Make Entry
        List entryList = genFileEntryList(sourcePath)
        String sourceRootPath = new File(sourcePath).getParentFile().getPath()
        String destRootPath = getLastDirectoryPath(destPath)
        //Check Dest
        checkDir(destPath, opt.modeAutoMkdir)
        checkFile(destPath, opt.modeAutoOverWrite)
        //Jar Files to Dest
        return jar(entryList, sourceRootPath, destPath)
    }

    static boolean jar(List entryList, String sourceRootPath, String destPath){
        //Compress Jar
        byte[] buffer = new byte[BUFFER]
//        JarOutputStream jos = new JarOutputStream(new FileOutputStream(destPath), manifest)
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(destPath))
        println "\nStart Compressing JAR"
        try{
            for (String file : entryList){
                String path = sourceRootPath + '/' + file
                path = path.replace("\\", "/")
                file = file.replace("\\", "/")
                println "Compressing: ${file}"
                JarEntry entry = new JarEntry(file)
                jos.putNextEntry(entry)
                if (new File(path).isFile()){
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
                    int len
                    while ( (len = bis.read(buffer)) > 0){
                        jos.write(buffer, 0, len)
                    }
                    bis.close()
                }
            }
            System.out.println("<Done>")

        }catch(IOException ex){
            System.out.println("<Error>")
            ex.printStackTrace()
            throw ex
        }finally{
            if (jos){
                jos.closeEntry()
                jos.close()
            }
        }
        return true
    }

    /**
     * COMPRESSING - TAR.GZ
     */
    static boolean tar(String sourcePath){
        tar(sourcePath, null)
    }

    static boolean tar(String sourcePath, String destPath){
        tar(sourcePath, destPath, new FileSetup())
    }

    static boolean tar(String sourcePath, String destPath, boolean modeAutoMkdir){
        tar(sourcePath, destPath, new FileSetup(modeAutoMkdir:modeAutoMkdir))
    }

    static boolean tar(String sourcePath, String destPath, FileSetup opt) throws IOException{
        //Auto DestPath
        destPath = getAutoDestPath(sourcePath, destPath, 'zip')
        //Check Path Parameter
        sourcePath = getFullPath(sourcePath)
        destPath = getFullPath(destPath)
        checkPath(sourcePath, destPath)
        //Make Entry
        List entryList = genFileEntryList(sourcePath)
        String sourceRootPath = new File(sourcePath).getParentFile().getPath()
        String destRootPath = getLastDirectoryPath(destPath)
        //Check Dest
        checkDir(destPath, opt.modeAutoMkdir)
        checkFile(destPath, opt.modeAutoOverWrite)
        //Tar Files to Dest
        return tar(entryList, sourceRootPath, destPath)
    }

    static boolean tar(List entryList, String sourceRootPath, String destPath){
        //Compress Tar
        byte[] buffer = new byte[BUFFER]
//        TarArchiveOutputStream taos = new TarArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(destPath)))
        TarArchiveOutputStream taos = new TarArchiveOutputStream(new GzipCompressorOutputStream(new BufferedOutputStream(new FileOutputStream(destPath))))
        // TAR has an 8 gig file limit by default, this gets around that
        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR) // to get past the 8 gig limit
        // TAR originally didn't support long file names, so enable the support for it
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)
        println "\nStart Compressing TAR"
        try{
            for (String file : entryList){
                String path = sourceRootPath + '/' + file
                path = path.replace("\\", "/")
                file = file.replace("\\", "/")
                println "Compressing: ${file}"
                TarArchiveEntry entry = new TarArchiveEntry(new File(file), file)
                if (new File(path).isFile()) {
                    entry.setSize(new File(path).length())
                    taos.putArchiveEntry(entry)
                    /////A
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path))
                    int len
                    while ( (len = bis.read(buffer)) > 0 ) {
                        taos.write(buffer, 0, len)
                    }
                    bis.close()
                    /////B
//                    FileInputStream fis = new FileInputStream(path)
//                    IOUtils.copy(fis, taos)
                    taos.flush()
                }else{
                    taos.putArchiveEntry(entry)
                }
                taos.closeArchiveEntry()
            }
            System.out.println("<Done>")

        }catch(IOException ex){
            System.out.println("<Error>")
            ex.printStackTrace()
            throw ex
        }finally{
            if (taos){
                taos.close()
            }
        }
        return true
    }




    /**
     * EXTRACTING
     */
    static boolean extract(String sourcePath){
        extract(sourcePath, getLastDirectoryPath(sourcePath), new FileSetup())
    }

    static boolean extract(String sourcePath, String destPath){
        extract(sourcePath, destPath, new FileSetup())
    }

    static boolean extract(String sourcePath, String destPath, boolean modeAutoMkdir){
        extract(sourcePath, destPath, new FileSetup(modeAutoMkdir:modeAutoMkdir))
    }

    static boolean extract(String filePath, String destPath, FileSetup opt){
        String fileName = new File(filePath).getName()
        String extension = fileName.substring(fileName.lastIndexOf('.'))?.toLowerCase()
        if (extension){
            if (extension == 'tar')
                return untar(filePath, destPath, opt)
            if (extension == 'jar')
                return unjar(filePath, destPath, opt)
            else
                return unzip(filePath, destPath, opt)
        }
    }

    /**
     * EXTRACTING - UNTAR
     */
    static boolean untar(String sourcePath){
        untar(sourcePath, getLastDirectoryPath(sourcePath))
    }

    static boolean untar(String sourcePath, String destPath){
        untar(sourcePath, destPath, new FileSetup())
    }

    static boolean untar(String sourcePath, String destPath, boolean modeAutoMkdir){
        untar(sourcePath, destPath, new FileSetup(modeAutoMkdir:modeAutoMkdir))
    }

    static boolean untar(String sourcePath, String destPath, FileSetup opt){
        //Check Path Parameter
        sourcePath = getFullPath(sourcePath)
        destPath = getFullPath(destPath)
        checkPath(sourcePath, destPath)
        //Make Entry
        List<String> filePathList = getFilePathList(sourcePath)
        String sourceRootPath = new File(sourcePath).getParentFile().getPath()
        String destRootPath = getLastDirectoryPath(destPath)
        //Check Dest
        checkDir(destPath, opt.modeAutoMkdir)
        checkFiles(filePathList, opt.modeAutoOverWrite)
        //Tar File To Dest
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
                println "Untar Completed successfully!!\n"

            } catch (IOException ex) {
                ex.printStackTrace()
                throw ex
            }
        }
        return true
    }

    /**
     * EXTRACTING - UNZIP
     */
    static boolean unzip(String sourcePath){
        unzip(sourcePath, getLastDirectoryPath(sourcePath))
    }

    static boolean unzip(String sourcePath, String destPath){
        unzip(sourcePath, destPath, new FileSetup())
    }

    static boolean unzip(String sourcePath, String destPath, boolean modeAutoMkdir){
        unzip(sourcePath, destPath, new FileSetup(modeAutoMkdir:modeAutoMkdir))
    }

    static boolean unzip(String sourcePath, String destPath, FileSetup opt){
        //Check Path Parameter
        sourcePath = getFullPath(sourcePath)
        destPath = getFullPath(destPath)
        checkPath(sourcePath, destPath)
        //Make Entry
        List<String> filePathList = getFilePathList(sourcePath)
        String sourceRootPath = new File(sourcePath).getParentFile().getPath()
        String destRootPath = getLastDirectoryPath(destPath)
        //Check Dest
        checkDir(destPath, opt.modeAutoMkdir)
        checkFiles(filePathList, opt.modeAutoOverWrite)
        //Zip File To Dest
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
                println "Unzip Completed successfully!!\n"

            }catch(IOException ex){
                ex.printStackTrace()
                throw ex
            }
        }
        return true
    }

    /**
     * EXTRACTING - UNJAR
     */
    static boolean unjar(String sourcePath){
        unjar(sourcePath, getLastDirectoryPath(sourcePath))
    }

    static boolean unjar(String sourcePath, String destPath){
        unjar(sourcePath, destPath, new FileSetup())
    }

    static boolean unjar(String sourcePath, String destPath, boolean modeAutoMkdir){
        unjar(sourcePath, destPath, new FileSetup(modeAutoMkdir:modeAutoMkdir))
    }

    static boolean unjar(String sourcePath, String destPath, FileSetup opt){
        //Check Path Parameter
        sourcePath = getFullPath(sourcePath)
        destPath = getFullPath(destPath)
        checkPath(sourcePath, destPath)
        //Make Entry
        List<String> filePathList = getFilePathList(sourcePath)
        String sourceRootPath = new File(sourcePath).getParentFile().getPath()
        String destRootPath = getLastDirectoryPath(destPath)
        //Check Dest
        checkDir(destPath, opt.modeAutoMkdir)
        checkFiles(filePathList, opt.modeAutoOverWrite)
        //Jar File To Dest
        filePathList.each{
            byte[] buffer = new byte[BUFFER]
            try{
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
                println "Unjar Completed successfully!!\n"

            } catch (IOException ex) {
                ex.printStackTrace()
                throw ex
            }
        }
        return true
    }



    static String getAutoDestPath(String sourcePath, String destPath, String extension){
        if (sourcePath && !destPath){
            String parentPath = new File(sourcePath).getParentFile().path
            String parentName = new File(sourcePath).getParentFile().name
            String fileName = new File(sourcePath).name.split('[.]')[0]
            destPath = sourcePath.contains('*') ? "${parentPath}/${parentName}.${extension}": "${parentPath}/${fileName}.${extension}"
        }
        return destPath
    }

    static List<String> genFileEntryList(String sourcePath){
        List<String> newEntryList = []
        String rootPath = new File(sourcePath).getParentFile().getPath()
        List<String> filePathList = getFilePathList(sourcePath)
        filePathList.each{ String onePath ->
            if (isMatchedPath(onePath, sourcePath))
                newEntryList = genFileEntryList(newEntryList, rootPath, onePath)
        }
        return newEntryList
    }

    static List<String> genFileEntryList(List<String> entryList, String rootPath, String filePath){
        File node = new File(filePath)
        String oneFilePath = node.getPath().substring(rootPath.length()+1, filePath.length())
        if(node.isDirectory()){
            String[] subNote = node.list()
            entryList.add(oneFilePath + System.getProperty('file.separator'))
            for (String filename : subNote){
                genFileEntryList(entryList, rootPath, new File(node, filename).getPath())
            }
        }else{
            entryList.add(oneFilePath)
        }
        return entryList
    }

    static boolean isMatchedPath(String onePath, rangePath){
        String regexpStr = rangePath.replace('\\', '/').replace('*',"[^\\/\\\\]*").replace('.', '\\.')
        return onePath.replace('\\', '/').matches(regexpStr)
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
        }else if (fullPath.contains('*')){
            new File(fullPath).getParentFile().listFiles().each{ File f ->
                if (isMatchedPath(f.path, fullPath))
                    filePathList << f.path
            }
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

    static File getFileFromResource(String resourcePath){
        //Works in IDE
//        URL url = getClass().getResource(absolutePath);
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourcePath)
        File file
        if (url.toString().startsWith("jar:")){
            //Works in JAR
            try {
                InputStream input = getClass().getResourceAsStream("/${resourcePath}")
                file = File.createTempFile("tempfile", ".tmp")
                OutputStream out = new FileOutputStream(file)
                int len
                byte[] bytes = new byte[1024]
                while ((len = input.read(bytes)) != -1) {
                    out.write(bytes, 0, len)
                }
                file.deleteOnExit()
            } catch (IOException ex) {
                ex.printStackTrace()
            }
        }else{
            //Works in your IDE, but not from a JAR
            file = new File(url.getFile())
        }
        if (file != null && !file.exists())
            throw new RuntimeException("Error: File " + file + " not found!")
        return file
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

    boolean exists(){
        return sourceFile.exists()
    }

    /**
     * backup
     */
    FileMan backup(){
        return backup(globalOption)
    }
    FileMan backup(FileSetup opt){
        opt = getMergedOption(opt)
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


    /**
     * copy
     */
    FileMan copy(String destPath){
        return copy(destPath, globalOption)
    }

    FileMan copy(String destPath, FileSetup opt){
        opt = getMergedOption(opt)
        copy(sourceFile.path, destPath, opt)
        return this
    }


    /**
     * move
     */
    FileMan move(String destPath){
        return move(destPath, globalOption)
    }

    FileMan move(String destPath, FileSetup opt){
        opt = getMergedOption(opt)
        move(sourceFile.path, destPath, opt)
        return this
    }

    /**
     * read
     */
    FileMan read(){
        return read(sourceFile, globalOption)
    }

    FileMan read(FileSetup fileSetup){
        return read(sourceFile, fileSetup)
    }

    FileMan read(File file){
        return read(file, globalOption)
    }

    FileMan read(File file, FileSetup fileSetup){
        List<String> lineList = loadFileContent(file, fileSetup)
        read(lineList)
        return this
    }

    FileMan readResource(String resourcePath){
        File resourceFile = getFileFromResource(resourcePath)
        return read(resourceFile, globalOption)
    }

    FileMan readFile(String filePath){
        filePath = getFullPath(filePath)
        return read(filePath, globalOption)
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

    /**
     * write
     */
    FileMan write(){
        return write(sourceFile, globalOption)
    }

    FileMan write(FileSetup fileSetup){
        return write(sourceFile, fileSetup)
    }

    FileMan write(String filePath){
        return write(filePath, globalOption)
    }

    FileMan write(String filePath, FileSetup fileSetup){
        filePath = getFullPath(filePath)
        return write(new File(filePath), fileSetup)
    }

    FileMan write(File fileToWrite, FileSetup fileSetup){
        List<String> lineList = []
        content.eachLine{ lineList << it }
        createNewFile(fileToWrite, lineList, fileSetup)
        return this
    }



    def analysis(){
        return ""
    }

    def report(){
        return ""
    }

    /**
     * replace
     */
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

    /**
     * replace line
     */
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

    /**
     * replace property
     */
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


















    FileSetup getMergedOption(FileSetup opt){
        return globalOption.clone().merge(opt)
    }

    /**
     * Check Exclude File
     */
    static boolean isExcludeFile(File targetFile, List<String> excludePathList){
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
        if (!relativePath)
            return null
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
