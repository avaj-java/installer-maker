import jaemisseo.man.util.Util
import org.junit.Test

/**
 * Created by sujkim on 2017-06-02.
 */
class TaskTest {

    @Test
    void taks_test(){
        Util.findAllClasses('').each { Class clazz ->
            println clazz
            println clazz.getAnnotations()
            println clazz.getDeclaredAnnotations()
            println clazz.getDeclaredConstructors()
            println clazz.getDeclaredFields()
            println clazz.getDeclaredMethods()
            println '//////////\n'
        }

//        Util.findAllClasses('install.task')
//
//        Util.findAllClasses('install.task', Undoable)
//
//        Util.findAllClasses('install.task', [Undoable, UndoMore])


    }


}
