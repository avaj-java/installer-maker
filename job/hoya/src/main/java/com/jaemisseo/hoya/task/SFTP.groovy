package com.jaemisseo.hoya.task


import com.jcraft.jsch.JSchException
import com.jcraft.jsch.SftpException
import com.jaemisseo.hoya.bean.FileSetup
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Document
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import com.jaemisseo.hoya.util.SFTPMan

/**
 * Created by sujkim on 2017-02-22.
 */

@Document("""
Upload and Download with SFTP

<url> 
  username/password@host:port:path   
  ex) username/password@127.0.0.1:22:~/path/to/do/something

<usage>
  ex) hoya -sftp upload /local/path/to/file username/password@127.0.0.1:22:/server/path/to/file        
  ex) hoya -sftp download username/password@127.0.0.1:22:/server/path/to/file /local/path/to/file
  ex) hoya -sftp -method=list -url=username/password@127.0.0.1:22:/server/path/to/file
  ex) hoya -sftp -method=entrylist -url=username/password@127.0.0.1:22:/server/path/to/file
  
  hoya -sftp -method=upload -id=ID -pw=PW -host=192.168.3.52 -from=/d/my_local_path/some_file.zip -to=//home/meta/sj-test
  hoya -sftp -method=download -id=ID -pw=PW -host=192.168.3.52 -from=//home/meta/sj-test -to=/d/my_local_path/some_file.zip

""")
@Task
@TerminalValueProtocol(['method', 'param1', 'param2'])
class SFTP extends TaskHelper{

    @Value(name='method', required=true, caseIgnoreValidList=['UPLOAD','DOWNLOAD','LIST','ENTRYLIST'])
    String method

    @Value(name='param1')
    String param1

    @Value(name='param2')
    String param2

    @Value(name='id')
    String id

    @Value(name='pw')
    String pw

    @Value(name='from')
    String from

    @Value(name='to')
    String to

    @Value(name='host')
    String host

    @Value(name='port')
    Integer port

    @Value
    FileSetup fileSetup

    @Value(name='mode.progress.bar', value="true") //TODO: Default Valuerk 기능이 없었나?
    Boolean modeProgressBar




    @Override
    Integer run(){
        SFTPMan sftpman = new SFTPMan().setModeProgressBar(modeProgressBar?:true)
        String id = id
        String pw = pw
        String host = host
        int port = port ?: 22
        String from = from
        String to = to

        //TODO: 옵션 정리 필요..
//        if (param1){
//            //Connection Expression
//            int seperatorAllIndex = param1.lastIndexOf('@')
//            logger.debug("Check1 - url:$param1  sep:$seperatorAllIndex")
//            String idPw = param1.substring(0, seperatorAllIndex)
//            logger.debug("Check2 - IDPW: ${idPw}")
//            String hostPortPath = param1.substring(seperatorAllIndex+1, param1.length())
//            logger.debug("Check3 - hostPortPath: ${hostPortPath}")
//            //Connection Expression 2
//            List<String> idPwlist = idPw.split('[/]').toList()
//            id = idPwlist[0]
//            pw = idPwlist[1] ? idPwlist[1..(idPwlist.size()-1)].join('/') : id
//            //Connection Expression 3
//            List<String> hostPortPathlist = hostPortPath.split(':').toList()
//            host = hostPortPathlist[0] ?: 'localhost'
//            port = hostPortPathlist[1] ? hostPortPathlist[1].toInteger() : 22
//            to = hostPortPathlist[2] ?: '~/'
//        }
        logger.debug("Check4 - ID:$id  PW:$pw  host:$host  port:$port  path:$to")
        sleep(1000)

        try{
            //Connect
            sftpman.connect(id, pw, host, port)
            //Do
            switch(method.toUpperCase()){
                case SFTPMan.UPLOAD:
                    sftpman.upload(from, to, fileSetup)
                    break
                case SFTPMan.DOWNLOAD:
                    sftpman.download(to, param2, fileSetup)
                    break
                case SFTPMan.LIST:
                    sftpman.printLs(to)
                    break
                case SFTPMan.ENTRYLIST:
                    sftpman.printRecursiveLs(to)
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
