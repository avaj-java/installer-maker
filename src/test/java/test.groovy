import com.jaemisseo.man.FileMan
import com.jaemisseo.man.VariableMan
import groovy.json.JsonSlurper
import install.task.TaskTestPort
import org.junit.Test

/**
 * Created by sujkim on 2017-02-26.
 */
class test {


    @Test
    void "hello Test"(){

        FileMan fileman = new FileMan('c:/Users/sujkim/installer.properties').read()

        String json = '{"datasource.main.url":"jdbc:oracle:thin:@$METAPROP_DB_URL_VAL_IP:$METAPROP_DB_URL_VAL_PORT:$METAPROP_DB_URL_VAL_SID", "datasource.main.user":"$PROP_USER_VAL", "datasource.main.password":"$PROP_PW_VAL"}'
        println new JsonSlurper().parseText(json)

        Map replacePropMap = [
                'install.level.f3.action':'SUJKIM SEXY MAN',
                'install.level.f4.action':'SUJKIM SEXY Huh'
        ]
        Map replaceLineMap = [   :
//                'install.level.f3.file':'WOW WOW WOW',
        ]
        Map replaceMap = [
                'dir':'dddddd',
        ]
        println fileman.replaceProperty(replacePropMap).replaceLine(replaceLineMap).replace(replaceMap).getContent()


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
    void "Check Available Port"(){
        new TaskTestPort().run([:])
    }


    @Test
    void "range"(){
        (14..20).each{
            println it
        }
    }

    @Test
    void "scanner"(){
        Scanner scan = new Scanner(System.in)
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
        println "하이"
        String val = scan.next()
//        String val = br.readLine()
        println "키키 ${val}"
    }

    @Test
    void "Test varman"(){
        println new VariableMan([
                'was.home':'~/ds/metastream/apache-tomcat-7.0.47',
                'program.home':'~/ds/metastream',
                'was.contextpath':'metastream',
        ]).parse('${was.home}/webapps/${was.contextpath}')
    }

    @Test
    void "Test *"(){
        println FileMan.getFilePathList('~/*')
    }


}
