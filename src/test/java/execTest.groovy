import install.task.Exec
import org.junit.Before
import org.junit.Test

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

        new Exec(
            commandForAll: null,
            commandForWin: 'dir',
            commandForLin: 'ls',
            isWindows: System.getProperty('os.name'),
            userDir: System.getProperty('user.home')
        ).run()

        println File.separator
    }


    

}
