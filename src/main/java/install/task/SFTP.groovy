package install.task

import com.jcraft.jsch.JSchException
import com.jcraft.jsch.SftpException
import install.bean.FileSetup
import install.configuration.annotation.Value
import install.configuration.annotation.type.Task
import install.configuration.annotation.type.TerminalValueProtocol
import install.util.SFTPMan
import install.util.TaskUtil

/**
 * Created by sujkim on 2017-02-22.
 */
@Task
@TerminalValueProtocol(['url', 'method', 'param'])
class SFTP extends TaskUtil{

    @Value(name='url')
    String url

    @Value(name='method', required=true)
    String method

    @Value(name='param')
    String param

    @Value
    FileSetup fileSetup




    @Override
    Integer run(){
        SFTPMan sftpman = new SFTPMan()
        String id
        String pw
        String host
        int port
        String path

        //Connection Expression
        int seperatorAllIndex = url.indexOf('@')
        String idPw = url.substring(0, seperatorAllIndex)
        String hostPortPath = url.substring(seperatorAllIndex+1, url.length())
        //Connection Expression 2
        List<String> idPwlist = idPw.split('[/]').toList()
        id = idPwlist[0]
        pw = idPwlist[1] ? idPwlist[1..(idPwlist.size()-1)].join('/') : id
        //Connection Expression 3
        List<String> hostPortPathlist = hostPortPath.split(':').toList()
        host = hostPortPathlist[0] ?: 'localhost'
        port = hostPortPathlist[1] ? hostPortPathlist[1].toInteger() : 22
        path = hostPortPathlist[2] ?: '~/'

        try{
            //Connect
            sftpman.connect(id, pw, host, port)
            //Do
            switch(method.toUpperCase()){
                case SFTPMan.UPLOAD:
                    sftpman.upload(param, path)
                    break
                case SFTPMan.DOWNLOAD:
                    sftpman.download(path, param)
                    break
                default:
                    break
            }

        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            sftpman.disconnect()
        }

        return STATUS_TASK_DONE
    }












}
