package install.task

import install.configuration.annotation.Alias
import install.configuration.annotation.HelpIgnore
import install.configuration.annotation.Value
import install.configuration.annotation.type.Document
import install.configuration.annotation.type.Task
import install.configuration.annotation.type.TerminalIgnore
import install.configuration.annotation.type.TerminalValueProtocol
import install.configuration.reflection.MethodInfomation
import install.util.TaskUtil
import jaemisseo.man.util.Util

import java.lang.annotation.Annotation
import java.lang.reflect.Member

/**
 * Created by sujkim on 2017-06-26.
 */
@Alias('h')
@Task
class Help extends TaskUtil{

    @HelpIgnore
    @Value('help.command.name')
    String specificCommandName

    @HelpIgnore
    @Value('help.task.name')
    String specificTaskName

    @HelpIgnore
    @Value(property='force', method='getBoolean')
    Boolean isForce

    String programName = 'installer-maker'



    @Override
    Integer run(){

        //All Command and Task (No Detail)
        if (!specificCommandName && !specificTaskName){
            println ' [ Commands ]'
            printHelpCommand()
            println '-------------------------'
            println ' [ Tasks ]'
            printHelpTask()

        }else{
            //Specific Command (Detail)
            if (specificCommandName){
                config.methodCommandNameMap.each { commandName, info ->
                    if (specificCommandName == commandName){
                        println " [ Command:${specificCommandName.toUpperCase()} ]"
                        printHelpSpecificCommand(commandName, info, true)
                    }
                }

            //Specific Task (Detail)
            }else if (specificTaskName){
                config.findAllInstances(Task).each { def instance ->
                    String taskName = instance.getClass().getSimpleName().toLowerCase()
                    if (specificTaskName == taskName){
                        println " [ Task:${specificTaskName.toUpperCase()} ]"
                        printHelpSpecificTask(instance, true)
                    }
                }
            }
        }

        return STATUS_TASK_DONE
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
        if (documentAnt)
            documentString = documentAnt.value()

        //-Print
        if (!helpIgnoreAnt)
            println "${programName} ${commandName}"

        //-Detail
        if (isDetail){
            if (documentString){
                println ''
                println documentString
            }
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

            //-Collect TerminalValueRule
            List<Annotation> allClassAnnotationList = config.findAllAnnotationFromClass(clazz, TerminalValueProtocol)
            Task taskAnt = allClassAnnotationList.find { it.annotationType() == Task }
            TerminalValueProtocol terminalValueRuleAnt = allClassAnnotationList.find { it.annotationType() == TerminalValueProtocol }
            Document documentAnt = allClassAnnotationList.find { it.annotationType() == Document }
            if (taskAnt && terminalValueRuleAnt)
                terminalValueRule = terminalValueRuleAnt.value().toList()
            if (documentAnt)
                documentString = documentAnt.value()

            //-Collect Properties
            Map<Member, List<Annotation>> allClassMemberAnnotationMap = config.findAllAnnotationFromClassMember(clazz, Value)
            allClassMemberAnnotationMap.each { Member member, List<Annotation> annotationList ->
                Value valueAnt = annotationList.find { it.annotationType() == Value }
                HelpIgnore helpIgnoreAnt = annotationList.find { it.annotationType() == HelpIgnore }
                if (valueAnt && !helpIgnoreAnt) {
                    String propertyName = valueAnt.value() ?: valueAnt.property() ?: ''
                    if (propertyName)
                        propertyList << propertyName
//                    boolean isRequired = valueAnt.required()
//                    String[] validList = valueAnt.validList()
                }
            }

            //-Print Non Property
            List nonPropertyPrintItemList = []
            terminalValueRule.each {
                nonPropertyPrintItemList << "<${it}>"
            }
            println "${programName} -${taskName} ${nonPropertyPrintItemList.join(' ')}"

            //-Print with Property
            if (isDetail){
                if (propertyList){
                    List propertyPrintItemList = []
                    propertyList.each {
                        propertyPrintItemList << "-${it}=<value>"
                    }
                    println "${programName} -${taskName} ${propertyPrintItemList.join(' ')}"
                }

                if (documentString){
                    println ''
                    println documentString
                }
            }
        }
    }



}
