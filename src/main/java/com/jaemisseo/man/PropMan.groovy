package com.jaemisseo.man

import com.jaemisseo.man.util.FileSetup
import groovy.json.JsonSlurper

/**
 * Created by sujung on 2016-09-25.
 */
class PropMan{

    Properties properties = new Properties()
    String nowPath
    String filePath
    Closure beforeGetClosure

    PropMan(){
        init()
    }

    PropMan(Map propMap){
        merge(propMap)
        init()
    }



    PropMan init(){
        nowPath = System.getProperty('user.dir')
        return this
    }

    PropMan clear(){
        properties.clear()
        return this
    }

    PropMan setBeforeGetProp(Closure beforeGetClosure){
        this.beforeGetClosure = beforeGetClosure
        return this
    }

    /**
     * SET DATA
     */
    void set(String key, def data){
        properties[key] = data
    }

    /**
     * GET DATA
     */
    def get(String key){
        return get(key, null)
    }

    def get(String key, Closure beforeClosure){
        def val = properties[key]
        if (val) {
            if (beforeClosure || beforeGetClosure){
                if (beforeClosure)
                    beforeClosure(key, val)
                else if (beforeGetClosure)
                    beforeGetClosure(key, val)
                val = properties[key]
            }
        }
        return val
    }

    Boolean getBoolean(String key){
        return (get(key, null) as Boolean)
    }

    /**
     * GET PARSED JSON DATA
     */
    def parse(String key){
        parse(key, null)
    }

    def parse(String key, Closure beforeClosure){
        String val = properties[key]
        if (val){
            if (beforeClosure || beforeGetClosure){
                if (beforeClosure)
                    beforeClosure(key, val)
                else if (beforeGetClosure)
                    beforeGetClosure(key, val)
                val = properties[key]
            }
            String valToCompare = val.trim()
            int lastIdx = valToCompare.length() -1
            if ( (valToCompare.indexOf('[') == 0 && valToCompare.lastIndexOf(']') == lastIdx) || (valToCompare.indexOf('{') == 0 && valToCompare.lastIndexOf('}') == lastIdx) ){
                def obj = new JsonSlurper().parseText(val)
                if (obj)
                    return obj
            }
        }
        return val ?: null
    }



    /**
     * LOAD PROPERTIES
     */
    PropMan readFile(List paths, String filename){
        def results = []
        paths.each { String path ->
            def result = [:]
            try{
                readFile("${path}/${filename}")
                result.isOk = true

            }catch(Exception){
                result.error = Exception
            }
            results << result
        }
        // Check Error
        if (!results.findAll{ return it.isOk }){
            results.each{
                println it.error
            }
            throw new Exception("Couldn't Find Properties File")
        }
    }

    PropMan readFile(String filePath){
        // Load Properties File
        try{
            String absolutePath = getFullPath(filePath)
            File file = new File(absolutePath)
            load(file)

        }catch(Exception e){
            throw e
        }
    }

    PropMan readResource(String absolutePath){
        URL url = Thread.currentThread().getContextClassLoader().getResource(absolutePath)
        File file = new File(url.getFile())
        load(file)
        return this
    }

    PropMan load(File file){
        try{
            String text = getTextFromFile(file)
            properties.load( new StringReader(text.replace('\\','\\\\')) )
            filePath = file.path

        }catch(Exception e){
            throw e
        }
        return this
    }

    String getTextFromFile(File file){
        return getTextFromFile(file, new FileSetup())
    }

    String getTextFromFile(File file, FileSetup fileSetup){
        List<String> lineList = loadFileContent(file, fileSetup)
        return lineList.join(System.getProperty("line.separator"))
    }

    int size(){
        return properties.size()
    }

    Map getMatchingMap(PropMan bPropman){
        return getMatchingMap(bPropman.properties)
    }

    Map getMatchingMap(Map bPropMap){
        Map aPropMap = this.properties
        return getMatchingMap(aPropMap, bPropMap)
    }

    Map getMatchingMap(Map aPropMap, Map bPropMap){
        return aPropMap.findAll{ bPropMap.containsKey(it.key) }
    }

    Map getNotMatchingMap(PropMan bPropman){
        return getNotMatchingMap(bPropman.properties)
    }

    Map getNotMatchingMap(Map bPropMap){
        Map aPropMap = this.properties
        return getNotMatchingMap(aPropMap, bPropMap)
    }

    Map getNotMatchingMap(Map aPropMap, Map bPropMap){
        return aPropMap.findAll{ !bPropMap.containsKey(it.key) }
    }


    //Check Condition
    boolean match(def condition){
        return !!find(properties, condition)
    }







    private List<String> loadFileContent(File f, FileSetup opt){
        String encoding = opt.encoding
        List<String> lineList = new ArrayList<String>()
        String line
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(f), encoding)
            BufferedReader br = new BufferedReader(isr)
            while ((line = br.readLine()) != null){
                lineList.add(line)
            }
            isr.close()
            br.close()

        } catch (Exception ex) {
            throw ex
        }

        return lineList
    }






    PropMan merge(String filePath){
        return mergeFile(filePath)
    }

    PropMan mergeFile(String filePath){
        String absolutePath = getFullPath(filePath)
        Properties properties = new PropMan().readFile(absolutePath).properties
        return merge(properties)
    }

    PropMan mergeResource(String filePath){
        Properties properties = new PropMan().readResource(filePath).properties
        return merge(properties)
    }

    PropMan merge(PropMan otherPropman){
        return merge( otherPropman.properties )
    }

    PropMan merge(Properties otherProperties){
        Map propMap = otherProperties
        return merge( (Map) propMap )
    }

    PropMan merge(Map propMap){
        propMap.each{
            properties[it.key] = it.value
        }
        return this
    }



    Map diffMap(PropMan with){
        return diffMap(with.properties)
    }

    Map diffMap(Map with){
        return getMatchingMap(with).findAll{ !it.value.equals(with[it.key]) }
    }



    void validate(def checkList){
        checkList.each { propKey ->
            if (!properties[propKey])
                throw new Exception("Doesn't exist ${propKey} on install.properties!!!  Please Check install.properties")
        }
    }


    String getFullPath(String path){
        return getFullPath(nowPath, path)
    }

    String getFullPath(String standardPath, String relativePath){
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

    boolean isItStartsWithRootPath(String path){
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

    def find(def object, def condition){
        if (object instanceof Map){
            def matchedObj = getMatchedObject(object, condition)
            return matchedObj
        }else if (object instanceof List){
            List results = []
            object.each{
                def matchedObj = getMatchedObject(it, condition)
                if (matchedObj)
                    results << matchedObj
            }
            return results
        }
    }

    def getMatchedObject(def object, def condition){
        if (condition instanceof String){
            condition = [id:condition]
        }
        if (condition instanceof List){
            for (int i=0; i<condition.size(); i++){
                if (find(object, condition[i]))
                    return object
            }
            return null //No Matching
        }
        if (condition instanceof Map){
            for (String key : (condition as Map).keySet()){
                String attributeValue = object[key]
                def conditionValue = condition[key]
                if (attributeValue){
                    if (conditionValue instanceof String && attributeValue == conditionValue){
                    }else if (conditionValue instanceof List && conditionValue.contains(attributeValue)){
                    }else{
                        return //No Matching
                    }
                }else{
                    return //No Matching
                }
            }
            return object
        }
        if (condition == null)
            return object
    }


}
