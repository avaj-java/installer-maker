package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.util.TaskUtil
import temp.util.StreamGobbler

import java.util.concurrent.Executors

/**
 * Created by sujkim on 2017-04-04.
 */
@Task
class Exec extends TaskUtil{

    @Value(property='exec.command')
    String commandForAll

    @Value(property='exec.command.lin')
    String commandForLin

    @Value(property='exec.command.win')
    String commandForWin

    @Value(property='os.name', method='getString')
    void setIsWindows(String osName){
        this.isWindows = osName ? osName.toLowerCase().startsWith("windows") : false
    }

    boolean isWindows

    @Value(property='user.dir', method='getString')
    String userDir



    @Override
    Integer run(){
        println "<Exec>"

        try{
            //Get Command
            String userCommand = (commandForAll) ? commandForAll : (isWindows) ? commandForWin : commandForLin

            //Exec Command
            commandWIthStyleA(userCommand)

        }catch(e){
            throw e
        }

        return STATUS_TASK_DONE
    }



    void commandWIthStyleA(String userCommand){
        //Data
        String command = (isWindows) ? "cmd.exe /c ${userCommand}" : "sh -c ${userCommand}"
        println command

        //Execute Command
        Process process = Runtime.getRuntime().exec("${command} ${userDir}")
        doProcess(process)
    }

    void commandWIthStyleB(String userCommand){
        //Data
        String command = (isWindows) ? "cmd.exe /c ${userCommand}" : "sh -c ${userCommand}"
        String[] commandArray = command.split(/\s+/)
        println command

        //Execute Command
        ProcessBuilder builder = new ProcessBuilder().command(commandArray).directory( new File(userDir) )
        Process process = builder.start()
        doProcess(process)
    }

    void doProcess(Process process){
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream())
        Executors.newSingleThreadExecutor().submit(streamGobbler)
        int exitCode = process.waitFor()
        assert exitCode == 0
    }

}
