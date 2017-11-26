package install.util

import install.bean.ReportSetup
import install.configuration.Config
import install.configuration.annotation.Inject
import install.configuration.data.PropertyProvider
import jaemisseo.man.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by sujkim on 2017-03-11.
 */
class TaskUtil{

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final Integer STATUS_NOTHING = 0
    public static final Integer STATUS_TASK_DONE = 1
    public static final Integer STATUS_TASK_RUN_FAILED = 2
    public static final Integer STATUS_UNDO_QUESTION = 3
    public static final Integer STATUS_REDO_QUESTION = 4


    PropMan propman
    VariableMan varman

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
        logger.error "It is Empty Task. Implement This method."
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
        logger.info ""
        logger.info "-------------------------"
        logger.info "----- ${title}"
        logger.info "-------------------------"
    }

    protected void logMiddleTitle(String title){
        logger.info ""
        logger.info '=================================================='
        logger.info " - ${title} -"
        logger.info '=================================================='
    }

    protected void logTaskDescription(String title){
        logger.debug '=================================================='
        logger.info ":${title}"
        logger.debug '=================================================='
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
        def property = provider.parse("property")
        def value = provider.get("value")
        if (property){
            //- Set Some Value to Some Property
            if (value && property instanceof String){
                provider.setRaw(property, value)

            //- Set Some Property with JSON Object
            }else if (property instanceof Map){
                (property as Map).each{ String propertyName, def propertyValue ->
                    provider.setRaw(propertyName, propertyValue)
                }
            }
        }
    }



    /*************************
     *
     * File(YML or YML or PROPERTIES) => Map
     *
     *************************/
    Map generateMapFromPropertiesFile(File propertiesFile){
        return generateMapFromPropertiesFile(propertiesFile, [])
    }

    Map generateMapFromPropertiesFile(File propertiesFile, String propertyNameFilterTarget){
        return generateMapFromPropertiesFile(propertiesFile, [propertyNameFilterTarget])
    }

    Map generateMapFromPropertiesFile(File propertiesFile, List<String> propertyNameFilterTargetList){
        Map prop
        //Load to Map
        if (propertiesFile.name.endsWith('.yml') || propertiesFile.name.endsWith('.yaml'))
            prop = YamlUtil.generatePropertiesMap(propertiesFile, propertyNameFilterTargetList)
        else
            prop = new PropMan(propertiesFile, propertyNameFilterTargetList).properties
        return prop
    }



    /*************************
     *
     * File(YML or YML or PROPERTIES) =>  Property Values(${} Parsed) => PropMan
     *
     *************************/
    PropMan generatePropMan(String responseFilePath){
        return generatePropMan(responseFilePath, [])
    }

    PropMan generatePropMan(String responseFilePath, String excludeStartsWith){
        return generatePropMan(responseFilePath, [excludeStartsWith])
    }

    PropMan generatePropMan(String responseFilePath, List<String> excludeStartsWithList){
        PropMan responsePropMan = new PropMan(responseFilePath)
        return generatePropMan(responsePropMan, excludeStartsWithList)
    }

    PropMan generatePropMan(Map responseMap, List<String> excludeStartsWithList){
        PropMan responsePropMan = new PropMan(responseMap)
        return generatePropMan(responsePropMan, excludeStartsWithList)
    }

    PropMan generatePropMan(PropMan responsePropMan, List<String> excludeStartsWithList){
        PropMan parsedResponsePropMan = parsePropMan(responsePropMan, new VariableMan(), excludeStartsWithList)
        return parsedResponsePropMan
    }

    PropMan parsePropMan(PropMan propmanToDI, VariableMan varman){
        return parsePropMan(propmanToDI, varman, [])
    }

    PropMan parsePropMan(PropMan propmanToParse, VariableMan varman, String excludeStartsWith){
        return parsePropMan(propmanToParse, varman, [excludeStartsWith])
    }

    PropMan parsePropMan(PropMan propmanToParse, VariableMan varman, List<String> excludeStartsWithList){
        varman.putFuncs([
                fullpath: { VariableMan.OnePartObject it ->
                    it.substitutes = (it.substitutes) ? FileMan.getFullPath(it.substitutes) : ""
                }
        ])
        /** Parse ${Variable} Exclude Levels **/
        // -BasicVariableOnly
        Map map = propmanToParse.properties
        if (excludeStartsWithList){
            map.each{ String propertyName, def value ->
                if ( value && value instanceof String && !excludeStartsWithList.findAll{ propertyName.startsWith(it) } )
                    propmanToParse.set(propertyName, varman.parseDefaultVariableOnly(value))
            }
        }else{
            map.each{ String propertyName, def value ->
                if ( value && value instanceof String )
                    propmanToParse.set(propertyName, varman.parseDefaultVariableOnly(value))
            }
        }
        // -All
        (1..5).each{ int i ->
            map = propmanToParse.properties
            varman.putVariables(map)
            if (excludeStartsWithList){
                map.each{ String propertyName, def value ->
                    if ( value && value instanceof String && !excludeStartsWithList.findAll{ propertyName.startsWith(it) } )
                        propmanToParse.set(propertyName, varman.parse(value))
                }
            }else{
                map.each{ String propertyName, def value ->
                    if ( value && value instanceof String )
                        propmanToParse.set(propertyName, varman.parse(value))
                }
            }
        }
        return propmanToParse
    }


}
