import com.jaemisseo.install.application.InstallerMakerCliApplication
import jaemisseo.man.configuration.context.Command
import org.junit.Ignore
import org.junit.Test

class BuildTest {

    @Test
    @Ignore
    void test(){
        String[] args = ["build"] as String[]

        Command command = new Command(args);

        new InstallerMakerCliApplication().run(command);
    }



}
