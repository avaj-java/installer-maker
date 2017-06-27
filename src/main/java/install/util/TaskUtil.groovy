package install.util

import install.bean.ReportSetup
import install.configuration.Config
import install.configuration.annotation.Inject
import install.data.PropertyProvider
import jaemisseo.man.*

/**
 * Created by sujkim on 2017-03-11.
 */
class TaskUtil{

    public static final Integer STATUS_NOTHING = 0
    public static final Integer STATUS_TASK_DONE = 1
    public static final Integer STATUS_TASK_RUN_FAILED = 2
    public static final Integer STATUS_UNDO_QUESTION = 3
    public static final Integer STATUS_REDO_QUESTION = 4


    PropMan propman
    VariableMan varman
    SqlMan sqlman
    FileMan fileman
    QuestionMan qman

    Integer status
    List reportMapList = []
    List rememberAnswerLineList = []
    String undoSign = '<'
    String redoSign = '>'

    Config config
    PropertyProvider provider

    @Inject void setConfig(Config config){ this.config = config }
    @Inject void setProvider(PropertyProvider provider){ this.provider = provider }

    /*************************
     * RUN
     *************************/
    Integer run(){
        //TODO: Override And Implement
        println "It is Empty Task. Implement This method."
    }



    /*************************
     * REPORT
     *************************/
    void reportWithConsole(ReportSetup reportSetup, List reportMapList){
        //TODO: Override And Implement
    }

    void reportWithText(ReportSetup reportSetup, List reportMapList){
        //TODO: Override And Implement
    }

    void reportWithExcel(ReportSetup reportSetup, List reportMapList){
        //TODO: Override And Implement
    }

    List<String> buildForm(String propertyPrefix){
        //TODO: Override And Implement
        // To Build 'Question User Response Form'
        return []
    }




    /*************************
     * Print Title
     *************************/
    protected void logBigTitle(String title){
        println "\n\n-------------------------"
        println "----- ${title}"
        println "-------------------------"
    }

    protected void logMiddleTitle(String title){
        println '\n=================================================='
        println " - ${title} -"
        println '=================================================='
    }




    /*************************
     * Check Question's Undo & Redo
     *************************/
    boolean checkUndoQuestion(String yourAnswer){
        //'Please Show Preview Question'
        return yourAnswer.equals(undoSign)
    }

    boolean checkRedoQuestion(String yourAnswer){
        //'Please Show Preview Question'
        return yourAnswer.equals(redoSign)
    }

    /*************************
     * Remember Answer
     *************************/
    protected void rememberAnswer(String yourAnswer){
        String propertyPrefix = provider.propertyPrefix
        rememberAnswerLineList.add("${propertyPrefix}answer.default=${yourAnswer}")
    }

    /*************************
     * Set Property
     *************************/
    protected void set(String propertyName, def value){
        provider.set(propertyName, value)
    }

    protected void setPropValue(){
        //Set Some Property
        def property = provider.parse("property")

        if (property instanceof String){
            def value = provider.get("value")
            provider.setRaw(property, value)
            
        }else if (property instanceof Map){
            (property as Map).each{ String propName, def propValue ->
                provider.setRaw(propName, propValue)
            }
        }
    }

}
