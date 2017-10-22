package install.bean

import install.configuration.annotation.Value
import install.configuration.annotation.type.Bean
import jaemisseo.man.util.Option

/**
 * Created by sujkim on 2017-03-18.
 */
@Bean
class TaskSetup extends Option {

    @Value(name='if', filter='parse')
    def condition

    @Value(name='desc', modeRenderJansi=true)
    String desc

    @Value('color')
    String color

    @Value('descColor')
    String descColor

    @Value('task')
    String taskName

    String jobName
    Class taskClazz
    String propertyPrefix

}
