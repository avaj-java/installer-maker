package install.bean

import install.util.TaskUtil
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Bean
import jaemisseo.man.util.Option

/**
 * Created by sujkim on 2017-03-18.
 */
@Bean
class TaskSetup extends Option {

    @Value(name='if', filter='parse')
    def condition

    @Value(name='ifoption', filter='parse')
    def conditionOption

    @Value(name='ifport', filter='parse')
    def conditionPort

    @Value('task')
    String taskTypeName

    @Value(name='desc', modeRenderJansi=true)
    String desc

    @Value('color.task')
    String color

    @Value('color.desc')
    String descColor

    @Value('variable.sign')
    String variableSign

    String jobName
    String commandName
    String taskName
    String propertyPrefix

    Class taskClazz
    TaskUtil taskInstance


}
