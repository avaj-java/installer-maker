package com.jaemisseo.man

import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created with IntelliJ IDEA.
 * User: sujkim
 * Date: 9/30/16
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
class VariableMan {

    /**
     * CONSTRUCTORS
     */
    VariableMan(){
        init()
    }

    VariableMan(Map<String, String> variableStringMapToPut){
        putVariables(variableStringMapToPut)
        init()
    }

    VariableMan(Map<String, String> variableStringMapToPut, Map<String, Closure> funcMapToPut){
        putVariables(variableStringMapToPut)
        putFuncs(funcMapToPut)
        init()
    }

    VariableMan(String charset){
        this.charset = charset
        init()
    }

    VariableMan(String charset, Map<String, String> variableStringMapToPut){
        this.charset = charset
        putVariables(variableStringMapToPut)
        init()
    }

    void init(){
        putVariableClosures(getBasicVariableClosureMap())
        putFuncs(getBasicFuncMap())
    }

    /**
     * Error Message
     */
    enum ErrorMessage{
        VAR1(1, "Doesn't Exist Code Rule."),
        VAR2(2, "Code Number Exceeds Limit."),
        VAR3(3, "It Needs Number Type Value To Set Length."),
        VAR4(4, "It is Unknown Code Rule's Function."),
        VAR5(5, "It Has Bad Syntax."),
        VAR6(6, "It Has Bad Syntax - brace does not match."),
        VAR7(7, "It Has Bad Syntax - bracket does not match."),
        VAR8(8, "It Has Bad Syntax - nothing beetween braces.")

        ErrorMessage(int code, String msg){ this.code = code; this.msg = msg }
        final int code
        final String msg
        String prefix = "VAR-"
        String getMsg(){ return "${prefix}${code}: ${msg}" }
    }

    /**
     * You Can Create Function's Properties With This Object
     */
    class FuncObject{
        String oneVal = ''
        String valueCode = ''
        String substitutes = ''
        byte[] bytes
        int length = 0
        boolean isOver = false
        String overValue = ''
        String funcNm = ''
        String[] members = []
    }

    Map<String, String> variableStringMap = [:]
    Map<String, Closure> variableClosureMap = [:]
    Map<String, Closure> FuncMap = [:]
    boolean modeDebug
    boolean modeExistCodeOnly
    String charset

    String patternToGetVariable = '[$][{][^{}]*\\w+[^{}]*[}]'     // If variable contains some word in ${} then convert to User Set Value or...
    String patternToGetMembers = '[(][^(]*[)]'
    int allCharLength = 0
    int allByteLength = 0
    int allUserSetLength = 0

    /**
     * You Can Set Debug Mode To Watch Detail
     * @param modeDebug
     * @return
     */
    VariableMan setModeDebug(boolean modeDebug){
        this.modeDebug = modeDebug
        return this
    }

    VariableMan setModeExistCodeOnly(boolean modeExistCodeOnly){
        this.modeExistCodeOnly = modeExistCodeOnly
        return this
    }

    /**
     * You Can Add Variable
     *  [ VariableName(String) : VariableValue(String) ]
     *  Please Do Not Set These Names => 'date', 'random'
     * @param variableStringMapToAdd
     * @return
     */
    @Deprecated
    VariableMan addVariables(Map<String, String> variableStringMapToAdd){
        if (variableStringMapToAdd)
            this.variableStringMap.putAll(variableStringMapToAdd)
        return this
    }

    /**
     * You Can Add Variable
     *  [ VariableName(String) : Variable Function(Closure) ]
     *  Please Refer To getBasicVariableClosureMap()
     * @param variableFuncMapToAdd
     * @return
     */
    @Deprecated
    VariableMan addVariableClosures(Map<String, Closure> variableFuncMapToAdd){
        if (variableFuncMapToAdd)
            variableClosureMap.putAll(variableFuncMapToAdd)
        return this
    }

    /**
     *  You Can Create Custom Function
     *  [ FunctionName(String) : Function(Closure) ]
     *  Please Refer To getBasicFuncMap()
     * @param funcMapToAdd
     */
    @Deprecated
    VariableMan addFuncs(Map<String, Closure> funcMapToAdd){
        funcMap.putAll(funcMapToAdd)
        return this
    }
    
    /**
     * You Can Put Variable
     *  [ VariableName(String) : VariableValue(String) ]
     *  Please Do Not Set These Names => 'date', 'random'
     * @param variableStringMapToAdd
     * @return
     */
    VariableMan putVariables(Map<String, String> variableStringMapToPut){
        if (variableStringMapToPut)
            this.variableStringMap.putAll(variableStringMapToPut)
        return this
    }

    /**
     * You Can Put Variable
     *  [ VariableName(String) : Variable Function(Closure) ]
     *  Please Refer To getBasicVariableClosureMap()
     * @param variableFuncMapToPut
     * @return
     */
    VariableMan putVariableClosures(Map<String, Closure> variableFuncMapToPut){
        if (variableFuncMapToPut)
            variableClosureMap.putAll(variableFuncMapToPut)
        return this
    }

    /**
     *  You Can Create Custom Function
     *  [ FunctionName(String) : Function(Closure) ]
     *  Please Refer To getBasicFuncMap()
     * @param funcMapToPut
     */
    VariableMan putFuncs(Map<String, Closure> funcMapToPut){
        funcMap.putAll(funcMapToPut)
        return this
    }

    /**
     * You Can Set CharacterSet
     * ex1) 'euc-kr'
     * ex2) 'utf-8'
     * @param charset
     * @return
     */
    VariableMan setCharset(String charset){
        this.charset = charset
        return this
    }

    /**
     * Parse !!!
     * @param codeRule
     * @return
     * @throws java.lang.Exception
     */
    String parse(String codeRule) throws Exception{
        return parse(codeRule, this.variableStringMap)
    }

    /**
     * Parse !!!
     * @param codeRule
     * @param variableStringMap
     * @return
     * @throws java.lang.Exception
     */
    String parse(String codeRule, Map<String, String> variableStringMap) throws Exception{
        validateCodeRule(codeRule)
        ///// Get String In ${ } step by step
        codeRule = codeRule.trim()
        String resultStr = codeRule
        Matcher matchedList = Pattern.compile(patternToGetVariable).matcher(codeRule)
        matchedList.each{ String oneVal ->
            FuncObject funcObj = new FuncObject()
            funcObj.oneVal = oneVal

            // 1. get String in ${ }
            String content = oneVal.replaceFirst('[\$]', '').replaceFirst('\\{', '').replaceFirst('\\}', '')
            validateFunc(content)

            // 2. Analysis And Run Function
            List<String> funcs = content.split('\\.')
            int variableEndIndex = funcs.findIndexOf{ it.indexOf('(') != -1 }
            int endIndex = funcs.size() -1
            if (variableEndIndex != -1){
                String variable = funcs[0..variableEndIndex].join('.')
                if (variableEndIndex < endIndex)
                    funcs = [variable] + funcs[variableEndIndex+1..endIndex]
                else
                    funcs = [content]
            }else{
                funcs = [content]
            }

            // If There are no LEFT or RIGHT FUNCTION => Set RIGHT FUNCTION
            if ( containsIgnoreCase(funcs, 'LEFT(') || containsIgnoreCase(funcs, 'RIGHT(') ){
            }else{
                funcs << 'RIGHT()'
            }
            funcs.eachWithIndex{ String oneFunc, int procIdx ->
                String funcNm = ""
                String[] members = []
                // get funcNm
                def array = oneFunc.replaceFirst('\\(', ' ').split(' ')
                array.eachWithIndex{ String el, int memIdx ->
                    if (memIdx==0)
                        funcNm = el
                }
                // get members
                Matcher m = Pattern.compile(patternToGetMembers).matcher(oneFunc)
                if (m){
                    String member = m[0]
                    members = member.substring(1, member.length() -1).split(',').collect{ it.trim() }
                }
                // run variable or func
                if (funcNm){
                    funcNm = funcNm.toUpperCase()
                    funcObj.funcNm = funcNm
                    funcObj.members = members
                    // 1) Get Variable's Value
                    if ( procIdx == 0){
                        funcObj.valueCode = funcNm
                        getVariableValue(funcObj, variableStringMap)

                    // 2) Run Fucntions To Adjust Value
                    }else if ( procIdx > 0 && containsKeyIgnoreCase(funcMap, funcNm) ){
                        runFunc(funcObj)

                    }else{
                        throw new Exception( ErrorMessage.VAR4.msg, new Throwable("[${funcNm}]") )
                    }

                }else{
//                maybe nothing is not bad
//                    throw new Exception( ErrorMessage.VAR5.msg, new Throwable("[${oneVal}]") )
                }

            }

            // 3. Replace One ${ }
            if (modeExistCodeOnly && !funcObj.substitutes){
            }else{
//                String patternPrefix = '[$][{][^{}]*(?i)'
//                String patternSurfix = '[^{}]*[}]'
                String patternToGetVariable = funcObj.oneVal.replace('$','\\$').replace('{','\\{').replace('}','\\}').replace('(','\\(').replace(')','\\)').replace('.','\\.')
                Matcher matchedTargetList = Pattern.compile(patternToGetVariable).matcher(resultStr)
                if (matchedTargetList.size())
                    resultStr = matchedTargetList.replaceAll(getRightReplacement(funcObj.substitutes))
            }
            // CASE - DEBUG MODE
            if (modeDebug && funcObj.substitutes){
                byte[] bytes = (charset) ? funcObj.substitutes.getBytes(charset) : funcObj.substitutes.getBytes()
                int userSetLength = funcObj.length
                int byteLength = bytes.length
                int charLength = funcObj.substitutes.length()
                allUserSetLength += userSetLength
                allByteLength += byteLength
                allCharLength += charLength
                println "////////// ${oneVal}"
                println "[${funcObj.substitutes}]"
                println "Length: ${charLength} / Byte: ${byteLength} / Your Set: ${userSetLength}"
                if (userSetLength !=0 && userSetLength != byteLength){
                    println "!!! !!! !!! !!! !!!"
                    println "!!! Not Matched !!!"
                    println "!!! !!! !!! !!! !!!"
                }
            }
        }
        if (modeDebug){
            println ""
            println "//////////////////////////////////////////////////"
            println "////// ALL LENGTH ////////////////////////////////"
            println "//////////////////////////////////////////////////"
            println "SETUP LENGTH : ${allUserSetLength}"
            println " BYTE LENGTH : ${allByteLength}"
            println " CHAR LENGTH : ${allCharLength}"
            println ""
        }
        return resultStr
    }



    String getRightReplacement(String replacement){
        // This Condition's Logic prevent disapearance \
        if (replacement.indexOf('\\') != -1)
            replacement = replacement.replaceAll('\\\\','\\\\\\\\')
        // This Condition's Logic prevent Error - java.lang.IllegalArgumentException: named capturing group has 0 length name
        if (replacement.indexOf('$') != -1)
            replacement = replacement.replaceAll('\\$','\\\\\\$')
        return replacement
    }



    /**
     * Validation
     * @param codeRule
     * @return
     */
    boolean validateCodeRule(String codeRule) throws Exception{
        /* DEVELOPER COMMENT: "It does not matter." */
        if (!codeRule || !codeRule.trim())
            throw new Exception( ErrorMessage.VAR1.msg + "[${codeRule}]" )
        if (Pattern.compile('[{]').matcher(codeRule).size() != Pattern.compile('[}]').matcher(codeRule).size())
            throw new Exception( ErrorMessage.VAR6.msg + "[${codeRule}]" )
        return true
    }
    boolean validateFunc(String func){
        /* DEVELOPER COMMENT: "Maybe ${} is not bad" */
//        if (!func.trim())
//            throw new Exception( ErrorMessage.VAR8.msg + "[${func}]" )
        if (func.matches('.*[)]{1}[^.]{1}.*'))
            throw new Exception( ErrorMessage.VAR5.msg + "[${func}]" )
        if (Pattern.compile('[(]').matcher(func).size() != Pattern.compile('[)]').matcher(func).size())
            throw new Exception( ErrorMessage.VAR7.msg + "[${func}]" )
        return true
    }


    void getVariableValue(FuncObject funcObj, Map<String, String> variableStringMap){
        String funcNm = funcObj.funcNm
        String[] members = funcObj.members
        //Variable(default) - DATE, RANDOM
        Closure variableClosure = getIgnoreCase(variableClosureMap, funcNm)
        if (variableClosure){
            variableClosure(funcObj)
            return
        }
        //Variable(custom) - ? (from variableStringMap)
        String variableValue = getIgnoreCase(variableStringMap, funcNm)
        if (variableValue != null){
            funcObj.substitutes = variableValue?:''
            funcObj.bytes = (charset) ? funcObj.substitutes.getBytes(charset) : funcObj.substitutes.getBytes()
            if (members && members[0].matches('[0-9]*') ){
                funcObj.length = members[0] ? Integer.parseInt(members[0]) : 0
                if (funcObj.length > 0){
                    int diff = funcObj.length - funcObj.bytes.length
                    if (diff < 0){
//                        funcObj.substitutes = funcObj.substitutes.substring(0, funcObj.length)
                        funcObj.isOver = true
                        funcObj.overValue = funcObj.substitutes
                        funcObj.substitutes = (charset) ? new String(funcObj.bytes, 0, funcObj.length, charset) : new String(funcObj.bytes, 0, funcObj.length)
                    }
                }else{
                    funcObj.length = 0
                }
            }else if (!members){
            }else{
                throw new Exception( ErrorMessage.VAR3.msg, new Throwable("[${funcObj.oneVal}]") )
            }
        //Variable(?) - (Not Matched)
        }else{
            funcObj.length = (members && members[0]) ? Integer.parseInt(members[0]) : 0
        }
    }


    void runFunc(FuncObject funcObj){
        Closure closure = getIgnoreCase(funcMap, funcObj.funcNm)
        if (closure)
            closure(funcObj)
    }





    /**
     *  These are Basic Functions
     *  Watch Below To Add Function.
     *
     *  Method 1 => New VariableMan(Map)
     *  Method 2 => New VariableMan().addVariableClosures(Map)
     *
     * @return
     */
    Map<String, Closure> getBasicVariableClosureMap(){
        return [
                'DATE': { FuncObject it ->
                    String[] members = it.members
                    String format = (members && members[0]) ? members[0] : 'YYYYMMdd'
                    it.substitutes = new SimpleDateFormat(format).format(new Date())
                    it.length = format.length()
                },
                'RANDOM': { FuncObject it ->
                    String[] members = it.members
                    int length = (members && members[0]) ? Integer.parseInt(members[0]) : 0
                    String numStr = "1"
                    String plusNumStr = "1"
                    for (int i = 0; i < length; i++){
                        numStr += "0"
                        if (i != length - 1)
                            plusNumStr += "0"
                    }
                    int result = new Random().nextInt(Integer.parseInt(numStr)) + Integer.parseInt(plusNumStr)
                    if (result > Integer.parseInt(numStr))
                        result -= Integer.parseInt(plusNumStr)
                    it.substitutes = result
                    it.length = length
                }
        ]
    }



    /**
     *  These are Basic Functions
     *  Watch Below To Add Function.
     *
     *  Method 1 => New VariableMan().addFuncs(Map)
     *
     * @return
     */
    Map<String, Closure> getBasicFuncMap(){
        return [
                'START': { FuncObject it ->
                    if (it.length && it.members ){
                        long nextSeq = Long.parseLong(it.substitutes)
                        long standardStartNum = Long.parseLong(it.members[0])
                        if (nextSeq < standardStartNum)
                            it.substitutes = it.members[0]
                    }
                },
                'LEFT': { FuncObject it ->
                    if (it.length && it.members ){
                        int diff = -1
                        int tryRemoveIdx = 0
                        if (!it.members || it.members[0].isEmpty() )
                            it.members[0] = ' '
                        while (diff != 0){
                            byte[] bytes = (charset) ? it.substitutes.getBytes(charset) : it.substitutes.getBytes()
                            diff = it.length - bytes.length
                            if (diff < 0){
//                                it.substitutes = it.substitutes.substring(0, it.length)
                                it.substitutes = (charset) ? new String(bytes, 0, it.length -tryRemoveIdx, charset) : new String(bytes, 0, it.length -tryRemoveIdx)
                                tryRemoveIdx++
                            }else if (diff == 0){
                            }else if (diff > 0){
                                it.substitutes = "${it.members[0]}${it.substitutes}"
                            }
                        }
                    }
                },
                'RIGHT': { FuncObject it ->
                    if (it.length && it.members ){
                        int diff = -1
                        int tryRemoveIdx = 0
                        if (!it.members || it.members[0].isEmpty() )
                            it.members[0] = ' '
                        while (diff != 0){
                            byte[] bytes = (charset) ? it.substitutes.getBytes(charset) : it.substitutes.getBytes()
                            diff = it.length - bytes.length
                            if (diff < 0){
//                                it.substitutes = it.substitutes.substring(0, it.length-1)
                                it.substitutes = (charset) ? new String(bytes, 0, it.length -tryRemoveIdx, charset) : new String(bytes, 0, it.length -tryRemoveIdx)
                                tryRemoveIdx++
                            }else if (diff == 0){
                            }else if (diff > 0){
                                it.substitutes = "${it.substitutes}${it.members[0]}"
                            }
                        }
                    }
                },
                'ERROR': { FuncObject it ->
                    if (it.length && it.members ){
                        String errorNm = it.members[0].toUpperCase()
                        if (errorNm.equals('OVER') && it.isOver)
                            throw new Exception( "${ErrorMessage.VAR2.msg} - Rule is '${it.oneVal}', But ${it.valueCode}'s value is ${it.overValue}", new Throwable(" Seted Rule is '${it.oneVal}', But ${it.valueCode}'s value is ${it.overValue}") )
                    }
                },
                'LOWER': { FuncObject it ->
                    it.substitutes = (it.substitutes) ? it.substitutes.toLowerCase() : ""
                },
                'UPPER': { FuncObject it ->
                    it.substitutes = (it.substitutes) ? it.substitutes.toUpperCase() : ""
                },
                'NUMBERONLY': { FuncObject it ->
                    it.substitutes = (it.substitutes) ? it.substitutes.replaceAll("[^0-9.]", "") : ""
                }
        ]
    }


    boolean containsIgnoreCase(List list, String value){
        value = value.toUpperCase()
        List resultItems = list.findAll{ String item ->
            item.toUpperCase().contains(value)
        }
        if (resultItems && resultItems.size() > 0)
            return true
        else
            return false
    }

    boolean containsKeyIgnoreCase(Map map, String key){
        key = key.toUpperCase()
        List resultKeys = map.keySet().findAll{ String itKey ->
            itKey.toUpperCase().contains(key)
        }.toList()
        if (resultKeys && resultKeys.size() > 0)
            return true
        else
            return false
    }

    def getIgnoreCase(Map map, String key){
        key = key.toUpperCase()
        List resultKeys = map.keySet().findAll{ String itKey ->
            itKey.toUpperCase().equals(key)
        }.toList()
        if (resultKeys && resultKeys.size() > 0){
            key = resultKeys[0]
            return map[key]
        }else{
            return null
        }

    }

}