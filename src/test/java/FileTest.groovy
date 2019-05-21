import install.bean.FileSetup
import jaemisseo.man.FileMan
import org.junit.Test

class FileTest {

    @Test
    void comparingFiles(){

        String fileA = '/test/sample.text'
        String fileB = '/test/sample-copy.text'

        FileMan.write(fileA, 'hello', new FileSetup(modeAutoMkdir: true, modeAutoOverWrite: true))
        FileMan.write(fileB, 'hello2', new FileSetup(modeAutoMkdir: true, modeAutoOverWrite: true))

        FileMan.isChanged(fileA, fileB)
    }

}
