package install.configuration

import install.JobUtil
import install.annotation.*
import jaemisseo.man.PropMan
import jaemisseo.man.util.Util

import java.lang.reflect.Method

/**
 * Created by sujkim on 2017-06-12.
 */
class Config {

    Map<Class, Object> instanceMap = [:]

    Map<String, MethodInfomation> initMap = [:]
    Map<String, MethodInfomation> initCheckMap = [:]
    Map<String, MethodInfomation> initLatelyMap = [:]

    Map<String, MethodInfomation> beforeMap = [:]
    Map<String, MethodInfomation> afterMap = [:]
    Map<String, MethodInfomation> commandMap = [:]

    InstallerPropertiesGenerator propGen
    InstallerLogGenerator logGen



    void makeProperties(String[] args){
        propGen = new InstallerPropertiesGenerator()
        propGen.makeExternalProperties(args)
        propGen.makeDefaultProperties()
        propGen.genResourceSingleton('builder', 'defaultProperties/builder.default.properties')
        propGen.genResourceSingleton('receptionist', 'defaultProperties/receptionist.default.properties')
        propGen.genResourceSingleton('installer', 'defaultProperties/installer.default.properties')
        propGen.genResourceSingleton('macgyver', 'defaultProperties/macgyver.default.properties')
    }

    void makeLoger(){
        logGen = new InstallerLogGenerator()
    }


    /*****
     * 1. Scan Class
     * 2. New Instance
     *****/
    void scan(){
        try {
            List<Class> jobList = Util.findAllClasses('install', [Job, Employee])
            List<Class> taskList = Util.findAllClasses('install', [Task])

            jobList.each { Class clazz ->
                instanceMap[clazz] = clazz.newInstance()
                (instanceMap[clazz] as JobUtil).propGen = propGen
            }

            //Scan Method,
            jobList.each { Class clazz ->
                clazz.getDeclaredMethods().each { Method method ->
                    method.getAnnotations().each { annotation ->
                        if (annotation instanceof Init) {
                            initMap[clazz] = new MethodInfomation(instance: instanceMap[clazz], clazz: clazz, methodName: method.name)
                            boolean isLately = annotation.lately()
                            initLatelyMap[clazz] = isLately
                            initCheckMap[clazz] = false
                        }else if (annotation instanceof Command){
                            Command commandAnt = annotation as Command
                            commandMap[commandAnt.value()] = new MethodInfomation(instance: instanceMap[clazz], clazz: clazz, methodName: method.name)
                        }else if (annotation instanceof Before){
                            beforeMap[clazz] = new MethodInfomation(instance: instanceMap[clazz], clazz: clazz, methodName: method.name)
                        }else if (annotation instanceof After){
                            afterMap[clazz] = new MethodInfomation(instance: instanceMap[clazz], clazz: clazz, methodName: method.name)
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace()
            throw e
        }
    }

    /*****
     * Init Instance
     *****/
    void init(){
        initMap.each{
            MethodInfomation info = it.value
            Class clazz = info.clazz
            if (!initLatelyMap[clazz] && initMap[clazz] && !initCheckMap[clazz]){
                initInstance(clazz)
            }
        }
    }

    void initInstance(Class clazz) {
        MethodInfomation info = initMap[clazz]
        Object instance = info.instance
        Method method = clazz.getMethod(info.methodName, null)
        println info.methodName
        println clazz
        try {
            method.invoke(instance, null)
        }catch(e){
            e.printStackTrace()
            throw e
        }
        initCheckMap[clazz] = true
    }

    /*****
     * Command
     *****/
    void command(){
        PropMan propmanDefault = propGen.getDefaultProperties()
        PropMan propmanExternal = propGen.getExternalProperties()
        List<String> userCommandList = propmanExternal.get('') ?: []
        userCommandList.each{ commandName ->
            command(commandName)
        }
    }

    void command(String commandName){
        MethodInfomation info = commandMap[commandName]
        if (info){
            Class clazz = info.clazz

            //Lately Init
            if (initLatelyMap[clazz] && initMap[clazz]  && !initCheckMap[clazz])
                initInstance(clazz)

            //Before
            if (beforeMap[clazz])
                runMethod(beforeMap[clazz])

            //Command
            runMethod(info)
            
            //After
            if (afterMap[clazz])
                runMethod(afterMap[clazz])
        }

    }

    Object runMethod(MethodInfomation info){
        return runMethod(info.instance, info.methodName)
    }

    Object runMethod(Object instance, String methodName){
        Object result
        try {
            Method method = instance.getClass().getMethod(methodName, null)
            result = method.invoke(instance, null)
        }catch(e){
            e.printStackTrace()
            throw e
        }
        return result
    }

}
