package install.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpException
import jaemisseo.man.FileMan;

class SFTPMan{

    static final String UPLOAD = "UPLOAD"
    static final String DOWNLOAD = "DOWNLOAD"
    static final String RM = "RM"
    static final String RMDIR = "RMDIR"
    static final String MKDIR = "MKDIR"
    static final String RENMAE = "RENAME"
    static final String LS = "LS"

    Session session = null;
    Channel channel = null;
    ChannelSftp sftp = null;

    /*****
     * Connect
     *****/
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

    /*****
     * Disconnect
     *****/
    SFTPMan disconnect(){
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
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
     * Upload
     *****/
    SFTPMan upload(String from, String to){
        //from
        File fileFrom = new File(from);
        //to
        File fileTo = new File(to);
        String lastDirPath = FileMan.getLastDirectoryPath(to).replaceAll(/[\/\\]+/, "/")
        cd(lastDirPath);
        sftp.put(new FileInputStream(fileFrom), fileTo.getName());
        return this
    }

    /*****
     * Download
     *****/
    SFTPMan download(String from, String to){
        InputStream is = null;
        FileOutputStream fos = null;

        //from
        File fileFrom = new File(from);
        try {
            String lastDirPath = FileMan.getLastDirectoryPath(from).replaceAll(/[\/\\]+/, "/")
            String fileName = FileMan.getLastFileName(from)
            cd(lastDirPath);
            is = sftp.get(fileName);
        }catch(SftpException e){
            throw e
        }

        //to
        File fileTo = new File(to);
        try{
            fos = new FileOutputStream(fileTo);
            int i;
            while ((i = is.read()) != -1){
                fos.write(i);
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            try{
                fos.close();
                is.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
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

}