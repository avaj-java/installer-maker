import install.bean.QuestionSetup
import jaemisseo.man.FileMan
import jaemisseo.man.VariableMan
import jaemisseo.man.bean.FileSetup
import jaemisseo.man.util.Util
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

}
