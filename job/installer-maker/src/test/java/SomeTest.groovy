import com.jaemisseo.hoya.bean.QuestionSetup
import jaemisseo.man.FileMan
import jaemisseo.man.VariableMan
import jaemisseo.man.bean.FileSetup
import org.junit.Test

class SomeTest {

    @Test
    void someTest(){
        List a = new QuestionSetup().makeFieldNameList()
        println a

        List b = new FileSetup().makeFieldNameList()
        println b
    }


    @Test
    void simpleTest(){
        List fileSystemPriorityPathList = ['~/reinjector/files.dat', './hello.txt']
        fileSystemPriorityPathList.any{ String setupFilePath ->
            //1
            File foundFile = FileMan.findFromApp(setupFilePath)
            //2
            String libDir = new VariableMan().parse('${_lib.dir}')
            File foundFile2 = FileMan.find(libDir, setupFilePath)

            if (foundFile){
                int cnt = 0
                foundFile.text.eachLine{
                    println "${++cnt}) ${it}"
                }
                return false
            }
            return false
        }
    }

    @Test
    void fileVariableTest(){
        VariableMan varman = new VariableMan().putVariableClosures([
                'FILE': { VariableMan.OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members){
                        String filePath = VariableMan.parseMember(it.members[0], vsMap, vcMap)
                        it.substitutes = FileMan.getStringFromFile(filePath)
                    }
                },
                'LISTFILE': { VariableMan.OnePartObject it, Map<String, Object> vsMap, Map<String, Closure> vcMap ->
                    if (it.members){
                        String filePath = VariableMan.parseMember(it.members[0], vsMap, vcMap)
                        it.originalValue = FileMan.getListFromFile(filePath)
                        it.substitutes = it.originalValue
                    }
                }
        ])
        File file = FileMan.getFileFromResource('text-test.txt')
        String filePath = file.path
        String result = varman.parse('${listfile("' +filePath+ '").join(" -hehe- ")}')
        println result
    }


}
