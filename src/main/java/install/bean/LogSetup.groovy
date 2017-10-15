package install.bean

import install.configuration.annotation.Value
import install.configuration.annotation.type.Bean

@Bean
class LogSetup {

    @Value(name='log.dir', filter='getFilePath')
    String logDir

    @Value('log.file.name')
    String logFileName

    @Value('log.level.console')
    String logLevelConsole

    @Value('log.level.file')
    String logLevelFile

}
