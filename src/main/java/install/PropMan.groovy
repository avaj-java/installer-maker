package install

import groovy.json.JsonSlurper

/**
 * Created by sujung on 2016-09-25.
 */
class PropMan extends Properties{

    String programPath

    PropMan(String programPath){
        this.programPath = programPath
    }

    def parse(String key){
        String val = this[key]
        if (val){
            String valToCompare = val.trim()
            int lastIdx = valToCompare.length() -1
            if ( (valToCompare.indexOf('[') == 0 && valToCompare.lastIndexOf(']') == lastIdx) || (valToCompare.indexOf('{') == 0 && valToCompare.lastIndexOf('}') == lastIdx) ){
                def obj = new JsonSlurper().parseText(val)
                if (obj)
                    return obj
            }
        }
        return val
    }

    PropMan getFIle(def paths, String filename){
        def exProp = [:]
        return getFile(paths, filename, exProp)
    }

    PropMan getFIle(def paths, String filename, def exProp){
        String programPath = programPath

        // Load Properties File
        def results = []
        paths.each { String path ->
            def result = [:]
            try{
                String absolutePath = getFullPath(programPath, path)
                File file = new File("${absolutePath}/${filename}")
                result.path = file.path
                file.withInputStream {
                    super.load(it)
                    result.isOk = true
                }
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

        // Merge prop with exProp
        exProp.each{
            super[it.key] = it.value
        }
        return this
    }

    void validate(def checkList){
        checkList.each { propKey ->
            if (!super[propKey])
                throw new Exception("Doesn't exist ${propKey} on install.properties!!!  Please Check install.properties")
        }
    }


    String getFullPath(String path){
        return getFullPath("", path)
    }

    String getFullPath(String absolutePath, String relativePath){
        if (relativePath.startsWith('/')) {
            String separator = File.separator
            int depth = absolutePath.split(/[\\]/).size()
            (2..depth).each{
                relativePath = "../${relativePath}"
            }
        }
        relativePath.split('/').each{ String next ->
            if (next.equals('..')){
                absolutePath = new File(absolutePath).getParent()
            }else if (next.equals('.')){
                // ignore
            }else if (next.equals('~')){
                absolutePath = System.getProperty("user.home")
            }else{
                absolutePath = "${absolutePath}/${next}"
            }
        }
        return absolutePath
    }
}
