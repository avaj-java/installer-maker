package install.bean

import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Bean
import jaemisseo.man.util.Option

/**
 * Created by sujkim on 2017-02-19.
 */
@Bean
class GlobalOptionForInstallerMaker extends Option{

    @Value('installer.name')
    String installerName
    @Value('installer.home.to.lib.relpath')
    String installerHomeToLibRelPath = './lib'
    @Value('installer.home.to.bin.relpath')
    String installerHomeToBinRelPath = './bin'
    @Value('installer.home.to.rsp.relpath')
    String installerHomeToRspRelPath = './rsp'
    @Value(name='build.dir', filter='getFilePath')
    String buildDir
    @Value(name='build.temp.dir', filter='getFilePath')
    String buildTempDir
    @Value(name='build.dist.dir', filter='getFilePath')
    String buildDistDir
    @Value(name='build.installer.home', filter='getFilePath')
    String buildInstallerHome
    @Value('properties.dir')
    String propertiesDir = './'

    @Value('mode.auto.rsp')
    Boolean modeAutoRsp
    @Value('mode.auto.zip')
    Boolean modeAutoZip
    @Value('mode.auto.tar')
    Boolean modeAutoTar

    @Value
    FileSetup fileSetup
    @Value
    ReportSetup reportSetup
    @Value
    LogSetup logSetup

}
