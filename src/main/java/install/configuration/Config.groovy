package install.configuration

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
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
import install.configuration.exception.OutOfArgumentException
import install.configuration.reflection.FieldInfomation
import install.configuration.reflection.MethodInfomation
import install.configuration.reflection.ReflectInfomation
import install.configuration.data.PropertyProvider
import install.configuration.data.Validator
import jaemisseo.man.PropMan
import jaemisseo.man.util.Util
import org.fusesource.jansi.Ansi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method

/**
 * Created by sujkim on 2017-06-12.
 */
class Config {

    static final Logger logger = LoggerFactory.getLogger(getClass());

    //Default
    Map<Class, ReflectInfomation> reflectionMap = [:]

    //Job
    Map<String, MethodInfomation> methodCommandNameMap = [:]
    Map<String, MethodInfomation> methodCommandAliasNameMap = [:]
    Map<Class, MethodInfomation> methodMainCommandClassMap = [:]

    //Data
    Map<String, MethodInfomation> methodFilterNameMap = [:]

    String[] args

    //Task's Value Protocol
    Map<String, List<String>> lowerTaskNameAndValueProtocolListMap = [:]

    PropertiesGenerator propGen
    LogGenerator logGen

    List<String> commandCalledByUserList = []
    List<String> taskCalledByUserList = []

    //Validator
    Validator validator = new Validator()



    Config setup(String packageNameToScan, String[] args){
        try{
            scan(packageNameToScan)

            makeProperties(args)
            makeLogger()

            commandCalledByUserList = getCommandListCalledByUser(propGen.getExternalProperties())
            taskCalledByUserList = getTaskListCalledByUser(propGen.getExternalProperties())

            PropertyProvider provider = findInstanceByAnnotation(Data)
            provider.propGen = propGen
            provider.logGen = logGen

            inject()
            init()

        }catch(OutOfArgumentException ooae){
            logger.error(ooae.getMessage())
            System.exit(0)
        }catch(Exception e){
            /** [Error] **/
            logger.error('Error on Starter', e)
            throw e
        }
        return this
    }

    Config makeProperties(String[] args){
        this.args = args
        propGen = new PropertiesGenerator()
        propGen.makeDefaultProperties()
        propGen.makeProgramProperties()
        propGen.makeExternalProperties(args, lowerTaskNameAndValueProtocolListMap)
        return this
    }

    Config makeLogger(String[] args){
        logGen = new LogGenerator()
        boolean modeSystemDebugLog = false
        boolean modeSystemDebugLogFile = false

        if (modeSystemDebugLog){
            logGen.setupConsoleLogger('trace')
        }

        if (modeSystemDebugLogFile){
            logGen.setupFileLogger('system', 'trace', './', 'installer-maker-debug')
        }

        if (modeSystemDebugLog || modeSystemDebugLogFile){
            // OPTIONAL: print logback internal status messages
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory()
            StatusPrinter.print(loggerContext)
        }
        return this
    }

    List<String> getCommandListCalledByUser(PropMan propmanExternal){
        return propmanExternal.get('') ?: []
    }

    List<String> getTaskListCalledByUser(PropMan propmanExternal){
        List<String> taskCalledByUserList = []
        // -Collect Task
        findAllInstances([Task]).each{ instance ->
            String taskName = instance.getClass().getSimpleName().toLowerCase()
            if (propmanExternal.get(taskName))
                taskCalledByUserList << taskName
        }
        // -Collect Task Alias
        Map<Class, ReflectInfomation> aliasTaskReflectionMap = reflectionMap.findAll{ clazz, info ->
            info.alias && propmanExternal.get(info.alias)
        }
        aliasTaskReflectionMap.each{ clazz, info ->
            taskCalledByUserList << info.instance.getClass().getSimpleName().toLowerCase()
        }
        return taskCalledByUserList
    }


    /*************************
     * 1. Scan Class
     * 2. New Instance
     *************************/
    Config scan(String packageName){
        try {
            //1. Scan Classes
            List<Class> jobList = Util.findAllClasses(packageName, [Job, Employee])
            List<Class> taskList = Util.findAllClasses(packageName, [Task])
            List<Class> dataList = Util.findAllClasses(packageName, [Data])
            List<Class> beanList = Util.findAllClasses(packageName, [Bean])

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
            throw e
        }
        return this
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
                MethodInfomation methodInfo = new MethodInfomation(instance: instance, clazz: clazz, annotationList: method.getAnnotations().toList(), method:method, methodName: method.name)
                if (commandName)
                    methodCommandNameMap[commandName] = methodInfo
                else
                    methodMainCommandClassMap[clazz] = methodInfo
            }
            if (aliasAnt){
                commandAliasName = aliasAnt.value()
                if (commandAliasName)
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
    Config inject(){
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
        return this
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
            boolean modeRenderJansi = valueAnt.modeRenderJansi()
            //Inject Value
            if (propertyName){
                def value = getFromProvider("${prefixParam}${prefix}${propertyName}", filterName)
                if (modeRenderJansi)
                    value = renderJansi(value)
                if (!validator.validate(value, valueAnt))
                    throw new Exception()
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
            boolean modeRenderJansi = valueAnt.modeRenderJansi()
            //Inject Value
            if (propertyName){
                def value = getFromProvider("${prefixParam}${prefix}${propertyName}", filterName)
                if (modeRenderJansi)
                    value = renderJansi(value)
                if (!validator.validate(value, valueAnt))
                    throw new Exception()
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

    String renderJansi(String content){
        return (content) ? new Ansi().render(content).toString() : null
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


    /*************************
     * CLEAN Value
     *************************/
    Object cleanValue(Object instance){
        Class clazz = instance.getClass()
        //1. CLEAN VALUE to FIELD
        Map<String, FieldInfomation> valueFieldNameMap = reflectionMap[clazz].valueFieldNameMap
        valueFieldNameMap.each{ fieldName, info ->
            instance[fieldName] = null
        }

        //2. CLEAN VALUE to METHOD
        Map<String, MethodInfomation> valueMethodNameMap = reflectionMap[clazz].valueMethodNameMap
        valueMethodNameMap.each{ String methodName, MethodInfomation info ->
            Object[] parameters = [null] as Object[]
            runMethod(instance, info.method, parameters)
        }
        return instance
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

    void command(Class clazz){
        MethodInfomation commandMethodInfo = getMainCommandMethodInfo(clazz)
        doCommand(commandMethodInfo)
    }

    void command(String commandName){
        MethodInfomation commandMethodInfo = getCommandMethodInfo(commandName)
        doCommand(commandMethodInfo)
    }

    void doCommand(MethodInfomation commandMethodInfo){
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

    MethodInfomation getCommandMethodInfo(String commandName){
        return methodCommandNameMap[commandName] ?: methodCommandAliasNameMap[commandName]
    }

    MethodInfomation getMainCommandMethodInfo(Class clazz){
        return methodMainCommandClassMap[clazz]
    }

    boolean hasCommand(String commandName){
        return !!getCommandMethodInfo(commandName)
    }

    boolean hasCommand(Class clazz){
        return !!getMainCommandMethodInfo(clazz)
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
//            throw new Exception(e.getCause())
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
