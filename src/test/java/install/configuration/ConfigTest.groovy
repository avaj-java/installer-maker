package install.configuration

import install.configuration.annotation.type.Data
import install.data.PropertyProvider
import org.junit.Test

/**
 * Created by sujkim on 2017-06-24.
 */
class ConfigTest {

    Config config = new Config()

    @Test
    void SimplTest(){
        config.scan()
        PropertyProvider provider = (config.findInstanceByAnnotation(Data) as PropertyProvider)
        provider.propGen = config.propGen
        config.inject()
    }

}
