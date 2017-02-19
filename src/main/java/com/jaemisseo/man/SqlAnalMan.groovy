package com.jaemisseo.man

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created with IntelliJ IDEA.
 * User: sujkim
 * Date: 10/2/16
 * Time: 10:02 AM
 * To change this template use File | Settings | File Templates.
 */
class SqlAnalMan {


    class SqlObject{
        boolean isExistOnDB
        boolean isOk
        Exception error
        String query
        def arrayToCompare


        String commandType
        String objectType
        String objectName

        int objectNameIdx
        int objectTypeIdx

        String schemeName
        String password
        String datafileName
        String tablespaceName
        String tempTablespaceName
        List<String> quotaNames = []
        String userName
        List<String> tableNames = []
        String indexName
        String viewName
        String functionName
        String sequenceName

        int passwordIdx
        int datafileNameIdx
        int tablespaceNameIdx
        int tempTablespaceNameIdx
        List<Integer> quotaNameIdxs = []
        int userNameIdx
        List<Integer> tableNameIdxs = []
        int indexNameIdx
        int viewNameIdx
        int functionNameIdx
        int sequenceNameIdx

        String warnningMessage
    }









    // Analysis Sql Query
    SqlObject getAnalysisObject(String query){
        SqlObject sqlObj = new SqlObject()
//        String queryToCompare = query.replace(")", " ) ").replace("(", " ( ").replaceAll("[,]", " , ").replaceAll("[;]", " ;").replaceAll(/\s{2,}/, " ")
        String sp = "{#-%}"
        String queryToCompare = getReplaceNotInOracleQuote(query, [
            "(" : "${sp}(${sp}",
            ")" : "${sp})${sp}",
            "," : "${sp},${sp}",
            ";" : "${sp};",
            "\n" : sp,
            "\r" : sp,
            " " : sp
        ])
        sqlObj.with{
            setQuery(query)
            arrayToCompare = queryToCompare.split("([{][#][-][%][}])+")
            commandType = arrayToCompare[0].toUpperCase()
        }

        switch (sqlObj.commandType){
            case 'CREATE':
                sqlObj = analCreate(sqlObj)
                break
            case 'ALTER':
                sqlObj = analAlter(sqlObj)
                break
            case 'INSERT':
                sqlObj = analInsert(sqlObj)
                break
            case 'UPDATE':
                sqlObj = analUpdate(sqlObj)
                break
            case 'COMMENT':
                sqlObj = analComment(sqlObj)
                break
            case 'GRANT':
                sqlObj = analGrant(sqlObj)
                break
            default:
                break
        }
        return sqlObj
    }

    String getObjectType(query){
        String objectType = ''
        if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_TABLE)).size()){
            objectType = 'TABLE'
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_INDEX)).size()){
            objectType = 'INDEX'
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_VIEW)).size()){
            objectType = 'VIEW'
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_SEQUENCE)).size()){
            objectType = 'SEQUENCE'
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_FUNCTION)).size()){
            objectType = 'FUNCTION'
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_TABLESPACE)).size()){
            objectType = 'TABLESPACE'
        }else if (getMatchedList(query, getSqlPattern(SqlMan.CREATE_USER)).size()){
            objectType = 'USER'
        }
        return objectType
    }



    void addOR(def objs, String pattern){
        if (!this.patternToGetQuery) this.patternToGetQuery = ""
        objs.each{ String obj ->
            if (this.patternToGetQuery) this.patternToGetQuery += "|"
            this.patternToGetQuery += pattern.replace("{{1}}", obj)
        }
    }

    String addOR(String pattern, def objs, String patternToAdd){
        if (!pattern) pattern = ""
        objs.each{ String obj ->
            if (pattern) pattern += "|"
            pattern += patternToAdd.replace("{{1}}", obj)
        }
        return pattern
    }


    Matcher getMatchedList(content, pattern){
        String querys = removeNewLine(removeAnnotation(content))
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
        Matcher m = p.matcher(querys)
        return m
    }



    String getSqlPattern(int target){
        getSqlPattern([target])
    }
    String getSqlPattern(def targetList){
        String pattern = ''
        targetList.each{
            switch (it){
                case SqlMan.ALL:
                    pattern = getSqlPattern([SqlMan.CREATE, SqlMan.INSERT, SqlMan.UPDATE, SqlMan.ALTER, SqlMan.COMMENT, SqlMan.GRANT])
                    break
                case SqlMan.CREATE:
                    pattern = getSqlPattern([SqlMan.CREATE_TABLE, SqlMan.CREATE_INDEX, SqlMan.CREATE_VIEW, SqlMan.CREATE_SEQUENCE, SqlMan.CREATE_FUNCTION, SqlMan.CREATE_TABLESPACE, SqlMan.CREATE_USER])
                    break
                case SqlMan.CREATE_TABLE:
                    pattern = addOR(pattern, ['TABLE'], "CREATE\\s{1,2}.{0,20}\\s{0,2}{{1}}\\s{1,2}[^;]{1,20000}[;]{1}")
                    break
                case SqlMan.CREATE_INDEX:
                    pattern = addOR(pattern, ['INDEX'], "CREATE\\s{1,2}.{0,20}\\s{0,2}{{1}}\\s{1,2}[^;]{1,20000}[;]{1}")
                    break
                case SqlMan.CREATE_VIEW:
                    pattern = addOR(pattern, ['VIEW'], "CREATE\\s{1,2}.{0,20}\\s{0,2}{{1}}\\s{1,2}[^;]{1,50000}[;]{1}")
                    break
                case SqlMan.CREATE_SEQUENCE:
                    pattern = addOR(pattern, ['SEQUENCE'], "CREATE\\s{1,2}.{0,20}\\s{0,2}{{1}}\\s{1,2}[^;]{1,20000}[;]{1}")
                    break
                case SqlMan.CREATE_FUNCTION:
                    pattern = addOR(pattern, ['FUNCTION'], "CREATE\\s{1,2}.{0,20}\\s{0,2}{{1}}\\s{1,2}[^/]{1,10000}BEGIN\\s+[^/]{1,10000}[;]{1}\\s*[/]{1}")
                    break
                case SqlMan.CREATE_TABLESPACE:
                    pattern = addOR(pattern, ['TABLESPACE'], "CREATE\\s{1,2}.{0,20}\\s{0,2}{{1}}\\s{1,2}[^;]{1,50000}[;]{1}")
                    break
                case SqlMan.CREATE_USER:
                    pattern = addOR(pattern, ['USER'], "CREATE\\s{1,2}.{0,20}\\s{0,2}{{1}}\\s{1,2}[^;]{1,50000}[;]{1}")
                    break
                case SqlMan.INSERT:
                    pattern = addOR(pattern, ['INSERT'], "{{1}}\\s{1,2}.{0,20}\\s{0,2}\\s{1,2}[^;]{1,20000}[;]{1}")
                    break
                case SqlMan.UPDATE:
                    pattern = addOR(pattern, ['UPDATE'], "{{1}}\\s{1,2}.{0,20}\\s{0,2}\\s{1,2}[^;]{1,20000}[;]{1}")
                    break
                case SqlMan.ALTER:
                    pattern = addOR(pattern, ['ALTER'], "{{1}}\\s{1,2}.{0,20}\\s{0,2}\\s{1,2}[^;]{1,20000}[;]{1}")
                    break
                case SqlMan.COMMENT:
                    pattern = addOR(pattern, ['COMMENT'], "{{1}}\\s{1,2}.{0,20}\\s{0,2}\\s{1,2}[^;]{1,20000}[;]{1}")
                    break
                case SqlMan.GRANT:
                    pattern = addOR(pattern, ['GRANT'], "{{1}}\\s{1,2}.{0,20}\\s{0,2}\\s{1,2}[^;]{1,20000}[;]{1}")
                    break
                default:
                    break
            }
        }
        return pattern
    }


    SqlObject analCreate(SqlObject obj){
        List<String> words = obj.arrayToCompare
        String objectType = getObjectType(obj.query)

        words.eachWithIndex{ String word, int idx ->
            word = word.toUpperCase()
            if (!obj.objectTypeIdx && word.equalsIgnoreCase(objectType)){
                obj.objectTypeIdx = idx
                obj.objectNameIdx = obj.objectTypeIdx + 1

            }else if (word.equalsIgnoreCase("TABLESPACE")){
                if (words[idx -1].equalsIgnoreCase("TEMPORARY"))
                    obj.tempTablespaceNameIdx = idx +1
                else
                    obj.tablespaceNameIdx = idx +1

            }else if (word.equalsIgnoreCase("QUOTA")){
                (0..3).each{
                    int tempIdx = idx + it
                    if (words[tempIdx].equalsIgnoreCase("ON"))
                        obj.quotaNameIdxs << tempIdx +1
                }
            }else if (word.equalsIgnoreCase("FROM") || word.equalsIgnoreCase("JOIN")){
                int i = idx
                int stepi = 0
                int notPairCnt = 0
                int notPairQuote = 0
                String searchWord
                while(words[++i] != null){
                    searchWord = words[i]
                    notPairQuote += searchWord.count("'")
                    if (searchWord.equals("(") && notPairQuote % 2 == 0)
                        notPairCnt++
                    if (searchWord.equals(")") && notPairQuote % 2 == 0)
                        notPairCnt--
                    if (notPairCnt > 0 || notPairQuote % 2 != 0){
                        continue
                    }else if (notPairCnt < 0){
                        break
                    }else if (notPairCnt == 0){
                        stepi++
                        if (searchWord.equals(",")){
                            stepi = 0
                        }else if (stepi == 1){
                            obj.tableNameIdxs << i
                        }else if (stepi > 1 && searchWord.toUpperCase() in ["WHERE", "ORDER", "GROUP"])
                            break
                    }
                }
            }
            switch (objectType){
                case 'TABLE':
                    if (word.equalsIgnoreCase("REFERENCES"))
                        obj.tableNameIdxs << idx + 1
                    break
                case 'INDEX':
                    if (word.equalsIgnoreCase("ON") && words[idx-1].equalsIgnoreCase(words[obj.objectNameIdx]))
                        obj.tableNameIdxs << idx + 1
                    break
                case 'VIEW':
                    break
                case 'SEQUENCE':
                    break
                case 'FUNCTION':
                    break
                case 'TABLESPACE':
                    if (word.equalsIgnoreCase("DATAFILE"))
                        obj.datafileNameIdx = idx + 1
                    break
                case 'USER':
                    if (word.equalsIgnoreCase("IDENTIFIED"))
                        obj.passwordIdx = idx + 2
                    break
                default:
                    break
            }
        }
        switch (objectType){
            case 'TABLE':
                obj.tableNameIdxs << obj.objectNameIdx
                break
            case 'INDEX':
                obj.indexNameIdx = obj.objectNameIdx
                break
            case 'VIEW':
                obj.viewNameIdx = obj.objectNameIdx
                break
            case 'SEQUENCE':
                obj.sequenceNameIdx = obj.objectNameIdx
                break
            case 'FUNCTION':
                obj.functionNameIdx = obj.objectNameIdx
                break
            case 'TABLESPACE':
                obj.tablespaceNameIdx = obj.objectNameIdx
                break
            case 'USER':
                obj.userNameIdx = obj.objectNameIdx
                obj.schemeName = words[obj.objectNameIdx]
                break
            default:
                break
        }
        obj.with{
            setObjectType(objectType)
        }
        return analObjectName(obj)
    }

    SqlObject analAlter(SqlObject obj){
        List<String> words = obj.arrayToCompare
        obj.objectTypeIdx = 1
        obj.objectNameIdx = 2
        obj.objectType = words[obj.objectTypeIdx]

        words.eachWithIndex{ String word, int idx ->
            word = word.toUpperCase()
            if (word.equalsIgnoreCase(obj.objectType) && !obj.objectTypeIdx){
                obj.objectTypeIdx = idx
                obj.objectNameIdx = obj.objectTypeIdx + 1

            }else if (word.equalsIgnoreCase("TABLESPACE")){
                if (words[idx -1].equalsIgnoreCase("TEMPORARY"))
                    obj.tempTablespaceNameIdx = idx +1
                else
                    obj.tablespaceNameIdx = idx +1

            }else if (word.equalsIgnoreCase("QUOTA")){
                (0..2).each{
                    int tempIdx = idx + it
                    if (words[tempIdx].equalsIgnoreCase("ON"))
                        obj.quotaNameIdxs << tempIdx +1
                }
            }
            switch (obj.objectType){
                case 'TABLE':
                    break
                case 'INDEX':
                    if (word.equalsIgnoreCase("ON") && words[idx-1].equalsIgnoreCase(words[obj.objectNameIdx]))
                        obj.tableNameIdxs << idx + 1
                    break
                case 'VIEW':
                    break
                case 'SEQUENCE':
                    break
                case 'FUNCTION':
                    break
                case 'TABLESPACE':
                    if (word.equalsIgnoreCase("DATAFILE"))
                        obj.datafileNameIdx = idx + 1
                    break
                case 'USER':
                    if (word.equalsIgnoreCase("IDENTIFIED"))
                        obj.passwordIdx = idx + 2
                    break
                default:
                    break
            }
        }

        switch (obj.objectType){
            case 'TABLE':
                obj.tableNameIdxs << obj.objectNameIdx
                break
            case 'INDEX':
                obj.indexNameIdx = obj.objectNameIdx
                break
            case 'VIEW':
                obj.viewNameIdx = obj.objectNameIdx
                break
            case 'SEQUENCE':
                obj.sequenceNameIdx = obj.objectNameIdx
                break
            case 'FUNCTION':
                obj.functionNameIdx = obj.objectNameIdx
                break
            case 'TABLESPACE':
                obj.tablespaceNameIdx = obj.objectNameIdx
                break
            case 'USER':
                obj.userNameIdx = obj.objectNameIdx
                obj.schemeName = words[obj.objectNameIdx]
                break
            default:
                break
        }
        return analObjectName(obj)
    }

    SqlObject analInsert(SqlObject obj){
        List<String> words = obj.arrayToCompare
        obj.objectType = 'TABLE'
        words.eachWithIndex{ String word, int idx ->
            if (word.equalsIgnoreCase('INTO')){
                obj.objectNameIdx = idx + 1
                obj.tableNameIdxs << obj.objectNameIdx
            }else if (word.equalsIgnoreCase("FROM") || word.equalsIgnoreCase("JOIN")){
                int i = idx
                int stepi = 0
                int notPairCnt = 0
                int notPairQuote = 0
                String searchWord
                while(words[++i] != null){
                    searchWord = words[i]
                    notPairQuote += searchWord.count("'")
                    if (searchWord.equals("(") && notPairQuote % 2 == 0)
                        notPairCnt++
                    if (searchWord.equals(")") && notPairQuote % 2 == 0)
                        notPairCnt--
                    if (notPairCnt > 0 || notPairQuote % 2 != 0){
                        continue
                    }else if (notPairCnt < 0){
                        break
                    }else if (notPairCnt == 0){
                        stepi++
                        if (searchWord.equals(",")){
                            stepi = 0
                        }else if (stepi == 1){
                            obj.tableNameIdxs << i
                        }else if (stepi > 1 && searchWord.toUpperCase() in ["WHERE", "ORDER", "GROUP"])
                            break
                    }
                }
            }
        }
        return analObjectName(obj)
    }

    SqlObject analUpdate(SqlObject obj){
        List<String> words = obj.arrayToCompare
        obj.objectType = 'TABLE'
        obj.objectNameIdx = 1
        obj.tableNameIdxs << obj.objectNameIdx
        words.eachWithIndex{ String word, int idx ->
            if (word.equalsIgnoreCase("FROM") || word.equalsIgnoreCase("JOIN")){
                int i = idx
                int stepi = 0
                int notPairCnt = 0
                int notPairQuote = 0
                String searchWord
                while(words[++i] != null){
                    searchWord = words[i]
                    notPairQuote += searchWord.count("'")
                    if (searchWord.equals("(") && notPairQuote % 2 == 0)
                        notPairCnt++
                    if (searchWord.equals(")") && notPairQuote % 2 == 0)
                        notPairCnt--
                    if (notPairCnt > 0 || notPairQuote % 2 != 0){
                        continue
                    }else if (notPairCnt < 0){
                        break
                    }else if (notPairCnt == 0){
                        stepi++
                        if (searchWord.equals(",")){
                            stepi = 0
                        }else if (stepi == 1){
                            obj.tableNameIdxs << i
                        }else if (stepi > 1 && searchWord.toUpperCase() in ["WHERE", "ORDER", "GROUP"])
                            break
                    }
                }
            }
        }
        return analObjectName(obj)
    }

    SqlObject analComment(SqlObject obj){
        List<String> words = obj.arrayToCompare
        words.eachWithIndex{ String word, int idx ->
            if (word.equalsIgnoreCase('ON')){
                obj.objectTypeIdx = idx + 1
                obj.objectNameIdx = idx + 2
            }
        }
        String objectName = words[obj.objectNameIdx]
        String objectType = words[obj.objectTypeIdx]
        obj.objectType = objectType
        def array = objectName.split('[.]')
        if (objectType.equalsIgnoreCase("COLUMN")){
            if (array.size() == 3){
                obj.schemeName = array[0]
                obj.objectName = "${array[1]}.${array[2]}"
            }else if (array.size() == 2){
                obj.objectName = "${array[0]}.${array[1]}"
            }
        }else if (objectType.equalsIgnoreCase("TABLE")){
            if (array.size() == 2){
                obj.schemeName = array[0]
                obj.objectName = array[1]
            }else if (array.size() == 1){
                obj.objectName = array[0]
            }
        }
        return obj
    }

    SqlObject analGrant(SqlObject obj){
        List<String> words = obj.arrayToCompare
        obj.objectType = 'USER'
        words.eachWithIndex{ String word, int idx ->
            if (word.equalsIgnoreCase('TO')){
                obj.objectNameIdx = idx + 1
                obj.schemeName = words[obj.objectNameIdx]
            }
        }
        return analObjectName(obj)
    }

    SqlObject analObjectName(SqlObject obj){
        String objectName = obj.arrayToCompare[obj.objectNameIdx]
        if (objectName.indexOf('.') != -1){
            def array = objectName.split('[.]')
            obj.schemeName = array[0]
            obj.objectName = array[1]
        }else{
            obj.objectName = objectName
        }
        return obj
    }





    SqlObject getReplacedObject(SqlObject obj, def opt){
        // Replace Some With Some On Query
        def words = obj.arrayToCompare
        String target
        if (opt){
            if (opt.replace){
                opt.replace.each{ String before, String after ->
                    words.collect{ it.replaceAll(before, after) }
                }
            }
            if (obj.schemeName){
                target = obj.schemeName
                ifReturn(opt.replaceUser, target).each{ String replaceStr ->
                    if (obj.objectType.equalsIgnoreCase("USER") || obj.commandType.equalsIgnoreCase("GRANT")){
                        words[obj.objectNameIdx] = replaceStr
                        obj.objectName = replaceStr
                        obj.schemeName = replaceStr
                    }else{
                        words[obj.objectNameIdx] = "${replaceStr}.${obj.objectName}" as String
                        obj.schemeName = replaceStr
                    }
                }
            }
            if (obj.tablespaceNameIdx){
                target = words[obj.tablespaceNameIdx]
                ifReturn(opt.replaceTablespace, target).each{ String replaceStr ->
                    words[obj.tablespaceNameIdx] = replaceStr
                    if (obj.objectType.equalsIgnoreCase("TABLESPACE")){
                        obj.objectName = replaceStr
                        obj.tablespaceName = replaceStr
                    }else{
                        obj.tablespaceName = replaceStr
                    }
                }
            }
            if (obj.tempTablespaceNameIdx){
                target = words[obj.tempTablespaceNameIdx]
                ifReturn(opt.replaceTablespace, target).each{ String replaceStr ->
                    words[obj.tempTablespaceNameIdx] = replaceStr
                    obj.tempTablespaceName = replaceStr
                }
            }
            if (obj.quotaNameIdxs){
                obj.quotaNameIdxs.each{
                    target = words[it]
                    ifReturn(opt.replaceTablespace, target).each{ String replaceStr ->
                        words[it] = replaceStr
                        obj.quotaNames << replaceStr
                    }
                }
            }
            if (obj.datafileNameIdx){
                target = words[obj.datafileNameIdx]
                target = target.substring(1, target.length() -1)
                ifReturn(opt.replaceDatafile, target).each{ String replaceStr ->
                    words[obj.datafileNameIdx] = "'${replaceStr}'" as String
                    obj.datafileName = "'${replaceStr}'" as String
                }
            }
            if (obj.passwordIdx){
                target = words[obj.passwordIdx]
                target = target.substring(1, target.length() -1)
                ifReturn(opt.replacePassword, target).each{ String replaceStr ->
                    words[obj.passwordIdx] = "\"${replaceStr}\"" as String
                    obj.password = "\"${replaceStr}\"" as String
                }
            }
            if (obj.commandType.equalsIgnoreCase("COMMENT")){
                def array = obj.objectName.split('[.]')
                String tab = array[0]
                String replaceTab = tab
                ifReturn(opt.replaceTable, tab).each{ String replaceStr ->
                    replaceTab = replaceStr
                }
                if (obj.objectType.equalsIgnoreCase("COLUMN") && array.size() == 2){
                    replaceTab = "${replaceTab}.${array[1]}" as String
                }else if (obj.objectType.equalsIgnoreCase("TABLE")){
                    obj.tableNames << replaceTab
                }
                obj.objectName = replaceTab
                words[obj.objectNameIdx] = ((obj.schemeName) ? "${obj.schemeName}.${replaceTab}" : "${replaceTab}") as String
            }
            if (obj.tableNameIdxs && !obj.commandType.equalsIgnoreCase("COMMENT")){
                obj.tableNameIdxs.each{
                    target = words[it]
                    String replaceStr = getReplacedName(target, opt.replaceUser, opt.replaceTable)
                    obj.tableNames << replaceStr
                    words[it] = replaceStr
                }
                if (obj.objectType.equalsIgnoreCase("TABLE"))
                    obj.objectName = obj.tableNames[0]
            }
            if (obj.functionNameIdx){
                target = words[obj.functionNameIdx]
                obj.functionName = getReplacedName(target, opt.replaceUser, opt.replaceFunction)
                words[obj.functionNameIdx] = obj.functionName
                if (obj.objectType.equalsIgnoreCase("FUNCTION"))
                    obj.objectName = obj.functionName
            }
            if (obj.viewNameIdx){
                target = words[obj.viewNameIdx]
                obj.viewName = getReplacedName(target, opt.replaceUser, opt.replaceView)
                words[obj.viewNameIdx] = obj.viewName
                if (obj.objectType.equalsIgnoreCase("VIEW"))
                    obj.objectName = obj.viewName
            }
            if (obj.indexNameIdx){
                target = words[obj.indexNameIdx]
                obj.indexName = getReplacedName(target, opt.replaceUser, opt.replaceIndex)
                words[obj.indexNameIdx] = obj.indexName
                if (obj.objectType.equalsIgnoreCase("INDEX"))
                    obj.objectName = obj.indexName
            }
            if (obj.sequenceNameIdx){
                target = words[obj.sequenceNameIdx]
                obj.sequenceName = getReplacedName(target, opt.replaceUser, opt.replaceSequence)
                words[obj.sequenceNameIdx] = obj.sequenceName
                if (obj.objectType.equalsIgnoreCase("SEQUENCE"))
                    obj.objectName = obj.sequenceName
            }
        }
        obj.query = words.join(" ")
        return obj
    }


    String getReplacedName(String target, def replaceUser, def replaceObject){
        String sNm
        String oNm
        target.split('[.]').eachWithIndex{ String o, int i ->
            if (i==0){
                oNm = o
            }else if (i==1){
                sNm = oNm
                oNm = o
            }
        }
        String replaceSNm = sNm
        String replaceONm = oNm
        ifReturn(replaceUser, sNm).each{ String replaceStr ->
            replaceSNm = replaceStr
        }
        ifReturn(replaceObject, oNm).each{ String replaceStr ->
            replaceONm = replaceStr
        }
        return (replaceSNm) ? ("${replaceSNm}.${replaceONm}" as String) : replaceONm
    }


    def ifReturn(def replaceObj, String target){
        List result = []
        if (!replaceObj || !target){

        }else if (replaceObj instanceof String){
            result << (replaceObj as String)

        }else{
            replaceObj.findAll{
                (it.key as String).equalsIgnoreCase(target)
            }.each{
                result << (it.value as String)
            }
        }
        return result
    }

















    String removeAnnotation(query){
        return query.replaceAll(/\-\-.*[\r\n;]/, " ")
    }
    String removeNewLine(query){
        return query.replaceAll(/[\r\n]/, " ")
    }
    String removeLastSemicoln(query){
        return query.replaceAll(/[;]\s*$/, '')
    }
    String removeLastSlash(query){
        return query.replaceAll(/[\/]\s*$/, '')
    }


    String getReplaceNotInOracleQuote(String query, def replaceMap){
        Map<Long, String> map = [:]
        Map<Long, String> validReplaceMap
        // Get All Index
        Map<String, List> indexListMap = [:]
        replaceMap.each{ String replaceTarget, String replacement ->
            indexListMap[replaceTarget] = query.findIndexValues{ it == replaceTarget }.sort{ long a, long b -> b <=> a }
        }
        List singleQuoteIndexList = query.findIndexValues{ it == '\'' }
        // Get protectRangeList
        int i = -1
        int quoteCnt = 0
        int startQuoteIdx = -1
        int endQuoteIdx = -1
        List<Long> protectRangeList = []
        while(singleQuoteIndexList[++i] != null){
            quoteCnt++
            if (quoteCnt == 1){
                startQuoteIdx = singleQuoteIndexList[i]
            }else if (quoteCnt == 2){
                if (i+1 < singleQuoteIndexList.size() && Math.abs(singleQuoteIndexList[i] - singleQuoteIndexList[i+1]) == 1){
                }else{
                    endQuoteIdx = singleQuoteIndexList[i]
                    protectRangeList << startQuoteIdx
                    protectRangeList << endQuoteIdx
                    quoteCnt = 0
                    startQuoteIdx = -1
                    endQuoteIdx = -1
                }
            }else if (endQuoteIdx == -1 && quoteCnt == 3){
                quoteCnt = 1
            }
        }
        // Get validReplaceMap
        indexListMap.each{ String replaceTarget, List indexList ->
            indexList.each{ Long idx -> map[idx] = replaceTarget }
        }
        validReplaceMap = map.sort{ a, b -> b.key <=> a.key }.findAll{
            boolean isProtected = false
            for (int idx=0; idx<protectRangeList.size(); idx+=2){
                Long startRange = protectRangeList[idx]
                Long endRange = protectRangeList[idx+1]
                Long target = it.key as Long
                if (startRange <= target && target <= endRange){
                    isProtected = true
                    break
                }
            }
            return !isProtected
        }
        // Get Replaced String
        validReplaceMap.each{
            query = replaceIndexRange(query, it.key as int, replaceMap[it.value])
        }
        return query
    }

    String replaceIndexRange(String target, int index, String replacement){
        return replaceIndexRange(target, index, 1, replacement)
    }

    String replaceIndexRange(String target, int startIndex, int count, String replacement){
        return "${target.substring(0, startIndex)}${replacement}${target.substring(startIndex + count)}"
    }



}
