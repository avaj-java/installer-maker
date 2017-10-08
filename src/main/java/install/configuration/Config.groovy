package install.configuration

import install.configuration.annotation.*
import install.configuration.annotation.method.After
import install.configuration.annotation.method.Before
import install.configuration.annotation.method.Command
import install.configuration.annotation.method.Filter
import install.configuration.annotation.method.Init
import install.configuration.annotation.type.Bean
import install.configuration.annotation.type.Data
import install.configuration.annotation.type.Employee
import install.configuration.annotation.type.Job
import install.configuration.annotation.type.Task
import install.configuration.annotation.type.TerminalValueProtocol
import install.configuration.reflection.FieldInfomation
import install.configuration.reflection.MethodInfomation
import install.configuration.reflection.ReflectInfomation
import jaemisseo.man.util.Util

import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method

/**
 * Created by sujkim on 2017-06-12.
 */
class Config {

    //Default
    Map<Class, ReflectInfomation> reflectionMap = [:]

    //Job
    Map<String, MethodInfomation> methodCommandNameMap = [:]
    Map<String, MethodInfomation> methodCommandAliasNameMap = [:]

    //Data
    Map<String, MethodInfomation> methodFilterNameMap = [:]

    //Task's Value Protocol
    Map<String, List<String>> lowerTaskNameAndValueProtocolListMap = [:]

    InstallerPropertiesGenerator propGen
    InstallerLogGenerator logGen



    void makeProperties(String[] args){
        propGen = new InstallerPropertiesGenerator()
        propGen.makeExternalProperties(args, lowerTaskNameAndValueProtocolListMap)
        propGen.makeDefaultProperties()
        propGen.genResourceSingleton('builder', 'defaultProperties/builder.default.properties')
        propGen.genResourceSingleton('receptionist', 'defaultProperties/receptionist.default.properties')
        propGen.genResourceSingleton('installer', 'defaultProperties/installer.default.properties')
        propGen.genResourceSingleton('macgyver', 'defaultProperties/macgyver.default.properties')
    }

    void makeLoger(){
        logGen = new InstallerLogGenerator()
    }


    /*************************
     * 1. Scan Class
     * 2. New Instance
     *************************/
    void scan(){
        try {
            //1. Scan Classes
            List<Class> jobList = Util.findAllClasses('install', [Job, Employee])
            List<Class> taskList = Util.findAllClasses('install', [Task])
            List<Class> dataList = Util.findAllClasses('install', [Data])
            List<Class> beanList = Util.findAllClasses('install', [Bean])

            //SJTEST
//            println "Job:${jobList.size()} / Task:${taskList.size()} / Data:${dataList.size()} / Bean:${beanList.size()}"

            //2. Scan Method & Field
            //- Config
            Class configClazz =this.getClass()
            reflectionMap[configClazz] = new ReflectInfomation(clazz: configClazz, instance: this)

            //- Job
            jobList.each{ Class clazz ->
                reflectionMap[clazz] = new ReflectInfomation(clazz: clazz, instance: clazz.newInstance())
                scanDefault(reflectionMap[clazz])
                scanCommand(reflectionMap[clazz])
            }

            //- Task
            taskList.each{ Class clazz ->
                reflectionMap[clazz] = new ReflectInfomation(clazz: clazz, instance: clazz.newInstance())
                scanDefault(reflectionMap[clazz])
                // ValueProtocol
                TerminalValueProtocol protocolAnt = clazz.getAnnotation(TerminalValueProtocol)
                if (protocolAnt && protocolAnt.value())
                    lowerTaskNameAndValueProtocolListMap[clazz.getSimpleName().toLowerCase()] = protocolAnt.value().toList()
            }

            //- Data
            dataList.each { Class clazz ->
                reflectionMap[clazz] = new ReflectInfomation(clazz: clazz, instance: clazz.newInstance())
                scanDefault(reflectionMap[clazz])
                scanMethod(reflectionMap[clazz])
            }

            //- Bean
            beanList.each{ Class clazz ->
                reflectionMap[clazz] = new ReflectInfomation(clazz: clazz, instance: clazz.newInstance())
                scanDefault(reflectionMap[clazz])
            }

        }catch(Exception e){
            e.printStackTrace()
            throw e
        }
    }

    boolean scanDefault(ReflectInfomation reflect){
        Class clazz = reflect.clazz
        Object instance = reflect.instance
        //- Type
        clazz.getAnnotations().each{ annotation ->
            if (annotation instanceof Alias){
                reflect.alias = annotation.value()
            }
        }
        //- Method
        clazz.getMethods().each{ Method method ->
            method.getAnnotations().each{ annotation ->
                if (annotation instanceof Init){
                    reflect.initMethod = new MethodInfomation(instance: instance, clazz: clazz, annotationList: method.getAnnotations().toList(), method:method, methodName: method.name)
                    boolean isLately = annotation.lately()
                    reflect.isLatelyInitMethod = isLately
                    reflect.checkInitMethod = false
                }else if (annotation instanceof Before){
                    reflect.beforeMethod = new MethodInfomation(instance: instance, clazz: clazz, annotationList: method.getAnnotations().toList(), method:method, methodName: method.name)
                }else if (annotation instanceof After) {
                    reflect.afterMethod = new MethodInfomation(instance: instance, clazz: clazz, annotationList: method.getAnnotations().toList(), method: method, methodName: method.name)
                }else if (annotation instanceof Value){
                    reflect.valueMethodNameMap[method.name] = new MethodInfomation(instance: instance, clazz: clazz, annotationList: method.getAnnotations().toList(), method:method, methodName: method.name, parameterTypes:method.parameterTypes.toList())
                }else if (annotation instanceof Inject){
                    reflect.injectMethodNameMap[method.name] = new MethodInfomation(instance: instance, clazz: clazz, annotationList: method.getAnnotations().toList(), method:method, methodName: method.name, parameterTypes:method.parameterTypes.toList())
                }
            }
        }
        //- Field
        clazz.getDeclaredFields().each { Field field ->
            field.getAnnotations().each{ annotation ->
                if (annotation instanceof Value){
                    reflect.valueFieldNameMap[field.name] = new FieldInfomation(instance: instance, clazz: clazz, annotationList: field.getAnnotations().toList(), field:field, fieldName: field.name, fieldType: field.getType())
                }else if (annotation instanceof Inject){
                    reflect.injectFieldNameMap[field.name] = new FieldInfomation(instance: instance, clazz: clazz, annotationList: field.getAnnotations().toList(), field:field, fieldName: field.name, fieldType: field.getType())
                }
            }
        }
    }

    boolean scanCommand(ReflectInfomation reflect){
        Class clazz = reflect.clazz
        Object instance = reflect.instance
        clazz.getDeclaredMethods().each { Method method ->
            Command commandAnt = method.getAnnotation(Command)
            Alias aliasAnt = method.getAnnotation(Alias)
            String commandName
            String commandAliasName
            if (commandAnt){
                commandName = commandAnt.value()
                methodCommandNameMap[commandName] = new MethodInfomation(instance: instance, clazz: clazz, annotationList: method.getAnnotations().toList(), method:method, methodName: method.name)
            }
            if (aliasAnt){
                commandAliasName = aliasAnt.value()
                methodCommandAliasNameMap[commandAliasName] = methodCommandNameMap[commandName]
            }
        }
    }

    boolean scanMethod(ReflectInfomation reflect){
        Class clazz = reflect.clazz
        Object instance = reflect.instance
        clazz.getDeclaredMethods().each { Method method ->
            method.getAnnotations().each { annotation ->
                if (annotation instanceof Filter) {
                    methodFilterNameMap[method.name] = new MethodInfomation(instance: instance, clazz: clazz, annotationList: method.getAnnotations().toList(), method:method, methodName: method.name)
                }
            }
        }
    }





    /*************************
     * INEJCT Bean
     *************************/
    void inject(){
        //1. INJECT to FIELD
        List<FieldInfomation> injectFieldList = reflectionMap.findAll{ clazz, reflect ->
            reflect.injectFieldNameMap
        }.collect{ clazz, reflect ->
            reflect.injectFieldNameMap.collect{ it.value }
        }
        injectFieldList.each{ info ->
            Class clazz = info.clazz
            Object instance = info.instance
            Object injector = findInstance(clazz)
            //inject
            instance[info.fieldName] = injector
        }
        //2. INJECT to METHOD
        List<MethodInfomation> injectMethodList = []
        reflectionMap.each{ clazz, reflect ->
            reflect.injectMethodNameMap.each{ methodName, methodInfo ->
                injectMethodList << methodInfo
            }
        }
        injectMethodList.each{ info ->
            Object[] parameters = info.method.parameterTypes.collect{ findInstance(it) }
            //inject
            runMethod(info.instance, info.method, parameters)
        }
    }

    /*************************
     * INJECT Value
     *************************/
    Object injectValue(Object instance){
        return injectValue(instance, '')
    }

    Object injectValue(Object instance, String prefixParam){
        Class clazz = instance.getClass()
        //1. INJECT VALUE to FIELD
        Map<String, FieldInfomation> valueFieldNameMap = reflectionMap[clazz].valueFieldNameMap
        valueFieldNameMap.each{ fieldName, info ->
            //Get Value
            Value valueAnt = info.findAnnotation(Value)
            String propertyName = valueAnt.value() ?: valueAnt.name() ?: ''
            String filterName = valueAnt.filter() ?: getFilterName(info.fieldType)
            String prefix = valueAnt.prefix() ?: ''
            //Inject Value
            if (propertyName){
                def value = getFromProvider("${prefixParam}${prefix}${propertyName}", filterName)
                validate(value, valueAnt)
                if (value != null)
                    instance[fieldName] = value
            //Inject New Object with Values
            }else{
                def newFieldObject = info.fieldType.newInstance()
                instance[fieldName] = newFieldObject
                injectValue(newFieldObject, "${prefixParam}${prefix}")
            }
        }

        //2. INJECT VALUE to METHOD
        Map<String, MethodInfomation> valueMethodNameMap = reflectionMap[clazz].valueMethodNameMap
        valueMethodNameMap.each{ String methodName, MethodInfomation info ->
            //Get Value
            Value valueAnt = info.findAnnotation(Value)
            String propertyName = valueAnt.value() ?: valueAnt.name() ?:''
            String filterName = valueAnt.filter() ?: getFilterName(info.parameterTypes[0])
            String prefix = valueAnt.prefix() ?: ''
            //Inject Value
            if (propertyName){
                def value = getFromProvider("${prefixParam}${prefix}${propertyName}", filterName)
                validate(value, valueAnt)
                Object[] parameters = [value] as Object[]
                runMethod(instance, info.method, parameters)
            //Inject Object
            }else{
            }
        }
        return instance
    }

    def getFromProvider(String property){
        return getFromProvider(property, '')
    }

    def getFromProvider(String property, String filterName){
        MethodInfomation info = methodFilterNameMap[filterName]
        if (!info)
            throw new Exception ("Doesn't Exist Data Filter [${filterName}]")
        return (property) ? runMethod(info, [property].toArray()) : runMethod(info)
    }

    String getFilterName(Class type){
        switch (type){
            case String:
                return 'getString'
                break
            case {it == Integer || it == int}:
                return 'getInteger'
                break
            case {it == Short || it == short}:
                return 'getShort'
                break
            case {it == Double || it == double}:
                return 'getDouble'
                break
            case {it == Long || it == long}:
                return 'getLong'
                break
            case {it == Boolean || it == boolean}:
                return 'getBoolean'
                break
            case List:
                return 'getList'
                break
            case Map:
                return 'getMap'
                break
            default:
                return 'get'
                break
        }
        return 'get'
    }

    boolean validate(def value, Value valueAnt){
        boolean isOk = false
        String propertyName = valueAnt.value() ?: valueAnt.name() ?:''

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

        if (required && !value)
            throw Exception()

        if (englishOnly && !value)
            throw Exception()

        if (numberOnly && !value)
            throw Exception()

        if (charOnly && !value)
            throw Exception()

        if (minLength > 0 && !value)
            throw Exception()

        if (maxLength > 0 && !value)
            throw Exception()

        if (maxLength > 0 && !value)
            throw Exception()

        return isOk
    }


    /*************************
     * INIT INSTANCE
     *************************/
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



    /*************************
     * Command
     *************************/
    void command(List<String> userCommandList){
        userCommandList.each{ commandName ->
            command(commandName)
        }
    }

    void command(String commandName){
        MethodInfomation commandMethodInfo = methodCommandNameMap[commandName] ?: methodCommandAliasNameMap[commandName]

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
     * FIND ALL ANNOTATION (from Class)
     *************************/
    List<Annotation> findAllAnnotationFromClass(Class clazz){
        return clazz.getAnnotations().toList()
    }

    List<Annotation> findAllAnnotationFromClass(Class clazz, Class annotationClass){
        return findAllAnnotationFromClass(clazz, [annotationClass])
    }

    List<Annotation> findAllAnnotationFromClass(Class clazz, List<Class> annotationClassList){
        List<Annotation> resultList = []
        if (clazz.getAnnotations().toList().findAll{ annotationClassList.contains(it.annotationType()) })
            resultList = clazz.getAnnotations().toList()
        return resultList
    }




    /*************************
     * FIND ALL ANNOTATION (from Class's Member)
     *************************/
    Map<Member, List<Annotation>> findAllAnnotationFromClassMember(Class clazz){
        Map resultMap = [:]
        clazz.getMethods().each { Method method ->
            resultMap[method] = method.getAnnotations().toList()
        }
        clazz.getMethods().each { Field field ->
            resultMap[field] = field.getAnnotations().toList()
        }
        return resultMap
    }

    Map<Member, List<Annotation>> findAllAnnotationFromClassMember(Class clazz, Class annotationClass){
        return findAllAnnotationFromClassMember(clazz, [annotationClass])
    }

    Map<Member, List<Annotation>> findAllAnnotationFromClassMember(Class clazz, List<Class> annotationClassList){
        Map resultMap = [:]
        clazz.getMethods().each { Method method ->
            List<Annotation> foundAnnotationClassList = method.getAnnotations().findAll{ annotationClassList.contains(it.annotationType()) }
            if (foundAnnotationClassList)
                resultMap[method] = foundAnnotationClassList
        }
        clazz.getDeclaredFields().each { Field field ->
            List<Annotation> foundAnnotationClassList = field.getAnnotations().findAll{ annotationClassList.contains(it.annotationType()) }
            if (foundAnnotationClassList)
                resultMap[field] = foundAnnotationClassList
        }
        return resultMap
    }

}
