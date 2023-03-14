package com.jaemisseo.hoya.task

import groovy.json.JsonOutput
import jaemisseo.man.FileMan
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import jaemisseo.man.util.HierarchicalHashMap

@Task
@TerminalValueProtocol(['file', 'contents.raw'])
class FileWrite extends TaskHelper{

    @Value(name="file", filter='getFilePath')
    String filePath

    @Value(name="contents.raw")
    String contents_raw

    @Value(name="contents.yaml")
    HierarchicalHashMap contents_yaml

    @Value(name="contents.json")
    HierarchicalHashMap contents_json

    @Value(name="contents.properties")
    HierarchicalHashMap contents_properties

    @Value(name="contents.simplejson")
    HierarchicalHashMap contents_simplejson


    @Override
    Integer run(){

        //1. Save
        if (contents_raw != null)
            saveFile_raw(filePath, contents_raw)

        if (contents_properties != null)
            saveFile_properties(filePath, contents_properties)

        if (contents_yaml != null)
            saveFile_yaml(filePath, contents_yaml)

        if (contents_json != null)
            saveFile_json(filePath, contents_json)

        if (contents_simplejson != null)
            saveFile_simplejson(filePath, contents_simplejson)

        //Set 'answer' and 'value' Property
//        set('value', value)
//        setPropValue()

        return STATUS_TASK_DONE
    }



    private void saveFile_raw(String filePath, String data){
        //TODO: Implements..
        File file = new File(filePath)

        //- Save
        FileMan.write(file.path, data)
    }

    private void saveFile_properties(String filePath, HierarchicalHashMap data){
        File file = new File(filePath)
        FileWriter writer = new FileWriter(file);

        //- Convert
        Map<String, String> entriesToSave = data.collectEntries{ k, v -> [k, v.toString()] }

        //- Save
        Properties properties = new Properties()
        properties.putAll(entriesToSave)
        properties.store(writer, "task:fileWrite")
    }

    private void saveFile_yaml(String filePath, HierarchicalHashMap data){
        //TODO: Implements..
        File file = new File(filePath)
        FileWriter writer = new FileWriter(file);

        //- Save
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml()
        yaml.dump(data, writer)
    }

    private void saveFile_json(String filePath, HierarchicalHashMap data){
        //TODO: Implements..
        File file = new File(filePath)
        FileWriter writer = new FileWriter(file);

        //- Convert
        String json = JsonOutput.toJson(data)

        //- Save
        FileMan.write(file.path, json)
    }


    private void saveFile_simplejson(String filePath, HierarchicalHashMap data){
        //TODO: Implements..
        File file = new File(filePath)
        FileWriter writer = new FileWriter(file);

        //- Convert
        String simplejson = null //TODO: implements..

        //- Save
        FileMan.write(file.path, simplejson)
    }


}
