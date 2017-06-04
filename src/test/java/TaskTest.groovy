import jaemisseo.man.util.Util
import org.junit.Test

/**
 * Created by sujkim on 2017-06-02.
 */
class TaskTest {

    @Test
    void taks_test(){
        Util.findAllClasses('install.task').each { println it }
        Util.findAllClasses('jaemisseo.man').each { println it }
    }


}
