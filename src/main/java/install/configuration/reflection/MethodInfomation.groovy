package install.configuration.reflection

import java.lang.annotation.Annotation
import java.lang.reflect.Method

/**
 * Created by sujkim on 2017-06-11.
 */
class MethodInfomation {

    //Parent(Class)
    Object instance
    Class clazz

    //Method
    List<Annotation> annotationList
    Method method
    String methodName

    //Parameter
    List<Class> parameterTypes

    Annotation findAnnotation(Class annotationClass){
        return annotationList.find{ it.annotationType() == annotationClass }
    }

}
