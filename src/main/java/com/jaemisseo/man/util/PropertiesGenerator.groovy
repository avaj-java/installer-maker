package com.jaemisseo.man.util

/**
 * Created by sujkim on 2017-03-29.
 */
class PropertiesGenerator {



    /**
     * Parameters from Terminal(Command Line) Option
     * @param args
     * @return
     */
    Map genValueListMap(String[] args){
        Map valueListMap = [:]
        //Generate Value Listing Map (Key starts with '-')
        if (args){
            String nowKey = ''

            args.each{
                it = it.replaceAll('\\^\\*', '\\*')         // Command Line(GitBash) Asterik Issue..  no use *, use ^*
                //COMMAND1: -PROPERTY.KEY.NAME=VALUE
                //RESULT2: valueListMap['PROPERTY.KEY.NAME'] = VALUE
                if (it.startsWith('-')){
                    int indexEqualMark = it.indexOf('=')
                    if (indexEqualMark != -1){
                        String beforeEqualMark = (it.startsWith('-')) ? it.substring(1, indexEqualMark) : ''
                        def afterEqualMark = it.substring(indexEqualMark + 1)
                        valueListMap[beforeEqualMark] = afterEqualMark ?: ''
                        nowKey = ''
                    }else {
                        nowKey = it.substring(1, it.length())
                        if (!valueListMap[nowKey])
                            valueListMap[nowKey] = valueListMap[nowKey] ?: []
                    }

                //COMMAND2: -PROPERTY.KEY.NAME VALUE1 VALUE2 ...
                //RESULT2: valueListMap['PROPERTY.KEY.NAME'] = [VALUE1, VALUE2, ...]
                }else{
                    if (!valueListMap[nowKey])
                        valueListMap[nowKey] = []
                    valueListMap[nowKey] << it
                }
            }

        }
        return valueListMap
    }

}
