package install.bean

import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Bean

/**
 * Created by sujkim on 2017-02-19.
 */
@Bean
class FileSetup extends jaemisseo.man.bean.FileSetup {

    @Value('file.encoding')
    String encoding = 'utf-8'
    @Value('file.backup.path')
    String lineBreak
    @Value('file.linebreak')
    String lastLineBreak = ''
    @Value('file.last.linebreak')
    String backupPath

    @Value('mode.auto.mkdir')
    Boolean modeAutoMkdir = false
    @Value('mode.auto.backup')
    Boolean modeAutoBackup = false
    @Value('mode.auto.overwrite')
    Boolean modeAutoOverWrite = false

}
