package com.jaemisseo.install.configuration

import jaemisseo.man.FileMan
import jaemisseo.man.TimeMan
import jaemisseo.man.bean.FileSetup
import jaemisseo.man.configuration.context.CommanderConfig
import jaemisseo.man.configuration.annotation.type.Bean
import jaemisseo.man.configuration.annotation.type.Data
import jaemisseo.man.configuration.annotation.type.Employee
import jaemisseo.man.configuration.annotation.type.Job
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.configuration.annotation.type.Undomore
import jaemisseo.man.configuration.data.PropertyProvider
import jaemisseo.man.util.Util
import org.junit.Test

/**
 * Created by sujkim on 2017-06-24.
 */
class ConfigTest {

    /***************************************************************************
     *
     *  Generate - pre-scanned-classes-list-files
     *
     ***************************************************************************/
    @Test
    void generateClassesFiles(){
        TimeMan timeman = new TimeMan().init().start()
        println "////////////////////////// ${timeman.getTime()}"
        ['jaemisseo.man', 'install'].each{ packageName ->
            List<Class> jobList = Util.findAllClasses(packageName, [Job])
            List<Class> employeeList = Util.findAllClasses(packageName, [Employee])
            List<Class> taskList = Util.findAllClasses(packageName, [Task])
            List<Class> dataList = Util.findAllClasses(packageName, [Data])
            List<Class> beanList = Util.findAllClasses(packageName, [Bean])
            List<Class> undoableList = Util.findAllClasses(packageName, [Undoable])
            List<Class> undomoreList = Util.findAllClasses(packageName, [Undomore])
            saveClassListFile(jobList, packageName, Job.class.getSimpleName())
            saveClassListFile(employeeList, packageName, Employee.class.getSimpleName())
            saveClassListFile(taskList, packageName, Task.class.getSimpleName())
            saveClassListFile(dataList, packageName, Data.class.getSimpleName())
            saveClassListFile(beanList, packageName, Bean.class.getSimpleName())
            saveClassListFile(undoableList, packageName, Undoable.class.getSimpleName())
            saveClassListFile(undomoreList, packageName, Undomore.class.getSimpleName())
        }
        println "////////////////////////// ${timeman.getTime()}"
    }

    void saveClassListFile(List<Class> classList, String packageName, String annotationName){
        String fileExtension = "classes"
        String fileName = "${packageName.replace('.', '_')}-${annotationName}.${fileExtension}"
        println "===== ${fileName} .. ${classList.size()}"
        List<String> classPathList = classList.collect{ it.getName() }
        FileMan.write("build/scan-target-classes/${fileName}", classPathList, new FileSetup(modeAutoMkdir:true, modeAutoOverWrite:true))
        println ""
    }




    CommanderConfig config = new CommanderConfig()

    @Test
    void SimplTest(){
        config.scan('jaemisseo')
        PropertyProvider provider = (config.findInstanceByAnnotation(Data) as PropertyProvider)
        provider.propGen = config.propGen
        config.injectDependenciesToBean()
    }

    @Test
    void findAllClassesTest(){
        TimeMan timeman = new TimeMan()

        println "START ==========  ${timeman.init().start().getTime()}"
        ['jaemisseo.man', 'install'].each{ packageName ->
            List<Class> jobList = CommanderConfig.findAllClasses(packageName, [Job, Employee])
            List<Class> taskList = CommanderConfig.findAllClasses(packageName, [Task])
            List<Class> dataList = CommanderConfig.findAllClasses(packageName, [Data])
            List<Class> beanList = CommanderConfig.findAllClasses(packageName, [Bean])
        }
        println "FINISH ==========  ${timeman.getTime()}"
    }




}
