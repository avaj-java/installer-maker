package install.bean

import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Bean

/**
 * Created by sujkim on 2017-03-18.
 */
@Bean
class QuestionSetup extends jaemisseo.man.bean.QuestionSetup {

    @Value(name='desc', required=true, modeRenderJansi=true)
    String question
    @Value('color.question')
    String questionColor

    @Value('answer')
    String answer
    @Value('answer.default')
    String recommandAnswer
    @Value('answer.validation')
    String validation       //Not Supported Yet
    @Value('answer.options')
    Map descriptionMap
    @Value('answer.values')
    Map valueMap
    @Value('answer.repeat.limit')
    Integer repeatLimit = 1

    @Value('mode.only.interactive')
    Boolean modeOnlyInteractive
    @Value('mode.load.rsp')
    Boolean modeLoadResponseFile
}
