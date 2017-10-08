package install.configuration.reflection

/**
 * Created by sujkim on 2017-06-22.
 */
class ReflectInfomation {

    //Own
    Class clazz
    Object instance
    String alias

    //Init
    MethodInfomation initMethod
    boolean checkInitMethod
    boolean isLatelyInitMethod

    //Before
    MethodInfomation beforeMethod

    //After
    MethodInfomation afterMethod

    //Value
    Map<String, FieldInfomation> valueFieldNameMap = [:]
    Map<String, MethodInfomation> valueMethodNameMap = [:]

    //Inject
    Map<String, FieldInfomation> injectFieldNameMap = [:]
    Map<String, MethodInfomation> injectMethodNameMap = [:]

}
