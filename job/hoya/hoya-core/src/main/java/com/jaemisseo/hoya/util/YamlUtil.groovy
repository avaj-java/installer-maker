package com.jaemisseo.hoya.util

import groovy.json.JsonOutput
import jaemisseo.man.PropMan
import org.yaml.snakeyaml.Yaml

class YamlUtil {



    /*************************
     * Generate PropertiesMap From file(YAML or YML)
     *************************/
   static Map<String, Object> generatePropertiesMap(String filePath){
        return generatePropertiesMap(filePath, [])
    }

    static Map<String, Object> generatePropertiesMap(String filePath, List<String> propertyNameFilterTargetList){
        File file = new File(getFullPath(filePath))
        return generatePropertiesMap(file, propertyNameFilterTargetList)
    }

    static Map<String, Object> generatePropertiesMap(File file){
        Map<String, Object> resultMap = [:]
        return extractPropertiesRecursivly(file, resultMap, [])
    }

    static Map<String, Object> generatePropertiesMap(File file, List<String> propertyNameFilterTargetList){
        Map<String, Object> resultMap = [:]
        return extractPropertiesRecursivly(file, resultMap, propertyNameFilterTargetList)
    }

    private static Map<String, Object> extractPropertiesRecursivly(File file, Map<String, Object> resultMap, List<String> propertyNameFilterTargetList){
        Map<String, Map<String, String>> ymlDataMap = readYml(file)
        return extractPropertiesRecursivly('', ymlDataMap, resultMap, propertyNameFilterTargetList)
    }

    private static Map<String, Object> extractPropertiesRecursivly(Map<String, Map<String, String>> ymlDataMap, Map<String, Object> resultMap, List<String> propertyNameFilterTargetList){
        return extractPropertiesRecursivly('', ymlDataMap, resultMap, propertyNameFilterTargetList)
    }

    private static Map<String, Object> extractPropertiesRecursivly(String propertyNameFollowingBranch, Map<String, Map<String, String>> ymlDataMapFollowingBranch, Map<String, Object> resultMap, List<String> propertyNameFilterTargetList){
        ymlDataMapFollowingBranch.each { String propertyNamePart, Object nextItem ->
            String nextPropertyName = propertyNameFollowingBranch ? "${propertyNameFollowingBranch}.${propertyNamePart}" : propertyNamePart
            if (nextItem instanceof Map) {
                extractPropertiesRecursivly(nextPropertyName, nextItem, resultMap, propertyNameFilterTargetList)
            }else{
                if (PropMan.isMatchingProperty(nextPropertyName, propertyNameFilterTargetList))
                    putValue(nextPropertyName, nextItem, resultMap)
            }
        }
        return resultMap
    }

    private static void putValue(String nextPropertyName, Object nextItem, Map<String, Object> properties){
        if (nextItem instanceof String) {
            properties[nextPropertyName] = nextItem
        } else if (nextItem instanceof List) {
            properties[nextPropertyName] = JsonOutput.toJson(nextItem as List)
        } else if (nextItem instanceof Boolean) {
            properties[nextPropertyName] = nextItem ? 'true' : 'false'
        } else if (nextItem) {
            properties[nextPropertyName] = String.valueOf(nextItem)
        }
    }

    private static Map<String, Map<String, String>> readYml(File file){
        Yaml yml = new Yaml()
        return yml.load(new FileInputStream(file))
    }







    private static String getFullPath(String path){
        String nowPath = System.getProperty('user.dir')
        return getFullPath(nowPath, path)
    }

    private static String getFullPath(String standardPath, String relativePath){
        if (!relativePath)
            return null
        if (isItStartsWithRootPath(relativePath))
            return relativePath
        if (!standardPath)
            return ''
        relativePath.split(/[\/\\]/).each{ String next ->
            if (next.equals('..')){
                standardPath = new File(standardPath).getParent()
            }else if (next.equals('.')){
                // ignore
            }else if (next.equals('~')){
                standardPath = System.getProperty("user.home")
            }else{
                standardPath = "${standardPath}/${next}"
            }
        }
        return new File(standardPath).path
    }

    private static boolean isItStartsWithRootPath(String path){
        boolean isRootPath = false
        path = new File(path).path
        if (path.startsWith('/') || path.startsWith('\\'))
            return true
        File.listRoots().each{
            String rootPath = new File(it.path).path
//            println "Root Path:" + rootPath
            if (path.startsWith(rootPath) || path.startsWith(rootPath.toLowerCase()))
                isRootPath = true
        }
        return isRootPath
    }

}
