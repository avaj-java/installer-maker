package com.jaemisseo.man.util

/**
 * Created by sujung on 2017-04-12.
 */
class UndoPropertiesObject {

    Map insertedMap
    Map updatedBeforeMap
    Map updatedAfterMap
    Map deletedMap

    UndoPropertiesObject gap(Map old, Map now){
        insertedMap = now.findAll{ !old.containsKey(it.key) }
        deletedMap = old.findAll{ !now.containsKey(it.key) }
        updatedBeforeMap = old.findAll{ now.containsKey(it.key) && now[it.key] != old[it.key]  }
        updatedAfterMap = now.findAll{ old.containsKey(it.key) && now[it.key] != old[it.key]  }
        return this
    }

}
