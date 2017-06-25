package install.configuration

import install.configuration.annotation.*
import install.configuration.annotation.method.After
import install.configuration.annotation.method.Before
import install.configuration.annotation.method.Command
import install.configuration.annotation.method.Init
import install.configuration.annotation.type.Data
import install.configuration.annotation.type.Employee
import install.configuration.annotation.type.Job
import install.configuration.annotation.type.Task
import install.configuration.reflection.FieldInfomation
import install.configuration.reflection.MethodInfomation
import install.configuration.reflection.ReflectInfomation
import jaemisseo.man.util.Util

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Created by sujkim on 2017-06-12.
 */
class Config {

    //Default
    Map<Class, ReflectInfomation> reflectionMap = [:]

    //Job
    Map<String, MethodInfomation> methodCommandNameMap = [:]

    //Data
    Map<String, MethodInfomation> methodMethodNameMap = [:]


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
            //Scan Method,
            List<Class> jobList = Util.findAllClasses('install', [Job, Employee])
            List<Class> taskList = Util.findAllClasses('install', [Task])
            List<Class> dataList = Util.findAllClasses('install', [Data])

            println "하하하 ${jobList.size()} / ${taskList.size()} / ${dataList.size()}"

            //Scan Config
            Class configClazz =this.getClass()
            reflectionMap[configClazz] = new ReflectInfomation(clazz: configClazz, instance: this)

            jobList.each{ Class clazz ->
                reflectionMap[clazz] = new ReflectInfomation(clazz: clazz, instance: clazz.newInstance())
                scanDefault(reflectionMap[clazz])
                scanCommand(reflectionMap[clazz])
            }

            taskList.each{ Class clazz ->
                reflectionMap[clazz] = new ReflectInfomation(clazz: clazz, instance: clazz.newInstance())
                scanDefault(reflectionMap[clazz])
            }

            dataList.each { Class clazz ->
                reflectionMap[clazz] = new ReflectInfomation(clazz: clazz, instance: clazz.newInstance())
                scanDefault(reflectionMap[clazz])
                scanMethod(reflectionMap[clazz])
            }

        }catch(Exception e){
            e.printStackTrace()
            throw e
        }
    }

    boolean scanDefault(ReflectInfomation reflect){
        Class clazz = reflect.clazz
        Object instance = reflect.instance
        clazz.getMethods().each{ Method method ->
            method.getAnnotations().each{ annotation ->
                if (annotation instanceof Init){
                    reflect.initMethod = new MethodInfomation(instance: instance, clazz: clazz, annotation: annotation, method:method, methodName: method.name)
                    boolean isLately = annotation.lately()
                    reflect.isLatelyInitMethod = isLately
                    reflect.checkInitMethod = false
                }else if (annotation instanceof Before){
                    reflect.beforeMethod = new MethodInfomation(instance: instance, clazz: clazz, annotation: annotation, method:method, methodName: method.name)
                }else if (annotation instanceof After){
                    reflect.afterMethod = new MethodInfomation(instance: instance, clazz: clazz, annotation: annotation, method:method, methodName: method.name)
                }else if (annotation instanceof Inject){
                    reflect.injectMethodNameMap[method.name] = new MethodInfomation(instance: instance, clazz: clazz, annotation: annotation, method:method, methodName: method.name)
                }
            }
        }
        clazz.getDeclaredFields().each { Field field ->
            field.getAnnotations().each{ annotation ->
                if (annotation instanceof Value){
                    reflect.valueFieldNameMap[field.name] = new FieldInfomation(instance: instance, clazz: clazz, annotation: annotation, field:field, fieldName: field.name)
                }else if (annotation instanceof Inject){
                    reflect.injectFieldNameMap[field.name] = new FieldInfomation(instance: instance, clazz: clazz, annotation: annotation, field:field, fieldName: field.name)
                }
            }
        }

    }

    boolean scanCommand(ReflectInfomation reflect){
        Class clazz = reflect.clazz
        Object instance = reflect.instance
        clazz.getDeclaredMethods().each { Method method ->
            method.getAnnotations().each { annotation ->
                if (annotation instanceof Command){
                    Command commandAnt = annotation as Command
                    methodCommandNameMap[commandAnt.value()] = new MethodInfomation(instance: instance, clazz: clazz, annotation: annotation, method:method, methodName: method.name)
                }
            }
        }
    }

    boolean scanMethod(ReflectInfomation reflect){
        Class clazz = reflect.clazz
        Object instance = reflect.instance
        clazz.getDeclaredMethods().each { Method method ->
            method.getAnnotations().each { annotation ->
                if (annotation instanceof install.configuration.annotation.method.Method) {
                    methodMethodNameMap[method.name] = new MethodInfomation(instance: instance, clazz: clazz, annotation: annotation, method:method, methodName: method.name)
                }
            }
        }
    }

    /*************************
     * INEJCT
     *************************/
    void inject(){
        //Inject to field
        List<FieldInfomation> injectFieldList = reflectionMap.findAll{ clazz, reflect ->
            reflect.injectFieldNameMap
        }.collect{ clazz, reflect ->
            reflect.injectFieldNameMap.collect{ it.value }
        }
        injectFieldList.each{ info ->
            Class clazz = info.clazz
            Object instance = info.instance
            Object injector = findInstance(clazz)
            instance[info.fieldName] = injector
        }
        //Inject to method
        List<MethodInfomation> injectMethodList = []
        reflectionMap.each{ clazz, reflect ->
            reflect.injectMethodNameMap.each{ methodName, methodInfo -> methodInfo
                injectMethodList << methodInfo
            }
        }
        injectMethodList.each{ info ->
            Class clazz = info.clazz
            Object instance = info.instance
            Object[] parameters = info.method.parameterTypes.collect{ findInstance(it) }
            runMethod(instance, info.method, parameters)
        }
    }



    /*************************
     * FIND INSTANCE
     *************************/
    Object findInstance(Class clazz){
        return findInstanceByClass(clazz)
    }

    Object findInstanceByClass(Class clazz){
        return reflectionMap[clazz].instance
    }

    Object findInstanceByAnnotation(Class annotation){
        List<Object> foundedInstanceList = findAllInstances([annotation])
        return foundedInstanceList.find{ return true }
    }

    List<Object> findAllInstances(Class annotation){
        return findAllInstances([annotation])
    }

    List<Object> findAllInstances(List<Class> annotationList){
        List<Object> foundedInstanceList = reflectionMap.findAll{ clazz, info ->
            Object o = info.clazz.getAnnotations().find{ annotationList.contains(it.annotationType()) }
            return o
        }.collect{ clazz, info ->
            info.instance
        }
        return foundedInstanceList
    }



    /*************************
     * INJECT VALUE
     *************************/
    Object injectValue(Object instance){
        //field to inject
        Map<String, FieldInfomation> valueFieldNameMap = reflectionMap[instance.getClass()].valueFieldNameMap
        valueFieldNameMap.each{ fieldName, info ->
            Value ant = (info.annotation as Value)
            String property = ant.value() ?: ant.property()
            String method = ant.method()
            def value = get(property, method)
            instance[fieldName] = value
        }
        return instance
    }

    def get(String property){
        return get(property, '')
    }

    def get(String property, String methodName){
        MethodInfomation info = methodMethodNameMap[methodName]
        if (!info)
            throw new Exception ("Doesn't Exist Value Data's Method")
        return (property) ? runMethod(info, [property].toArray()) : runMethod(info)
    }



    /*****
     * Init Instance
     *****/
    void init(){
        List<MethodInfomation> initMethodList = reflectionMap.findAll{ clazz, reflect -> reflect.initMethod }.collect{ clazz, reflect -> reflect.initMethod }
        initMethodList.each{ info ->
            Class clazz = info.clazz
            if (!reflectionMap[clazz].isLatelyInitMethod
                && reflectionMap[clazz].initMethod
                && !reflectionMap[clazz].checkInitMethod){
                    initInstance(info)
            }
        }
    }

    void initInstance(MethodInfomation info) {
        Class clazz = info.clazz
        Object instance = info.instance
        Method method = info.method
        try {
            method.invoke(instance, null)
        }catch(e){
            e.printStackTrace()
            throw e
        }
        reflectionMap[clazz].checkInitMethod = true
    }



    /*****
     * Command
     *****/
    void command(List<String> userCommandList){
        userCommandList.each{ commandName ->
            command(commandName)
        }
    }

    void command(String commandName){
        MethodInfomation commandMethodInfo = methodCommandNameMap[commandName]
        if (commandMethodInfo){
            Class clazz = commandMethodInfo.clazz

            //Lately Init
            if (reflectionMap[clazz].isLatelyInitMethod
                && reflectionMap[clazz].initMethod
                && !reflectionMap[clazz].checkInitMethod){
                    initInstance(reflectionMap[clazz].initMethod)
            }

            //Before
            if (reflectionMap[clazz].beforeMethod)
                runMethod(reflectionMap[clazz].beforeMethod)

            //Command
            runMethod(commandMethodInfo)
            
            //After
            if (reflectionMap[clazz].afterMethod)
                runMethod(reflectionMap[clazz].afterMethod)
        }

    }



    /*****
     * RUN METHOD
     *****/
    Object runMethod(MethodInfomation info){
        return runMethod(info.instance, info.method)
    }

    Object runMethod(MethodInfomation info, Object[] parameters){
        return runMethod(info.instance, info.method, parameters)
    }

    Object runMethod(Object instance, String methodName){
        return runMethod(instance, methodName, null, null)
    }

    Object runMethod(Object instance, String methodName, Class[] parameterTypes, Object[] parameters){
        try {
            Method method = instance.getClass().getMethod(methodName, parameterTypes)
            return runMethod(instance, method, parameters)
        }catch(e){
            e.printStackTrace()
            throw e
        }
    }

    Object runMethod(Object instance, Method method){
        return runMethod(instance, method, null)
    }

    Object runMethod(Object instance, Method method, Object[] parameters){
        Object result
        try {
            result = method.invoke(instance, parameters)
        }catch(e){
            e.printStackTrace()
            throw e
        }
        return result
    }

}
