package install.util

import groovy.json.JsonOutput
import org.yaml.snakeyaml.Yaml

class YamlUtil {



    /*************************
     * Generate PropertiesMap From file(YAML or YML)
     *************************/
   static Map<String, Object> generatePropertiesMapFromYmlFile(String filePath){
       Map<String, Object> propertiesMap
        // Load Yml File
        try{
            String absolutePath = getFullPath(filePath)
            File file = new File(absolutePath)
            propertiesMap = generatePropertiesMap(file)

        }catch(Exception e){
            throw e
        }
        return propertiesMap
    }





    private static Map<String, Object> generatePropertiesMap(File file){
        Map<String, Object> resultMap = [:]
        return extractPropertiesRecursivly( file, resultMap)
    }

    private static Map<String, Object> extractPropertiesRecursivly(File file, Map<String, Object> properties){
        Map<String, Map<String, String>> ymlDataMap = readYml(file)
        return extractPropertiesRecursivly('', ymlDataMap, properties)
    }

    private static extractPropertiesRecursivly(Map<String, Map<String, String>> ymlDataMap, Map<String, Object> properties){
        return extractPropertiesRecursivly('', ymlDataMap, properties)
    }

    private static Map extractPropertiesRecursivly(String propertyNameFollowingBranch, Map<String, Map<String, String>> ymlDataMapFollowingBranch, Map<String, Object> properties){
        ymlDataMapFollowingBranch.each{ String propertyNamePart, Object nextItem ->
            String nextPropertyName = propertyNameFollowingBranch ? "${propertyNameFollowingBranch}.${propertyNamePart}" : propertyNamePart
            if (nextItem instanceof Map){
                extractPropertiesRecursivly(nextPropertyName, nextItem, properties)
            }else if (nextItem instanceof String){
                properties[nextPropertyName] = nextItem
            }else if (nextItem instanceof List){
                properties[nextPropertyName] = JsonOutput.toJson(nextItem as List)
            }else if (nextItem instanceof Boolean){
                properties[nextPropertyName] = nextItem ? 'true' : 'false'
            }else if (nextItem){
                properties[nextPropertyName] = nextItem
            }
        }
        return properties
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
