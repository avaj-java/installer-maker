package install.bean

import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Bean

@Bean
class LogSetup {

    @Value('log.level.console')
    String logLevelConsole

    @Value('log.level.file')
    String logLevelFile

    @Value(name='log.dir', filter='getFilePath')
    String logDir

    @Value('log.file.name')
    String logFileName

}
