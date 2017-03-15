package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.VariableMan
import com.jaemisseo.man.util.FileSetup
import install.bean.AskGlobalOption

/**
 * Created by sujkim on 2017-02-17.
 */
class TaskAsk extends TaskUtil{

    TaskAsk(PropMan propman){
        this.propman = propman
        this.varman = new VariableMan(propman.properties)
        this.gOpt = new AskGlobalOption().merge(new AskGlobalOption(
                modeRemember     : propman.get('remember.answer'){ varman.parse(it) },
                filePath         : propman.get('remember.answer.file.path'){ varman.parse(it) },
                fileEncoding     : propman.get('remember.answer.file.encoding'){ varman.parse(it) },
                modeBackup       : propman.get('remember.answer.file.backup'){ varman.parse(it) },
                backupPath       : propman.get('remember.answer.file.backup.path'){ varman.parse(it) },
                autoMkdir        : propman.get('remember.answer.auto.mkdir'){ varman.parse(it) }
        ))
    }

    static final String TYPE_FREE = "FREE"
    static final String TYPE_CHOICE = "CHOICE"
    static final String TYPE_YN = "YN"
    static final String TYPE_YN_Y = "Y"
    static final String TYPE_YN_N = "N"

    AskGlobalOption gOpt
    VariableMan varman
    String levelNamesProperty = 'ask.level'



    /**
     * RUN
     */
    void run(){
        List rememberAnswerLineList = []

        //1. READ ANSWER
        if (gOpt.modeRemember){
            try{
                PropMan rememberAnswerPropman = new PropMan().readFile(gOpt.filePath).properties
                propman.merge(rememberAnswerPropman)
            }catch(Exception e){
            }
        }

        //3. Each level by level
        eachLevel(levelNamesProperty){ String levelName ->

            String propertyPrefix = "${levelNamesProperty}.${levelName}."

            //4. Ready a Question
            varman = new VariableMan(propman.properties)
            //Get!
            def conditionIfObj = propman.parse("${propertyPrefix}if"){ varman.parse(it) }
            Map descriptionMap = propman.parse("${propertyPrefix}answer.description.map"){ varman.parse(it) }
            Map valueMap = propman.parse("${propertyPrefix}answer.value.map"){ varman.parse(it) }
            Map replacePropertyMap
            String msg = propman.get("${propertyPrefix}msg"){ varman.parse(it) }
            String question = propman.get("${propertyPrefix}question"){ varman.parse(it) }
            String type = propman.get("${propertyPrefix}answer.type"){ varman.parse(it) }
            String recommandAnswer = propman.get("${propertyPrefix}answer"){ varman.parse(it) }
            String property = propman.get("${propertyPrefix}property"){ varman.parse(it) }

            //5. Ask Question
            if (question && propman.match(conditionIfObj)){
                type = type?.trim()?.toUpperCase()
                String yourAnswer
                String value
                int repeatLimit = 5
                int repeatCount = 0
                boolean isOk = false

                while (!isOk){
                    if (repeatCount++ > repeatLimit)
                        throw new Exception('So Many Not Good Answer. Please Correct Answer :) ')
                    //Print Question
                    println "${question} [${recommandAnswer}]? "
                    //Print Selection
                    if (descriptionMap)
                        descriptionMap.sort{ a,b -> a.key <=> b.key }.each{ println "  ${it.key}) ${it.value}" }
                    //Wait Your Input
                    print "> "
                    yourAnswer = new Scanner(System.in).nextLine()

                    //If You Just Enter, Input Recommand Answer
                    if (!yourAnswer)
                        yourAnswer = recommandAnswer

                    //Check is it Right Answer
                    switch (type){
                        case TYPE_YN:
                            yourAnswer = yourAnswer ? yourAnswer.toUpperCase() : ''
                            isOk = (yourAnswer.equals(TYPE_YN_Y) || yourAnswer.equals(TYPE_YN_N))
                            break
                        case TYPE_CHOICE:
                            isOk = yourAnswer && (valueMap.containsKey(yourAnswer) || descriptionMap.containsKey(yourAnswer))
                            break
                        case TYPE_FREE:
                            isOk = true
                            break
                        default :
                            isOk = true
                            break
                    }

                    //Check Answer
                    println "=> ${yourAnswer}\n"
                    if (!isOk)
                        println "!! Not Good Answer. Please Answer Angain"
                }

                //6. Get Value
                value = (valueMap && valueMap[yourAnswer]) ? valueMap[yourAnswer] : yourAnswer
                if (property && value)
                    propman.set(property, value)
                propman.set("${propertyPrefix}answer", yourAnswer)
                propman.set("${propertyPrefix}value", value)
                rememberAnswerLineList.add("${propertyPrefix}answer=${yourAnswer}")
                //
                if (type.equals(TYPE_YN) && yourAnswer.equals(TYPE_YN_Y)){
                    replacePropertyMap = propman.parse("${propertyPrefix}replace.property"){ varman.parse(it) }
                    if (replacePropertyMap){
                        replacePropertyMap.each{
                            String propName = varman.parse(it.key)
                            String replaceValue = varman.parse(it.value)
                            propman.set(propName, replaceValue)
                        }
                    }
                }
            }else if(msg){
                println msg
            }

        }

        //7. WRITE ANSWER
        new FileMan(gOpt.filePath).set(new FileSetup()).backup().read(rememberAnswerLineList).write()
    }




}
