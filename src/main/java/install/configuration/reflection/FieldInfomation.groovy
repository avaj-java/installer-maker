package install.configuration.reflection

import java.lang.annotation.Annotation
import java.lang.reflect.Field

/**
 * Created by sujkim on 2017-06-11.
 */
class FieldInfomation {

    Object instance
    Class clazz
    Annotation annotation
    Field field
    String fieldName

}
