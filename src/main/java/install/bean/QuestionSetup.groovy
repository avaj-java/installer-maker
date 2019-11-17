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

    Map descriptionMap

    Map valueMap

    @Value('answer.repeat.limit')
    Integer repeatLimit = 1

    @Value('mode.only.interactive')
    Boolean modeOnlyInteractive
    @Value('mode.load.rsp')
    Boolean modeLoadResponseFile


    @Value('answer.options')
    void setOptionList(List<String> optionObject){
        descriptionMap = descriptionMap ?: [:]
        if (optionObject instanceof List){
            optionObject.eachWithIndex{ String item, int i ->
                String selection
                String description
                int seperatorIdx = item.indexOf(')')
                if (seperatorIdx != -1){
                    selection = item.substring(0, seperatorIdx)
                    description = item.substring(seperatorIdx +1, item.length())
                }else{
                    selection = generateNewKeySeq(descriptionMap)
                    description = item
                }
                descriptionMap[selection] = description
            }
        }
    }

    @Value('answer.values')
    void setValueList(List<String> valueObject){
        valueMap = valueMap ?: [:]
        if (valueObject instanceof List){
            valueObject.eachWithIndex{ String item, int i ->
                String selection
                String value
                int seperator = item.indexOf(')')
                if (seperator != -1){
                    selection = item.substring(0, seperator)
                    value = item?.substring(seperator +2, item.length())
                }else{
                    selection = generateNewKeySeq(valueMap)
                    value = item
                }
                valueMap[selection] = value
            }
        }
    }

    String generateNewKeySeq(Map map){
        int count = 0
        while ( map.containsKey(String.valueOf(++count)) ){}
        return count
    }

}
