package install.configuration.reflection

/**
 * Created by sujkim on 2017-06-22.
 */
class ReflectInfomation {

    Class clazz
    Object instance
    String alias

    MethodInfomation initMethod
    boolean checkInitMethod
    boolean isLatelyInitMethod
    MethodInfomation beforeMethod
    MethodInfomation afterMethod

    Map<String, FieldInfomation> valueFieldNameMap = [:]
    Map<String, MethodInfomation> valueMethodNameMap = [:]

    Map<String, FieldInfomation> injectFieldNameMap = [:]
    Map<String, MethodInfomation> injectMethodNameMap = [:]

}
