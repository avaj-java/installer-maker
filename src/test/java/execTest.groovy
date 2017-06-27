import install.task.Exec
import org.junit.Before
import org.junit.Test
import temp.util.StreamGobbler

import java.util.concurrent.Executors

/**
 * Created by sujkim on 2017-06-27.
 */
class execTest {

    boolean isWindows

    @Before
    void before(){
        isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    }



    @Test
    void simpleTest(){
        /////
//        testStyleCommand()
//        /////
//        testStyleJVM()

        new Exec(
            commandForAll: 'dir',
            isWindows: System.getProperty('os.name'),
            userDir: System.getProperty('user.home')
        ).run()

        println File.separator
    }

    void testStyleCommand(){
        String homeDirectory = System.getProperty("user.home");
        String command = (isWindows) ? "cmd.exe /c dir" : "sh -c ls"

        Process process = Runtime.getRuntime().exec("${command} ${homeDirectory}");
        doProcess(process)
    }

    void testStyleJVM(){
        String homeDirectory = System.getProperty("user.home");
        String command = (isWindows) ? "cmd.exe /c dir" : "sh -c ls"
        String[] commandArray = command.split(/\s+/)
        
        ProcessBuilder builder = new ProcessBuilder().command(commandArray).directory( new File(homeDirectory) );
        Process process = builder.start();
        doProcess(process)
    }

    void doProcess(Process process){
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream());
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;
    }


    

}
