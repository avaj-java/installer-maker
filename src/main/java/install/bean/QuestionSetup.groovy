package install.bean

import install.configuration.annotation.Value
import install.configuration.annotation.type.Bean

/**
 * Created by sujkim on 2017-03-18.
 */
@Bean
class QuestionSetup extends jaemisseo.man.bean.QuestionSetup {

    @Value(name='question', required=true, modeRenderJansi=true)
    String question
    @Value('questionColor')
    String questionColor

    @Value('answer')
    String answer
    @Value('answer.default')
    String recommandAnswer
    @Value('answer.validation')
    String validation       //Not Supported Yet
    @Value('answer.description.map')
    Map descriptionMap
    @Value('answer.value.map')
    Map valueMap
    @Value('answer.repeat.limit')
    Integer repeatLimit = 1

    @Value('mode.only.interactive')
    Boolean modeOnlyInteractive
    @Value('response.file.path')
    Boolean modeLoadResponseFile
}
