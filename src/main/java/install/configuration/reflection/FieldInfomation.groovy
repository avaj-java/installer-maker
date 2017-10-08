package install.configuration.reflection

import java.lang.annotation.Annotation
import java.lang.reflect.Field

/**
 * Created by sujkim on 2017-06-11.
 */
class FieldInfomation {

    //Parent(Class)
    Object instance
    Class clazz

    //Field
    List<Annotation> annotationList
    Field field
    Class fieldType
    String fieldName

    Annotation findAnnotation(Class annotationClass){
        return annotationList.find{ it.annotationType() == annotationClass }
    }

}
