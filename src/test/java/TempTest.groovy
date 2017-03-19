import com.jaemisseo.man.FileMan
import com.jaemisseo.man.VariableMan
import groovy.json.JsonSlurper
import install.task.TaskTestPort
import org.junit.Test

/**
 * Created by sujkim on 2017-02-26.
 */
class TempTest {


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
    void "range"(){
        (14..20).each{
            println it
        }
    }



}
