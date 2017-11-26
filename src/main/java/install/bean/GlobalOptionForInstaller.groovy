package install.bean

import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Bean
import jaemisseo.man.util.Option

/**
 * Created by sujkim on 2017-02-19.
 */
@Bean
class GlobalOptionForInstaller extends Option{

    @Value('remember.answer.file.path')
    String rememberFilePath
    @Value('rsp')
    String responseFilePath

    @Value('mode.remember.answer')
    Boolean modeRemember

    @Value(prefix='remember.answer.')
    FileSetup rememberFileSetup



    @Value
    FileSetup fileSetup
    @Value
    ReportSetup reportSetup
    @Value
    LogSetup logSetup

}
