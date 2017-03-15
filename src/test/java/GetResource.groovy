import com.jaemisseo.man.PropMan
import org.junit.Test

/**
 * Created by sujkim on 2017-03-04.
 */
class GetResource {


    @Test
    void "getResource"(){
        URL url = Thread.currentThread().getContextClassLoader().getResource('installer.properties')
        println url
        println url.getFile()
        println new File(url.getFile())
        println new File(url.getFile()).text
//        new PropMan().readResource('installer.properties')
    }

    @Test
    void "file list test"(){
        File file = new File(".")
        if (file.isDirectory()){
            File[] files = file.listFiles()
            for (int i = 0; i < files.length; i++) {
                files[i].path
            }
        }else{
            println file.path
        }
    }
}
