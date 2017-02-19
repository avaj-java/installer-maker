package com.jaemisseo.man.util

/**
 * Created by sujkim on 2017-02-19.
 */
class Option {

    Option merge(Option newOption){
        Option oldOption = this
        oldOption.each{ String fieldName ->
            oldOption[fieldName] = (newOption[fieldName] != null && newOption[fieldName] != '') ? newOption[fieldName] : oldOption[fieldName]
        }
        return this
    }

    List each(Closure closure){
        List list = properties.keySet().toList()
        list -= ['class']
        list.each{ String fieldName ->
            closure(fieldName)
        }
        return list
    }

}
