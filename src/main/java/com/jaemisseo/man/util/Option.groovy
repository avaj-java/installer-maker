package com.jaemisseo.man.util

/**
 * Created by sujkim on 2017-02-19.
 */
class Option<T> {

    T clone(){
        Option oldOption = this
        T clonedObject = this.class.newInstance().merge(oldOption)
        return clonedObject
    }

    T clone(Map andPutMap){
        T clonedObject = this.clone()
        (clonedObject as Option).put(andPutMap)
        return clonedObject
    }

    T put(Map filedValueMap){
        Option oldOption = this
        filedValueMap.each{ String filedNameToChange, def value ->
            oldOption[filedNameToChange] = value
        }
        return oldOption
    }

    T merge(Option newOption){
        Option oldOption = this
        oldOption.eachFieldName{ String fieldName ->
            oldOption[fieldName] = (newOption[fieldName] != null && newOption[fieldName] != '') ? newOption[fieldName] : oldOption[fieldName]
        }
        return this
    }

    List eachFieldName(Closure closure){
        List list = properties.keySet().toList()
        list -= ['class']
        list.each{ String fieldName ->
            closure(fieldName)
        }
        return list
    }

}
