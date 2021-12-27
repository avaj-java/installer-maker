import groovy.json.JsonSlurper
import org.junit.Test

class GroovyTest {


    @Test
    void closureTest(){
        /** any **/
        boolean anyResult = (0..10).any{
            println it
            //false => continue
            //true => break (then, return true)
            return (0 <= it && it <= 5)
        }
        println anyResult

        /** every **/
        boolean everyResult = (0..10).every{
            println it
            //false => break (then, return false)
            //true => continue
            return (0 <= it && it <= 5)
        }
        println everyResult
    }


    @Test
    void parseJsonTest(){
        String json = '''{
            "1" : "1",            
            "4" : "4",            
            "3" : "3",            
            "2" : "2",
            "5" : "5"
        }'''


        println new JsonSlurper().parseText(json)

        //
        def jsonText = '''
        {"A":0,"B":0,"C":2,"D":0,"F":5,"G":7,"H":9}
        '''

        LinkedHashMap json2 = new JsonSlurper().parseText(jsonText)
        json2.each{ entry ->
            println "${entry.key}"
        }
    }


    @Test
    void stringList(){
        String content = """
            hahah
                hohoho hoho
             hoho   ho  ho
        """
        println content
        println '-----'
        println content.trim()
        println '-----'


        List<String> stringList = content.split('\n').toList()
        Integer shortestIndentIndex = null
        List<String> resultStringList = stringList.findAll{
            List charList = it.toList()
            int indentIndex = 0
            for (int i=0; i<charList.size(); i++){
                if (charList[i] != " "){
                    indentIndex = i
                    break
                }
            }
            if (indentIndex > 0){
                if (!shortestIndentIndex || shortestIndentIndex > indentIndex){
                    shortestIndentIndex =  indentIndex
                }
                return true
            }else{
                return false
            }
        }
        String resultString = resultStringList.collect{ it.substring(shortestIndentIndex) }.join('\n')
        println resultString
    }

    @Test
    void parseTest(){
        String a = '''[
            "${var.data.dir}/sql/${sql.vendor}/kics/02.create_batch_ddl.sql"
            ,"${var.data.dir}/sql/${sql.vendor}/kics/02.create_ddl.sql"
            ,"${var.data.dir}/sql/${sql.vendor}/kics/02.create_goyOrn_dll.sql"
            ,"${var.data.dir}/sql/${sql.vendor}/kics/02.create_sequence_ddl.sql"
            ,"${var.data.dir}/sql/${sql.vendor}/kics/02.UUID_java_function.sql"
        ]'''

        String b = '''[
            "D:\\\\dev_by_sj\\\\workspaces\\\\metastream3-develop\\\\build\\\\installer_myproject\\\\data\\\\sql\\\\oracle\\\\kics\\\\02.create_batch_ddl.sql"
            ,"D:\\\\dev_by_sj\\\\workspaces\\\\metastream3-develop\\\\build\\\\installer_myproject\\\\data\\\\sql\\\\oracle\\\\kics\\\\02.create_ddl.sql"
            ,"D:\\\\dev_by_sj\\\\workspaces\\\\metastream3-develop\\\\build\\\\installer_myproject\\\\data\\\\sql\\\\oracle\\\\kics\\\\02.create_goyOrn_dll.sql"
            ,"D:\\\\dev_by_sj\\\\workspaces\\\\metastream3-develop\\\\build\\\\installer_myproject\\\\data\\\\sql\\\\oracle\\\\kics\\\\02.create_sequence_ddl.sql"
            ,"D:\\\\dev_by_sj\\\\workspaces\\\\metastream3-develop\\\\build\\\\installer_myproject\\\\data\\\\sql\\\\oracle\\\\kics\\\\02.UUID_java_function.sql"
        ]'''

        String c = '''
           ["${}asdfasdf", "${asadf}"]     
        '''


        List list = new JsonSlurper().parseText(c)
        println list

    }

}
