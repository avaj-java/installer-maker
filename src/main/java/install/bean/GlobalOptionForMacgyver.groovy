package install.bean

import install.configuration.annotation.Value
import install.configuration.annotation.type.Bean
import jaemisseo.man.util.Option

/**
 * Created by sujkim on 2017-02-19.
 */
@Bean
class GlobalOptionForMacgyver extends Option{

    @Value
    FileSetup fileSetup
    @Value
    ReportSetup reportSetup
    @Value
    LogSetup logSetup

}
