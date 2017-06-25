package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.util.TaskUtil

/**
 * Created by sujkim on 2017-04-04.
 */
@Task
class Exec extends TaskUtil{

    @Value(property='sh.file.path', method='getFilePath')
    String shFilePath

    @Value(property='bat.file.path', method='getFilePath')
    String batFilePath

    @Value(property='os.name', method='getString')
    String osName



    @Override
    Integer run(){
        println "<Run SH or BAT>"
        StringBuffer output = new StringBuffer()
        String command

        try{
            //Gen Command
            command = osName.contains('Win') ? "cmd /c start ${batFilePath}" : "${shFilePath}"
            //Exec
            Process p = Runtime.getRuntime().exec(command)
            p.waitFor()
            //Log
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))
            String line = ""
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n")
            }
        }catch(e){
            throw e
        }

        return STATUS_TASK_DONE
    }

}
