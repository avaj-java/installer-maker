package install.task

import com.jaemisseo.man.PropMan

/**
 * Created by sujkim on 2017-04-04.
 */
class TaskExec extends TaskUtil{

    @Override
    void run(){

        //Ready
        String shFilePath = getFilePath('sh.file.path')
        String batFilePath = getFilePath('bat.file.path')

        //DO
        println "<Run SH or BAT>"
        StringBuffer output = new StringBuffer()
        String command

        try{
            //Check OS
            boolean isWin = getString('os.name').contains('Win')
            //Gen Command
            command = (isWin) ? "cmd /c start ${batFilePath}" : "${shFilePath}"
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

    }

}
