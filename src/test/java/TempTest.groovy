import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import org.junit.Test

import java.text.SimpleDateFormat

/**
 * Created by sujkim on 2017-02-26.
 */
class TempTest {

    String sep = System.getProperty("line.separator")

    @Test
    void "hello Test"(){
    }

    @Test
    void "test temp"(){
        List<String> funcs = ['d','s','j','k(','i']
        int funcStartIndex = funcs.findIndexOf{ it.indexOf('(') != -1 }
        String variable = funcs[0..funcStartIndex-1].join('.')
        List list = [variable] + funcs[funcStartIndex..funcs.size()-1]
        println list

    }

    @Test
    void "date format"(){
        println "${new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())}"
    }


    @Test
    void "groovy range"(){
        (14..20).each{
            println it
        }

        ('f4'..'f9').each{
            println it
        }
    }

    @Test
    void "compress extract"(){
//        String sourcePath = 'D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\lib\\temp\\*'
//        String destPath = 'D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\test.zip'
//        FileMan.zip(sourcePath, destPath, true)
//
//        String sourcePath2 = 'D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\lib\\temp\\*'
//        String destPath2 = 'D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\test.jar'
//        FileMan.jar(sourcePath2, destPath2, true)
//
//        String sourcePath3 = 'D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\lib\\temp\\*'
//        String destPath3 = 'D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\test.tar.gz'
//        FileMan.tar(sourcePath3, destPath3, true)
//
//        FileMan.unzip(destPath)
//        FileMan.unjar(destPath2)
//        FileMan.untar(destPath3)
    }

    @Test
    void "file path range test"(){
        String path = "D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\*"
        List<String> entryList = FileMan.getSubFilePathList(path)
        println entryList.size()
        println path
        entryList.each{ println it }
        println ""

        String path2 = "D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\*te*.*ja*"
        List<String> entryList2 = FileMan.getSubFilePathList(path2)
        println entryList2.size()
        println path2
        entryList2.each{ println it }
        println ""

    }


    @Test
    void "exec test"(){
        //Ready
//        String filePath = "D:\\dev_by_sj\\workspaces\\metastream-gradle\\build\\installer_myproject\\bin\\install.bat"
        String filePath = "/D/dev_by_sj/workspaces/metastream-gradle/build/installer_myproject/bin/install"

        //DO
        println "<Run SH or BAT>"
        StringBuffer output = new StringBuffer()
        String command = ""

        try{
            //Check OS
            Boolean isWin = false
            if (isWin){
                command = "cmd /c start ${filePath}"
            }else{
                command = "${filePath}"
            }
            //Exec
            Process p = Runtime.getRuntime().exec(command)
            p.waitFor()
            //Log
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))
            String line = ""
            while ((line = reader.readLine())!= null) {
                output.append(line + sep)
            }
        }catch(e){
        }

        println output
    }

    @Test
    void "as Boolean"(){
        println "1" as Boolean
        println "0" as Boolean
        println "true" as Boolean
        println "false" as Boolean
        println null as Boolean
        println "" as Boolean
        println "tue" as Boolean
        println "flse" as Boolean

        PropMan propman = new PropMan([aaa:''])

        println propman.getBoolean('aaa')
        println propman.getBoolean('bbb')
        println propman.getBoolean('ddd')

    }

}
