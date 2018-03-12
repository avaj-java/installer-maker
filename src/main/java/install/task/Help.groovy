package install.task

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import install.Commander
import install.bean.HelpDataForCommand
import install.bean.HelpDataForTask
import jaemisseo.man.FileMan
import jaemisseo.man.bean.FileSetup
import jaemisseo.man.configuration.annotation.Alias
import jaemisseo.man.configuration.annotation.HelpIgnore
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Document
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.TerminalIgnore
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.configuration.annotation.type.Undomore
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
    @Value('mode.generate')
    Boolean modeGenerate

    @HelpIgnore
    @Value('force')
    Boolean isForce



    @Override
    Integer run(){

        if (modeGenerate){
            generate()
            return STATUS_TASK_DONE
        }

        //All Command and Task (No Detail)
        if (!specificCommandName && !specificTaskName){

            //- Print Help Task Document
            List<Annotation> allClassAnnotationList = config.findAllAnnotationFromClass(Help)
            Document documentAnt = allClassAnnotationList.find { it.annotationType() == Document }
            printDocument(documentAnt)

            if (isCommandable(applicationName)){
                logger.info ' [ Commands ]'
                printHelpAllCommand()
                logger.info '-------------------------'
            }

            if (isTaskRunable(applicationName)){
                logger.info ' [ Tasks ]'
                printHelpAllTask()
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
        return [Commander.APPLICATION_INSTALLER_MAKER, Commander.APPLICATION_HOYA].contains(applicationName)
    }

    boolean isTaskRunable(String applicationName){
        return [Commander.APPLICATION_INSTALLER_MAKER, Commander.APPLICATION_HOYA].contains(applicationName)
    }


    /*************************
     *
     * All Command (no detail)
     *
     *************************/
    void printHelpAllCommand(){
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
    void printHelpAllTask(){
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

        if (helpIgnoreAnt)
            return

        /** Print Help **/
        printHelp("${this.applicationName} ${commandName}")
    }

    /*************************
     *
     * Specific Task (detail)
     *
     *************************/
    void printHelpSpecificTask(def instance, boolean isDetail){
        Class taskType = instance.getClass()
        String taskTypeName = taskType.getSimpleName().toLowerCase()

        //-Collect
        List<Annotation> allClassAnnotationList = config.findAllAnnotationFromClass(taskType)
        TerminalIgnore terminalIgnoreAnt = allClassAnnotationList.find { it.annotationType() == TerminalIgnore }
        HelpIgnore helpIgnoreAnt = allClassAnnotationList.find{ it.annotationType() == HelpIgnore }
        Document documentAnt = allClassAnnotationList.find { it.annotationType() == Document }
        if (!terminalIgnoreAnt){

            /** Print Help **/
            if (isDetail)
                printDocument(documentAnt)

            if (helpIgnoreAnt)
                return

            if (isDetail){
                logger.info ''
                logger.info ' [ Simple Execution ]'
            }
            printNonProperty(taskTypeName, allClassAnnotationList)

            if (isDetail)
                printWithProperty(taskType)
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
        printHelp("${this.applicationName} -${taskName} ${nonPropertyPrintItemList.join(' ')}")
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
            printHelp(propertyPrintItemList.join('\n'))
        }
    }

    void printDocument(Document documentAnt){
        if (documentAnt){
            String documentString = documentAnt.value()
            if (documentString){
                printHelp(documentString)
            }
        }
    }

    void printHelp(String contents){
        logger.info Util.multiTrim(contents, 5)
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



    /**************************************************
     *
     *  Generate Help File
     *
     *  - Command Data
     *      {
     *          name :
     *          alias:
     *          document :
     *      }
     *
     *  - Task Data
     *      {
     *          name :
     *          alias:
     *          document :
     *          options : {
     *              terminalProtocol :
     *              terminalIgnore :
     *              undorable :
     *              undomore :
     *          }
     *          properties : {
     *              propertyName : valueType or valueList
     *              propertyName2 : valueType or valueList
     *              ...
     *          }
     *      }
     **************************************************/
    void generate(){
        /** Collecting Datas **/
        // -Command Datas
        Map commandDataMap = [:]
        config.methodCommandNameMap.each{ commandName, info ->
            HelpDataForCommand helpData = generateHelpDataForCommand(commandName, info)
            if (helpData)
                commandDataMap[helpData.name] = helpData
        }

        // -Task Datas
        Map taskDataMap = [:]
        config.findAllInstances(Task).each{ def instance ->
            HelpDataForTask helpData = generateHelpDataForTask(instance)
            if (helpData)
                taskDataMap[helpData.typeName] = helpData
        }

        /** Parse to JSON **/
        String commandDataListJson = JsonOutput.toJson(commandDataMap)
        String taskDataListJson = JsonOutput.toJson(taskDataMap)

        /** Generate File to Help **/
        String destDir = "./build/help"
        FileSetup fileSetup = new FileSetup(modeAutoMkdir: true, modeAutoOverWrite: true)

        FileMan.write("${destDir}/commands.json", commandDataListJson, fileSetup)
        FileMan.write("${destDir}/tasks.json", taskDataListJson, fileSetup)
        new FileMan().readResource("help/index.html").write("${destDir}/index.html", fileSetup)

    }

    private HelpDataForCommand generateHelpDataForCommand(String commandName, MethodInfomation info){
        HelpIgnore helpIgnoreAnt = info.findAnnotation(HelpIgnore)
        if (!helpIgnoreAnt){
            Document documentAnt = info.findAnnotation(Document)
            Alias aliasAnt = info.findAnnotation(Alias)
            return new HelpDataForCommand(
                    name: commandName,
                    document: documentAnt?.value(),
                    alias: aliasAnt?.value()
            )
        }
        return null
    }

    private HelpDataForTask generateHelpDataForTask(Object instance){
        Class taskType = instance.getClass()
        String taskTypeName = taskType.getSimpleName().toLowerCase()

        //-Collect
        List<Annotation> allClassAnnotationList = config.findAllAnnotationFromClass(taskType)
        HelpIgnore helpIgnoreAnt = allClassAnnotationList.find{ it.annotationType() == HelpIgnore }
        if (!helpIgnoreAnt){
            TerminalIgnore terminalIgnoreAnt = allClassAnnotationList.find { it.annotationType() == TerminalIgnore }
            Document documentAnt = allClassAnnotationList.find { it.annotationType() == Document }
            Alias aliasAnt = allClassAnnotationList.find { it.annotationType() == Alias }
            Undoable undoableAnt = allClassAnnotationList.find { it.annotationType() == Undoable }
            Undomore undomoreAnt = allClassAnnotationList.find { it.annotationType() == Undomore }
            TerminalValueProtocol terminalValueRuleAnt = allClassAnnotationList.find { it.annotationType() == TerminalValueProtocol }
            return new HelpDataForTask(
                    typeName: taskTypeName,
                    document: documentAnt?.value(),
                    alias: aliasAnt?.value(),
                    options:[
                        terminalIgnore: !!terminalIgnoreAnt,
                        terminalValueProtocol: terminalValueRuleAnt?.value()?.toList(),
                        undoable: !!undoableAnt,
                        undomore: !!undomoreAnt,
                    ],
                    properties: collectValueNames(taskType)
            )
        }
        return null
    }


}
