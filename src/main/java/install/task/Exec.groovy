package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.util.TaskUtil
import jaemisseo.man.util.Util

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
            commandWIthStyleB(userCommand)

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
        builder.redirectErrorStream(true);
        Process process = builder.start()
        doProcess(process)
    }

    void doProcess(Process process){
//        List<String> lineList = new BufferedReader(new InputStreamReader(process.getInputStream())).readLines()
//        lineList.each{
//            println it
//        }
        Util.newThread {
            OutputStream stdin = process.getOutputStream(); // <- Eh?
            InputStream stdout = process.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));

            Scanner scan = new Scanner(stdout);
            String line

            while (scan.hasNext()) {
                String input = scan.nextLine();
                if (input.trim().equals("exit")) {
                    // Putting 'exit' amongst the echo --EOF--s below doesn't work.
                    writer.write("exit\n");
                } else {
//                String dd = new Scanner(System.in).nextLine();
//                writer.write(dd)
                    writer.write("((" + input + ") && echo --EOF--) || echo --EOF--\n");
                }
                writer.flush();

                line = reader.readLine();
                while (line != null && !line.trim().equals("--EOF--")) {
                    java.lang.System.out.println(line);
                    line = reader.readLine();
                }
                if (line == null) {
                    break;
                }
            }
        }

        int exitCode = process.waitFor()
        assert exitCode == 0
    }

}
