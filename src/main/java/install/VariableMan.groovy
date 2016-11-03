package install

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

    VariableMan() {
        funcMap = getBasicFuncMap()
    }
    VariableMan(def funcMapToAdd) {
        funcMap = getBasicFuncMap()
        addFuncs(funcMapToAdd)
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
     * You Can Create Function With This Object
     */
    class FuncObject{
        String substitutes = ''
        int length = 0
        boolean isOver = false
        String funcNm = ''
        String[] members = []
    }

    /**
     * Function Map
     */
    def funcMap = [:]

    /**
     *  You Can Create Custom Function
     *  Please Refer To getBasicFuncMap()
     * @param funcMapToAdd
     */
    VariableMan addFuncs(def funcMapToAdd){
        funcMap.putAll(funcMapToAdd)
    }


    /**
     *
     * @param codeRule
     * @param replaceMap
     * @return
     * @throws Exception
     */
    String parse(String codeRule, def replaceMap) throws Exception{
        validateCodeRule(codeRule)
        ///// Get String In ${ } step by step
        codeRule = codeRule.trim()
        String newCode = codeRule
        String pattern = '[$][{][^{]*[}]'
        Matcher matchedList = Pattern.compile(pattern).matcher(codeRule)
        matchedList.each{ String oneVal ->
            FuncObject funcObj = new FuncObject()
            // 1. get String in ${ }
            String content = oneVal.replaceFirst('[\$]', '').replaceFirst('\\{', '').replaceFirst('\\}', '')
            validateFunc(content)
            // 2. Analysis And Run Function
            def funcs = content.split('[.]')
            funcs.each{ String oneFunc ->
                String funcNm = ""
                String[] members = []
                // get funcNm
                def array = oneFunc.replaceFirst('\\(', ' ').split(' ')
                array.eachWithIndex{ String el, int idx->
                    if (idx==0)
                        funcNm = el
                }
                // get members
                String patternToGetMembers = '[(][^(]*[)]'
                Matcher m = Pattern.compile(patternToGetMembers).matcher(oneFunc)
                if (m){
                    String member = m[0]
                    members = member.substring(1, member.length() -1).split(',').collect{ it.trim() }
                }
                // run function
                if (funcNm){
                    funcNm = funcNm.toUpperCase()
                    // 2. 1) Custom Function To Get Some Value
                    // 2. 2) Basic Fucntions To Adjust Value
                    if ( replaceMap.keySet().findAll{ String key -> key.toUpperCase().equals(funcNm) }
                        || funcMap.keySet().findAll{ String key -> key.toUpperCase().equals(funcNm) } ){
                        replaceMap.each{
                            if (funcNm.equals(it.key.toUpperCase())){
                                funcObj.substitutes = it.value
                                if (members && members[0].matches('[0-9]*') ){
                                    funcObj.length = members[0] ? Integer.parseInt(members[0]) : 0
                                    if (funcObj.length > 0){
                                        int diff = funcObj.length - funcObj.substitutes.length()
                                        if (diff < 0){
                                            funcObj.substitutes = funcObj.substitutes.substring(0, funcObj.length)
                                            funcObj.isOver = true
                                        }
                                    }else{
                                        funcObj.length = 0
                                    }
                                }else if (!members){
                                }else{
                                    throw new Exception( ErrorMessage.VAR3.msg )
                                }
                            }
                        }
                        funcMap.each{ String thisFuncNm, def clouser ->
                            thisFuncNm = thisFuncNm.toUpperCase()
                            if (funcObj.length && members && funcNm.equals(thisFuncNm)){
                                funcObj.funcNm = funcNm
                                funcObj.members = members
                                clouser(funcObj)
                            }
                        }
                    }else{
                        throw new Exception( ErrorMessage.VAR4.msg )
                    }
                }else{
                    throw new Exception( ErrorMessage.VAR5.msg )
                }
            }
            // 3. Replace One ${ }
            if (funcObj.substitutes)
                newCode = newCode.replaceFirst(pattern, funcObj.substitutes)
        }
        return newCode
    }


    boolean validateCodeRule(String codeRule){
        if (!codeRule || !codeRule.trim())
            throw new Exception( ErrorMessage.VAR1.msg )
        if (Pattern.compile('[{]').matcher(codeRule).size() != Pattern.compile('[}]').matcher(codeRule).size())
            throw new Exception( ErrorMessage.VAR6.msg )
        return true
    }
    boolean validateFunc(String func){
        if (!func.trim())
            throw new Exception(ErrorMessage.VAR8.msg)
        if (func.matches('.*[)]{1}[^.]{1}.*'))
            throw new Exception(ErrorMessage.VAR5.msg)
        if (Pattern.compile('[(]').matcher(func).size() != Pattern.compile('[)]').matcher(func).size())
            throw new Exception(ErrorMessage.VAR7.msg)
        return true
    }




    /**
     *  This is Basic Functions
     * @return
     */
    def getBasicFuncMap(){
        return [
                'START': { FuncObject it ->
                    long nextSeq = Long.parseLong(it.substitutes)
                    long standardStartNum = Long.parseLong(it.members[0])
                    if (nextSeq < standardStartNum)
                        it.substitutes = it.members[0]
                },
                'LEFT': { FuncObject it ->
                    int diff = -1
                    while (diff != 0){
                        diff = it.length - it.substitutes.length()
                        if (diff < 0){
                            it.substitutes = it.substitutes.substring(0, it.length)
                        }else if (diff == 0){
                        }else if (diff > 0){
                            it.substitutes = it.substitutes = "${it.members[0]}${it.substitutes}"
                        }
                    }
                },
                'RIGHT': { FuncObject it ->
                    int diff = -1
                    while (diff != 0){
                        diff = it.length - it.substitutes.length()
                        if (diff < 0){
                            it.substitutes = it.substitutes.substring(0, it.length)
                        }else if (diff == 0){
                        }else if (diff > 0){
                            it.substitutes = "${it.substitutes}${it.members[0]}"
                        }
                    }
                },
                'ERROR': { FuncObject it ->
                    String errorNm = it.members[0].toUpperCase()
                    if (errorNm.equals('OVER') && it.isOver)
                        throw new Exception( ErrorMessage.VAR2.msg)
                }
        ]
    }




}
