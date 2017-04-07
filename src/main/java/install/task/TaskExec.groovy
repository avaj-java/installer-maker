package install.task

import com.jaemisseo.man.PropMan

/**
 * Created by sujkim on 2017-04-04.
 */
class TaskExec extends TaskUtil{

    TaskExec (PropMan propman){
        this.propman = propman
    }



    /**
     * RUN
     */
    void run(String propertyPrefix){

        //Ready
        String shFilePath = getFilePath(propertyPrefix, 'sh.file.path')
        String batFilePath = getFilePath(propertyPrefix, 'bat.file.path')

        //DOｓ                        ｓ
        println "<Run SH or BAT>"
        StringBuffer output = new StringBuffer()
        String command

        try{
            //Check OS
            boolean isWin = propman.getString('os.name').contains('Win')
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
