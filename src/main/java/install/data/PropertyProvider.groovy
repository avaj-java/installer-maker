package install.data

import install.configuration.InstallerLogGenerator
import install.configuration.InstallerPropertiesGenerator
import install.configuration.annotation.method.Init
import install.configuration.annotation.method.Filter
import install.configuration.annotation.type.Data
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan

/**
 * Created by sujkim on 2017-06-20.
 */
@Data
class PropertyProvider {

    PropMan propman
    String propertyPrefix
    InstallerPropertiesGenerator propGen
    InstallerLogGenerator logGen

    @Init
    void init(){
        this.propman = propGen.getDefaultProperties()
    }

    void printVersion(){
        logGen.logVersion(propGen.getDefaultProperties())
    }

    void printSystem(){
        logGen.logSystem(propGen.getDefaultProperties())
    }
    
    void shift(String name){
        this.propman = propGen.get(name)
    }

    void shift(String name, String propertyPrefix){
        this.propman = propGen.get(name)
        this.propertyPrefix = propertyPrefix
    }


    boolean checkCondition(String propertyPrefix){
        def conditionIfObj = propman.parse("${propertyPrefix}if")
        boolean isTrue = propman.match(conditionIfObj)
        return isTrue
    }


    void set(String propertyName, def value){
        propman.set("${propertyPrefix}${propertyName}", value ?: '')
    }

    void setRaw(String propertyName, def value){
        propman.set(propertyName, value ?: '')
    }


    
    /*****
     *
     *****/
    @Filter('get')
    def get(String propertyName){
        return propman.get("${propertyPrefix}${propertyName}") ?: propman.get(propertyName)
    }

    @Filter('parse')
    def parse(String propertyName){
        return propman.parse("${propertyPrefix}${propertyName}") ?: propman.parse(propertyName)
    }

    @Filter('getString')
    String getString(String propertyName){
        return propman.getString("${propertyPrefix}${propertyName}") ?: propman.getString(propertyName)
    }

    @Filter('getInteger')
    Integer getInteger(String propertyName){
        Integer localValue = propman.getInteger("${propertyPrefix}${propertyName}")
        Integer globalValue = propman.getInteger(propertyName)
        return (localValue != null) ? localValue : (globalValue != null) ? globalValue : null
    }

    @Filter('getShort')
    Short getShort(String propertyName){
        Integer localValue = propman.getInteger("${propertyPrefix}${propertyName}")
        Integer globalValue = propman.getInteger(propertyName)
        return (localValue != null) ? localValue : (globalValue != null) ? globalValue : null
    }

    @Filter('getDouble')
    Double getDouble(String propertyName){
        Integer localValue = propman.getInteger("${propertyPrefix}${propertyName}")
        Integer globalValue = propman.getInteger(propertyName)
        return (localValue != null) ? localValue : (globalValue != null) ? globalValue : null
    }

    @Filter('getLong')
    Long getLong(String propertyName){
        Integer localValue = propman.getInteger("${propertyPrefix}${propertyName}")
        Integer globalValue = propman.getInteger(propertyName)
        return (localValue != null) ? localValue : (globalValue != null) ? globalValue : null
    }

    @Filter('getBoolean')
    Boolean getBoolean(String propertyName){
        Boolean localValue = propman.getBoolean("${propertyPrefix}${propertyName}")
        Boolean globalValue = propman.getBoolean(propertyName)
        return (localValue != null) ? localValue : (globalValue != null) ? globalValue : null
    }

    @Filter('getMap')
    Map getMap(String propertyName){
        Map map = parse(propertyName)
        return map
    }

    @Filter('getList')
    List getList(String propertyName){
        List list = parse(propertyName)
        return list
    }

    @Filter('getFilePathList')
    List<String> getFilePathList(String propertyName){
        return getFilePathList(propertyName, '')
    }

    List<String> getFilePathList(String propertyName, String extention){
        String filePath = get(propertyName)
        return FileMan.getSubFilePathList(filePath, extention)
    }

    @Filter('getFilePath')
    String getFilePath(String propertyName){
        String filePath = get(propertyName)
        return FileMan.getFullPath(filePath)
    }

}
