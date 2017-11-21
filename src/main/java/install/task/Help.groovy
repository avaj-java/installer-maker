package install.task

import install.Commander
import install.Starter
import install.configuration.annotation.Alias
import install.configuration.annotation.HelpIgnore
import install.configuration.annotation.Value
import install.configuration.annotation.type.Document
import install.configuration.annotation.type.Task
import install.configuration.annotation.type.TerminalIgnore
import install.configuration.annotation.type.TerminalValueProtocol
import install.configuration.reflection.FieldInfomation
import install.configuration.reflection.MethodInfomation
import install.configuration.reflection.ReflectInfomation
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

            if (isCommandable(applicationName)){
                logger.info ' [ Commands ]'
                printHelpCommand()
                logger.info '-------------------------'
            }

            if (isTaskRunable(applicationName)){
                logger.info ' [ Tasks ]'
                printHelpTask()
                //- Print Help Task Document
                List<Annotation> allClassAnnotationList = config.findAllAnnotationFromClass(Help)
                Document documentAnt = allClassAnnotationList.find { it.annotationType() == Document }
                printDocument(documentAnt)
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
        return [Commander.APPLICATION_INSTALLER_MAKER, Commander.APPLICATION_MACGYVER].contains(applicationName)
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

        //-Print
        if (!helpIgnoreAnt)
            logger.info "${this.applicationName} ${commandName}"

        //-Detail
        if (isDetail){
            printDocument(documentAnt)
        }

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
            String documentString = ''
            List<String> terminalValueRule = []
            List<String> propertyList = []

            //-Collect
            List<Annotation> allClassAnnotationList = config.findAllAnnotationFromClass(clazz)
            Task taskAnt = allClassAnnotationList.find { it.annotationType() == Task }
            TerminalValueProtocol terminalValueRuleAnt = allClassAnnotationList.find { it.annotationType() == TerminalValueProtocol }
            Document documentAnt = allClassAnnotationList.find { it.annotationType() == Document }
            if (taskAnt && terminalValueRuleAnt)
                terminalValueRule = terminalValueRuleAnt.value().toList()

            //-Collect Value Names (Properties)
            propertyList = collectValueNames(clazz)

            //-Print Non Property
            List nonPropertyPrintItemList = []
            terminalValueRule.each {
                nonPropertyPrintItemList << "<${it}>"
            }
            logger.info "${this.applicationName} -${taskName} ${nonPropertyPrintItemList.join(' ')}"

            //-Print with Property
            if (isDetail){
                if (propertyList){
                    List propertyPrintItemList = []
                    propertyList.each {
                        propertyPrintItemList << "-${it}=<value>"
                    }

                    //
//                    logger.info "${this.applicationName} -${taskName} ${propertyPrintItemList.join(' ')}"

                    //
                    logger.info('')
                    logger.info(' [ Properties ] ')
                    propertyPrintItemList.each{
                        logger.info("${it}")
                    }
                }

                printDocument(documentAnt)
            }
        }
    }



    /*************************
     *
     * Collect Value Annotation's Name on Class
     *
     *************************/
    List<String> collectValueNames(Class clazz){
        List resultPropertyList = []
        return collectValueNames(clazz, '', resultPropertyList)
    }

    List<String> collectValueNames(Class clazz, String propertyPrefixFirstName, List resultPropertyList){
        ReflectInfomation reflectInfo = config.reflectionMap[clazz]
        if (reflectInfo){
            reflectInfo.valueFieldNameMap.each{ String fieldName, FieldInfomation fieldInfo ->
                resultPropertyList = collectValueNames(propertyPrefixFirstName, fieldInfo.annotationList, fieldInfo.fieldType, resultPropertyList)
            }
            reflectInfo.valueMethodNameMap.each{ String methodName, MethodInfomation methodInfo ->
                resultPropertyList = collectValueNames(propertyPrefixFirstName, methodInfo.annotationList, methodInfo.parameterTypes[0], resultPropertyList)
            }
        }
        return resultPropertyList
    }

    List<String> collectValueNames(String propertyPrefixFirstName, List<Annotation> annotationList, Class valueType, List resultPropertyList){
        Value valueAnt = annotationList.find { it.annotationType() == Value }
        HelpIgnore helpIgnoreAnt = annotationList.find { it.annotationType() == HelpIgnore }
        if (valueAnt && !helpIgnoreAnt) {
            String propertyName = valueAnt.value() ?: valueAnt.name() ?: ''
            String propertyPrefixName = valueAnt.prefix()
            String fullPropertyPrefixName = "${propertyPrefixFirstName}${propertyPrefixName}"
            String fullPropertyName = "${propertyPrefixFirstName}${propertyPrefixName}${propertyName}"
            if (propertyName)
                resultPropertyList << fullPropertyName
            else
                collectValueNames(valueType, fullPropertyPrefixName, resultPropertyList)
//                    boolean isRequired = valueAnt.required()
//                    String[] validList = valueAnt.validList()
        }
    }



    /*************************
     *
     * Print Document Annotation Information
     *
     *************************/
    void printDocument(Document documentAnt){
        if (documentAnt){
            String documentString = documentAnt.value()
            if (documentString){
                logger.info ''
                logger.info multiTrim(documentString)
            }
        }
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
            if (indentIndex > 0){
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
