package install.data

import install.configuration.annotation.Value
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Validator {

    final Logger logger = LoggerFactory.getLogger(this.getClass())



    boolean validate(def value, Value valueAnt){
        boolean isOk = true
        String errorMessage
        String propertyName = valueAnt.value() ?: valueAnt.name() ?:''
        String propertyNameLog = "Property: ${propertyName}"
        boolean required = valueAnt.required()
        boolean englishOnly = valueAnt.englishOnly()
        boolean numberOnly = valueAnt.numberOnly()
        boolean charOnly = valueAnt.charOnly()
        int minLength = valueAnt.minLength()
        int maxLength = valueAnt.maxLength()
        List<String> validList = valueAnt.validList().toList()
        List<String> contains = valueAnt.contains().toList()
        List<String> caseIgnoreValidList = valueAnt.caseIgnoreValidList().toList()
        List<String> caseIgnoreContains = valueAnt.caseIgnoreContains().toList()
        String regexp = valueAnt.regexp()

        if (required && value == null){
            errorMessage = "[${propertyNameLog}] Required value."
        }

        if (minLength > 0){
            if (value == null){
            }else if (value instanceof String && ((String)value).length() < minLength){
                errorMessage = "[${propertyNameLog}] '${value}', It is shorter than the minimum length. MIN:${minLength} But ${String.valueOf(value).length()}"
            }else if (value instanceof Integer && String.valueOf(value).length() < minLength){
                errorMessage = "[${propertyNameLog}] '${value}', It is shorter than the minimum length. MIN:${minLength} But ${String.valueOf(value).length()}"
            }else if (value instanceof List){
            }else if (value instanceof Map){
            }
        }

        if (maxLength > 0){
            if (value == null){
            }else if (value instanceof String && ((String)value).length() > maxLength){
                errorMessage = "[${propertyNameLog}] '${value}', Maximum length exceeded. MAX:${maxLength} But ${String.valueOf(value).length()}"
            }else if (value instanceof Integer && String.valueOf(value).length() > maxLength){
                errorMessage = "[${propertyNameLog}] '${value}', Maximum length exceeded. MAX:${maxLength} But ${String.valueOf(value).length()}"
            }else if (value instanceof List){
            }else if (value instanceof Map){
            }
        }

        if (englishOnly){
            if (value == null) {
            }else if (value instanceof String && !value.matches("[A-Za-z]+")){
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. Only English is available."
            }else if (value instanceof Integer){
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. Only English is available."
            }else if (value instanceof List){
            }else if (value instanceof Map){
            }
        }

        if (numberOnly){
            if (value == null){
            }else if (value instanceof String && !((String)value).isNumber()) {
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. Only numbers are allowed."
            }else if (value instanceof Integer){
            }else if (value instanceof List){
            }else if (value instanceof Map){
            }
        }

        if (charOnly){
            if (value == null){
            }else if (value instanceof String && !value.matches("\\D+")) {
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. Numbers are not allowed."
            }else if (value instanceof Integer){
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. Numbers are not allowed."
            }else if (value instanceof List){
            }else if (value instanceof Map){
            }
        }

        if (validList){
            if (value == null){
            }else if (value instanceof String && !validList.contains(value)) {
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. Valid values are [${validList.join(', ')}]"
            }else if (value instanceof Integer && !validList.contains(value)){
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. Valid values are [${validList.join(', ')}]"
            }else if (value instanceof List){
            }else if (value instanceof Map){
            }
        }

        if (contains){
            if (value == null){
            }else if (value instanceof String && !contains.findAll{ ((String)value).contains(String.valueOf(it)) }){
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. You must include the allowed values. [${contains.join(', ')}]"
            }else if (value instanceof Integer &&  !contains.findAll{ String.valueOf(value).contains(String.valueOf(it)) }){
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. You must include the allowed values. [${contains.join(', ')}]"
            }else if (value instanceof List){
            }else if (value instanceof Map){
            }
        }

        if (caseIgnoreValidList){
            if (value == null){
            }else if (value instanceof String && !caseIgnoreValidList.collect{ it.toUpperCase() }.contains(value.toUpperCase()) ){
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. Valid values are [${caseIgnoreValidList.join(', ')}]"
            }else if (value instanceof Integer && !caseIgnoreValidList.contains(value)){
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. Valid values are [${caseIgnoreValidList.join(', ')}]"
            }else if (value instanceof List){
            }else if (value instanceof Map){
            }
        }

        if (caseIgnoreContains){
            if (value == null){
            }else if (value instanceof String && !caseIgnoreContains.findAll{ ((String)value).toUpperCase().contains(String.valueOf(it).toUpperCase()) }){
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. You must include the allowed values. [${caseIgnoreContains.join(', ')}]"
            }else if (value instanceof Integer &&  !caseIgnoreContains.findAll{ String.valueOf(value).contains(String.valueOf(it)) }){
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. You must include the allowed values. [${caseIgnoreContains.join(', ')}]"
            }else if (value instanceof List){
            }else if (value instanceof Map){
            }
        }

        if (regexp){
            if (value == null){
            }else if (value instanceof String && !String.valueOf(value).matches(regexp)){
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. Must match regular expression. [${regexp}]"
            }else if (value instanceof Integer && !String.valueOf(value).matches(regexp)){
                errorMessage = "[${propertyNameLog}] '${value}' is invalid value. Must match regular expression. [${regexp}]"
            }else if (value instanceof List){
            }else if (value instanceof Map){
            }
        }

        if (errorMessage){
//            logger.error(errorMessage)
            throw new Exception(errorMessage)
        }

        return !errorMessage
    }

}
