package install.configuration

import jaemisseo.man.configuration.Config
import jaemisseo.man.configuration.annotation.type.Data
import jaemisseo.man.configuration.data.PropertyProvider
import org.junit.Test

/**
 * Created by sujkim on 2017-06-24.
 */
class ConfigTest {

    Config config = new Config()

    @Test
    void SimplTest(){
        config.scan('jaemisseo')
        PropertyProvider provider = (config.findInstanceByAnnotation(Data) as PropertyProvider)
        provider.propGen = config.propGen
        config.inject()
    }

}
