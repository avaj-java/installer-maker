package install.task

import install.Commander
import jaemisseo.man.configuration.annotation.Alias
import jaemisseo.man.configuration.annotation.HelpIgnore
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Document
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.TerminalIgnore
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import jaemisseo.man.configuration.reflection.FieldInfomation
import jaemisseo.man.configuration.reflection.MethodInfomation
import jaemisseo.man.configuration.reflection.ReflectInfomation
import install.util.TaskUtil
import jaemisseo.man.util.Util

import java.lang.annotation.Annotation

/**
 * Created by sujkim on 2017-06-26.
 */
@Alias('h')
@Task
@Document("""
You can know How to use Command or Task on Terminal      
""")
class Help extends TaskUtil{

    @HelpIgnore
    @Value('help.command.name')
    String specificCommandName

    @HelpIgnore
    @Value('help.task.name')
    String specificTaskName

    @HelpIgnore
    @Value('application.name')
    String applicationName = 'installer-maker'

    @HelpIgnore
    @Value('force')
    Boolean isForce



    @Override
    Integer run(){

//        println "asdf $applicationName"
//        println isCommandable(applicationName)
//        println isTaskRunable(applicationName)

        //All Command and Task (No Detail)
        if (!specificCommandName && !specificTaskName){

            //- Print Help Task Document
            List<Annotation> allClassAnnotationList = config.findAllAnnotationFromClass(Help)
            Document documentAnt = allClassAnnotationList.find { it.annotationType() == Document }
            printDocument(documentAnt)

            if (isCommandable(applicationName)){
                logger.info ' [ Commands ]'
                printHelpCommand()
                logger.info '-------------------------'
            }

            if (isTaskRunable(applicationName)){
                logger.info ' [ Tasks ]'
                printHelpTask()
            }

        }else{

            //Specific Command (Detail)
            if (specificCommandName && isCommandable(applicationName)){
                config.methodCommandNameMap.each { commandName, info ->
                    if (specificCommandName == commandName){
                        logger.info " [ Command:${specificCommandName.toUpperCase()} ]"
                        printHelpSpecificCommand(commandName, info, true)
                    }
                }

            //Specific Task (Detail)
            }else if (specificTaskName && isTaskRunable(applicationName)){
                config.findAllInstances(Task).each { def instance ->
                    String taskName = instance.getClass().getSimpleName().toLowerCase()
                    if (specificTaskName == taskName){
                        logger.info " [ Task:${specificTaskName.toUpperCase()} ]"
                        printHelpSpecificTask(instance, true)
                    }
                }
            }

        }

        return STATUS_TASK_DONE
    }


    boolean isCommandable(String applicationName){
        return [Commander.APPLICATION_INSTALLER_MAKER].contains(applicationName)
    }

    boolean isTaskRunable(String applicationName){
        return [Commander.APPLICATION_INSTALLER_MAKER, Commander.APPLICATION_HOYA].contains(applicationName)
    }


    /*************************
     *
     * All Command (no detail)
     *
     *************************/
    void printHelpCommand(){
        //Command
        config.methodCommandNameMap.each{ commandName, info ->
            printHelpSpecificCommand(commandName, info, false)
        }
    }

    /*************************
     *
     * All Task (no detail)
     *
     *************************/
    void printHelpTask(){
        //Task
        config.findAllInstances(Task).each{ def instance ->
            printHelpSpecificTask(instance, false)
        }
    }



    /*************************
     *
     * Specific Command (detail)
     *
     *************************/
    void printHelpSpecificCommand(String commandName, MethodInfomation info, boolean isDetail){
        String documentString = ''

        //-Collect
        HelpIgnore helpIgnoreAnt = info.findAnnotation(HelpIgnore)
        Document documentAnt = info.findAnnotation(Document)

        if (isDetail)
            printDocument(documentAnt)

        /** Print Help **/
        if (!helpIgnoreAnt)
            logger.info "${this.applicationName} ${commandName}"

    }

    /*************************
     *
     * Specific Task (detail)
     *
     *************************/
    void printHelpSpecificTask(def instance, boolean isDetail){
        Class clazz = instance.getClass()
        String taskName = clazz.getSimpleName().toLowerCase()

        if (!Util.findAllClasses('install', TerminalIgnore).contains(clazz)){
            //-Collect
            List<Annotation> allClassAnnotationList = config.findAllAnnotationFromClass(clazz)
            Document documentAnt = allClassAnnotationList.find { it.annotationType() == Document }

            /** Print Help **/
            if (isDetail)
                printDocument(documentAnt)

            if (isDetail){
                logger.info ''
                logger.info ' [ Simple Execution ]'
            }
            printNonProperty(taskName, allClassAnnotationList)

            if (isDetail)
                printWithProperty(clazz)
        }
    }

    void printNonProperty(String taskName, List<Class> allClassAnnotationList){
        Task taskAnt = allClassAnnotationList.find { it.annotationType() == Task }
        TerminalValueProtocol terminalValueRuleAnt = allClassAnnotationList.find { it.annotationType() == TerminalValueProtocol }
        List<String> terminalValueRule = []
        if (taskAnt && terminalValueRuleAnt)
            terminalValueRule = terminalValueRuleAnt.value().toList()
        List nonPropertyPrintItemList = []
        terminalValueRule.each {
            nonPropertyPrintItemList << "<${it}>"
        }
        logger.info "${this.applicationName} -${taskName} ${nonPropertyPrintItemList.join(' ')}"
    }

    void printWithProperty(Class clazz){
        //-Collect Value Names (Properties)
        Map<String, String> propertyItemMap = collectValueNames(clazz)
        if (propertyItemMap){
            List propertyPrintItemList = []
            propertyItemMap.each {
                propertyPrintItemList << "-${it.key}=<${it.value}>"
            }

            logger.info('')
            logger.info(' [ Options ] ')
            propertyPrintItemList.each{
                logger.info("${it}")
            }
        }
    }

    void printDocument(Document documentAnt){
        if (documentAnt){
            String documentString = documentAnt.value()
            if (documentString){
//                logger.info ''
//                logger.info ' [ Detail ]'
                logger.info multiTrim(documentString)
            }
        }
    }



    /*************************
     *
     * Collect Value Annotation's Name on Class
     *
     *************************/
    Map<String, String> collectValueNames(Class clazz){
        Map<String, String> resultPropertyMap = [:]
        return collectValueNames(clazz, '', resultPropertyMap)
    }

    Map<String, String> collectValueNames(Class clazz, String propertyPrefixFirstName, Map<String, String> resultPropertyMap){
        ReflectInfomation reflectInfo = config.reflectionMap[clazz]
        if (reflectInfo){
            reflectInfo.valueFieldNameMap.each{ String fieldName, FieldInfomation fieldInfo ->
                resultPropertyMap = collectValueNames(propertyPrefixFirstName, fieldInfo.annotationList, fieldInfo.fieldType, resultPropertyMap)
            }
            reflectInfo.valueMethodNameMap.each{ String methodName, MethodInfomation methodInfo ->
                resultPropertyMap = collectValueNames(propertyPrefixFirstName, methodInfo.annotationList, methodInfo.parameterTypes[0], resultPropertyMap)
            }
        }
        return resultPropertyMap
    }

    Map<String, String> collectValueNames(String propertyPrefixFirstName, List<Annotation> annotationList, Class valueType, Map<String, String> resultPropertyMap){
        Value valueAnt = annotationList.find { it.annotationType() == Value }
        HelpIgnore helpIgnoreAnt = annotationList.find { it.annotationType() == HelpIgnore }
        if (valueAnt && !helpIgnoreAnt) {
            String propertyName = valueAnt.value() ?: valueAnt.name() ?: ''
            String propertyPrefixName = valueAnt.prefix()
            String fullPropertyPrefixName = "${propertyPrefixFirstName}${propertyPrefixName}"
            String fullPropertyName = "${propertyPrefixFirstName}${propertyPrefixName}${propertyName}"

            String value = 'value'
            List<String> validList = (valueAnt.validList() ?: valueAnt.caseIgnoreValidList())?.toList()
            boolean isRequired = valueAnt.required()

            if (validList)
                value = validList.join('|')

            if (propertyName){
                resultPropertyMap[fullPropertyName] = value
            }else{
                collectValueNames(valueType, fullPropertyPrefixName, resultPropertyMap)
            }
        }
        return resultPropertyMap
    }









    String multiTrim(String content){
        //- Remove Shortest Left Indent
        Integer shortestIndentIndex = 0
        List<String> stringList = content.split('\n').toList()
        List<String> resultStringList = stringList.findAll{
            List charList = it.toList()
            int indentIndex = 0
            for (int i=0; i<charList.size(); i++){
                if (charList[i] != " "){
                    indentIndex = i
                    break
                }
            }
            if (indentIndex >= 0){
                if (shortestIndentIndex == 0 || shortestIndentIndex > indentIndex){
                    shortestIndentIndex =  indentIndex
                }
            }
            return true
        }
        //- Remove Empty Top and Bottom
        Integer startRowIndex
        Integer endRowIndex
        resultStringList.eachWithIndex{ String row, int index ->
            String line = row.trim()
            if (line && startRowIndex == null)
                startRowIndex = index
            if (line)
                endRowIndex = index
        }
        resultStringList = resultStringList[startRowIndex..endRowIndex]
        String resultString = resultStringList.collect{ it.substring(shortestIndentIndex) }.join('\n')
        return resultString
    }

}
