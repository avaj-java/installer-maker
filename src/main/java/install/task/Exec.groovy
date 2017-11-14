package install.task

import install.configuration.annotation.HelpIgnore
import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.configuration.annotation.type.TerminalValueProtocol
import install.util.TaskUtil
import jaemisseo.man.util.Util

/**
 * Created by sujkim on 2017-04-04.
 */
@Task
@TerminalValueProtocol(['command'])
class Exec extends TaskUtil{

    @Value('command')
    List<String> commandForAllList

    @Value('command.lin')
    List<String> commandForLinList

    @Value('command.win')
    List<String> commandForWinList

    @HelpIgnore
    @Value('os.name')
    void setIsWindows(String osName){
        this.isWindows = osName ? osName.toLowerCase().startsWith("windows") : false
    }

    boolean isWindows

    @HelpIgnore
    @Value('user.dir')
    String userDir



    @Override
    Integer run(){
        try{
            //Get Command
            List<String> userCommandList = (commandForAllList) ? commandForAllList : (isWindows) ? commandForWinList : commandForLinList

            //Exec Command
            userCommandList.each{ String command ->
                commandWIthStyleB(command)
            }

        }catch(e){
            throw e
        }

        logMiddleTitle 'FINISHED EXEC'

        return STATUS_TASK_DONE
    }



    void commandWIthStyleA(String userCommand){
        //Data
        String command = (isWindows) ? "cmd.exe /c ${userCommand}" : "sh -c ${userCommand}"
        logger.debug "Command> ${command}"

        //Execute Command
        Process process = Runtime.getRuntime().exec("${command} ${userDir}")
        doProcess(process)
    }

    void commandWIthStyleB(String userCommand){
        //Data
        String command = (isWindows) ? "cmd.exe /c ${userCommand}" : "sh -c ${userCommand}"
        String[] commandArray = command.split(/\s+/)
        logger.debug "Command> ${command}"

        //Execute Command
        ProcessBuilder builder = new ProcessBuilder().command(commandArray).directory( new File(userDir) )
        builder.redirectErrorStream(true);
        Process process = builder.start()
        doProcess(process)
    }

    void doProcess(Process process){
        Util.newThread {
            OutputStream stdin = process.getOutputStream(); // <- Eh?
            InputStream stdout = process.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));

            //Method 1. Print by Character
            int c
            while ((c = reader.read()) != -1) {
                print ((char) c)
            }
            reader.close()
            
            //Method 2. Print by Line
//            String line
//            line = reader.readLine()
//            while (line != null && !line.trim().equals("--EOF--")){
//                println line
//                line = reader.readLine()
//            }
        }

        int exitCode = process.waitFor()
        assert exitCode == 0
    }

}
