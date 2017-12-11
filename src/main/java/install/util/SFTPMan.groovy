package install.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpATTRS
import com.jcraft.jsch.SftpException
import jaemisseo.man.FileMan
import jaemisseo.man.bean.FileSetup
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

class SFTPMan{

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    static final String UPLOAD = "UPLOAD"
    static final String DOWNLOAD = "DOWNLOAD"
    static final String LIST = "LIST"
    static final String ENTRYLIST = "ENTRYLIST"
    static final String RM = "RM"
    static final String RMDIR = "RMDIR"
    static final String MKDIR = "MKDIR"
    static final String RENMAE = "RENAME"
    static final String LS = "LS"

    Session session = null;
    Channel channel = null;
    ChannelSftp sftp = null;

    /*************************
     *
     * Connect
     *
     *************************/
    SFTPMan connect(String user){
        return connect(user, user)
    }

    SFTPMan connect(String user, String password){
        return connect(user, password, 'localhost')
    }

    SFTPMan connect(String user, String password, String host){
        return connect(user, password, host, 22)
    }

    SFTPMan connect(String user, String password, String host, int port){
        JSch jsch = new JSch()

//            ssh.setKnownHosts("~/.ssh/id_rsa.pub");
        session = jsch.getSession(user, host, port);
        session.setPassword(password);

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();

        channel = session.openChannel("sftp");
        channel.connect();

        sftp = (ChannelSftp) channel;
        return this
    }

    /*************************
     *
     * Disconnect
     *
     *************************/
    SFTPMan disconnect(){
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
        return this
    }

    /*************************
     *
     * SUBLIST
     *
     *************************/
    SFTPMan printLs(String serverPath){
        java.util.Vector<ChannelSftp.LsEntry> vec = ls(serverPath)
        vec.each{
            println it
        }
        return this
    }

    /*************************
     *
     * ENTRYLIST
     *
     *************************/
    SFTPMan printRecursiveLs(String serverPath){
        Map<String, ChannelSftp.LsEntry> entryMap = getEntryMap(serverPath)
        entryMap.each{ String relPath, ChannelSftp.LsEntry entry ->
            println "${entry.attrs}  ${relPath}"
        }
        return this
    }

    /*************************
     *
     * Upload
     *
     *************************/
    SFTPMan upload(String localPath, String serverPath){
        return upload(localPath, serverPath, new FileSetup())
    }

    SFTPMan upload(String localPath, String serverPath, FileSetup opt){
        //- Check Path Parameter
        localPath = FileMan.getFullPath(localPath)
        serverPath = FileMan.getFullPath(serverPath)
        FileMan.checkPath(localPath, serverPath)
        //- Check Source(Local)
        List<String> entryList = FileMan.getEntryList(localPath)
        if (FileMan.checkSourceFiles(localPath, entryList)){
            //- Check Dest(Server)
            checkDir(serverPath, opt.modeAutoMkdir)
            checkFiles(serverPath, entryList, opt.modeAutoOverWrite)
            /** UPLOAD **/
            FileMan.startLogPath('UPLOAD', localPath, serverPath)
            if (FileMan.isFile(localPath)){
                File sourceFile = new File(localPath)
                if (FileMan.isFile(serverPath) || !isDirectory(serverPath)){
                    put(sourceFile, serverPath, opt)
                }else{
                    File destFile = new File(serverPath, sourceFile.getName())
                    String destFilePath = FileMan.toSlash(destFile.path)
                    put(sourceFile, destFilePath, opt)
                }
            }else{
                String sourceRootPath = new File(localPath).getParentFile().getPath()
                String destRootPath = FileMan.getLastDirectoryPath(serverPath)
                entryList.each{ String relPath ->
                    File sourceFile = new File(sourceRootPath, relPath)
                    File destFile = new File(destRootPath, relPath)
                    String destFilePath = FileMan.toSlash(destFile.path)
                    if (sourceFile.isDirectory()){
                        mkdir(destFilePath)
                    }else {
                        put(sourceFile, destFilePath, opt)
                    }
                }
            }
        }
        return this
    }

    /*************************
     *
     * Download
     *
     *************************/
    SFTPMan download(String serverPath, String localPath){
        return download(serverPath, localPath, new FileSetup())
    }

    SFTPMan download(String serverPath, String localPath, FileSetup opt){
        //- Check Path Parameter
        localPath = FileMan.getFullPath(localPath)
        serverPath = FileMan.getFullPath(serverPath)
        FileMan.checkPath(localPath, serverPath)
        //- Check Source(Server)
        Map<String, ChannelSftp.LsEntry> entryMap = getEntryMap(serverPath)
        List<String> entryList = entryMap.keySet().toList()
        if (FileMan.checkSourceFiles(serverPath, entryList)){
            //- Check Dest(Local)
            FileMan.checkDir(localPath, opt.modeAutoMkdir)
            FileMan.checkFiles(localPath, entryList, opt.modeAutoOverWrite)
            /** DOWNLOAD **/
            FileMan.startLogPath('DOWNLOAD', serverPath, localPath)
            if (FileMan.isFile(localPath)){
                get(serverPath, new File(localPath), opt)
            }else{
                String sourceRootPath = new File(serverPath).getParentFile().getPath()
                String destRootPath = FileMan.getLastDirectoryPath(localPath)
                entryMap.each{ String relPath, ChannelSftp.LsEntry entry ->
                    File sourceFile = new File(sourceRootPath, relPath)
                    File destFile = new File(destRootPath, relPath)
                    String sourceFilePath = FileMan.toSlash(sourceFile.path)
                    if (entry.getAttrs().isDir()){
                        FileMan.mkdirs(destFile.path)
                    }else {
                        get(sourceFilePath, destFile, opt)
                    }
                }
            }
        }
        return this
    }




    /*****
     * CD
     *****/
    SFTPMan cd(String path){
        sftp.cd(path)
        return this
    }

    /*****
     * rename
     *****/
    SFTPMan rename(String oldPath, String newPath){
        sftp.rename(oldPath, newPath)
        return this
    }

    /*****
     * rm
     *****/
    SFTPMan rm(String path){
        sftp.rm(path)
        return this
    }

    /*****
     * rmdir
     *****/
    SFTPMan rmdir(String path){
        sftp.rmdir(path)
        return this
    }

    /*****
     * ls
     *****/
    Vector ls(String path){
        Vector vec = sftp.ls(path)
        return vec
    }

    /*****
     * mkdir
     *****/
    SFTPMan mkdir(String path){
        sftp.mkdir(path)
        return this
    }

    /*****
     * get
     *****/
    SFTPMan get(String serverFilePath){
        return get(serverFilePath, new File(FileMan.getFullPath('./')))
    }

    SFTPMan get(String serverFilePath, File localFile){
        return get(serverFilePath, localFile, new FileSetup())
    }

    SFTPMan get(String serverFilePath, File localFile, FileSetup opt){
        if (opt.modeAutoOverWrite)
            sftp.get(serverFilePath, new FileOutputStream(localFile))
        else
            sftp.get(serverFilePath, new FileOutputStream(localFile))
        return this
    }

    /*****
     * put
     *****/
    SFTPMan put(File localFile, String serverFilePath){
        return put(new FileInputStream(localFile), serverFilePath, new FileSetup())
    }

    SFTPMan put(File localFile, String serverFilePath, FileSetup opt){
        if (opt.modeAutoOverWrite)
            sftp.put(new FileInputStream(localFile), serverFilePath, sftp.OVERWRITE)
        else
            sftp.put(new FileInputStream(localFile), serverFilePath)
        return this
    }



    private boolean isDirectory(String serverPath) throws SftpException{
        if (serverPath.contains('*'))
            return false
        if (isExist(serverPath))
            return sftp.stat(serverPath).isDir()
        return false
    }

    private boolean isExist(String serverPath) throws SftpException{
        boolean result
        try{
            SftpATTRS attrs = sftp.lstat(serverPath)
            result = true
        }catch(Exception e){
            result = false
        }
        return result
    }

    private boolean checkDir(String serverPath, boolean modeAutoMkdir){
//        File baseDir = new File(path).getParentFile()
        String baseDirPath = FileMan.toSlash(FileMan.getLastDirectoryPath(serverPath))
        if (modeAutoMkdir && !isExist(baseDirPath)){
            autoMkdirs(baseDirPath)
            if (!isExist(baseDirPath))
                throw new Exception("< Failed to CREATE Directory > Directory To Save File Could Not be Created", new Throwable("You Need To Check Permission Check And... Some... "))
        }else{
            if (!isExist(baseDirPath))
                throw new Exception("< Failed to WRITE File> No Directory To Save File ", new Throwable("Check Please."))
        }
        return true
    }

    private boolean checkFile(String serverPath){
        if (isExist(serverPath))
            throw new Exception("< Failed to WRITE File > File Already Exists. ${serverPath}", new Throwable("Check Please. ${serverPath}"))
        return true
    }

    private boolean checkFile(String path, boolean modeAutoOverWrite){
        if (!modeAutoOverWrite)
            checkFile(path)
        return true
    }

    private boolean checkFiles(List<String> entry, boolean modeAutoOverWrite){
        if (!modeAutoOverWrite){
            entry.each{ String relPath ->
                checkFile(FileMan.toSlash(new File(relPath).path))
            }
        }
        return true
    }

    private boolean checkFiles(String destPath, List<String> entry, boolean modeAutoOverWrite){
        destPath = FileMan.getFullPath(FileMan.replaceWeirdString(destPath))
        if (!modeAutoOverWrite) {
            if (FileMan.isFile(destPath) || !isDirectory(destPath)){
                checkFile(destPath)
            } else {
                String rootPath = FileMan.getLastDirectoryPath(destPath)
                entry.each { String relPath ->
                    checkFile(FileMan.toSlash(new File(rootPath, relPath).path))
                }
            }
        }
        return true
    }

    boolean autoMkdirs(String serverPath){
        String destDirPath = FileMan.getLastDirectoryPath(serverPath)
        //Do AUTO MKDIR
        mkdirs(destDirPath)
        //Check Directory
        if (!isDirectory(destDirPath) && !isExist(destDirPath))
            throw new Exception('There is No Directory. OR Maybe, It Failed To Create Directory.')
    }

    boolean mkdirs(String path){
        boolean isOk = false
        List<String> list = path.split('[/]').toList()
        list.eachWithIndex{ String directoryName, int i ->
            String checkPath = '/' + (list[0..i].join('/'))
            if (!isExist(checkPath)){
                isOk = mkdir(checkPath)
                logger.debug "Created Directory: ${path}"
            }
        }
        return isOk
    }

    /*************************
     *
     * subfilepathlist
     *
     *************************/
    Vector<ChannelSftp.LsEntry> getSubFilePathList(String rootPath, String sourcePath){
        Vector<ChannelSftp.LsEntry> filePathList
        String fileName = new File(sourcePath).getName()
        try{
            if (isDirectory(sourcePath)){
                filePathList = ls(rootPath)
                filePathList = filePathList.findAll{ it.getFilename() == fileName }
            }else{
                filePathList = ls(sourcePath)
            }
        }catch(Exception e){
            //No Files
        }
        return filePathList
    }

    /*************************
     *
     * recursive entry list
     *
     *************************/
    Map<String, ChannelSftp.LsEntry> getEntryMap(String sourcePath){
        Map<String, ChannelSftp.LsEntry> newEntryMap = [:]
        sourcePath = FileMan.toSlash(new File(sourcePath).getPath())
        //- Get Entry's Root Path Length
        int entryRootStartIndex = 0
        String rootPath = ''
        String ParentPath = ''
        if (!FileMan.isRootPath(sourcePath)){
            rootPath = FileMan.toSlash(new File(sourcePath).getParentFile().getPath())
            ParentPath = FileMan.toSlash(new File(sourcePath).getParentFile().getPath())
            if (FileMan.isRootPath(ParentPath)){
                entryRootStartIndex = rootPath.length()
            }else{
                entryRootStartIndex = rootPath.length() +1
            }
        }
        //- Get Entries
        Vector<ChannelSftp.LsEntry> filePathList = getSubFilePathList(rootPath, sourcePath)
        filePathList.each{ ChannelSftp.LsEntry lsEntry ->
            String onePath = rootPath +'/'+ lsEntry.getFilename()
            if (FileMan.isMatchedPath(onePath, sourcePath))
                newEntryMap = getEntryMap(newEntryMap, entryRootStartIndex, onePath, lsEntry)
        }
        return newEntryMap
    }

    Map<String, ChannelSftp.LsEntry> getEntryMap(Map<String, ChannelSftp.LsEntry> entryMap, int entryRootStartIndex, String filePath, ChannelSftp.LsEntry lsEntry){
        String oneFilePath = filePath.substring(entryRootStartIndex, filePath.length())
        if (!['.', '..'].contains(lsEntry.getFilename())){
            if (lsEntry.getAttrs().isDir()){// check for a folder
                entryMap[oneFilePath] = lsEntry
                java.util.Vector<ChannelSftp.LsEntry> list = ls(filePath)
                for (ChannelSftp.LsEntry entry : list) {
                    getEntryMap(entryMap, entryRootStartIndex, (filePath +'/'+ entry.getFilename()), entry)
                }
            }else{
                entryMap[oneFilePath] = lsEntry
            }
        }
        return entryMap
    }



}