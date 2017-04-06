package com.jaemisseo.man.util

/**
 * Created by sujkim on 2017-02-19.
 */
class FileSetup extends Option<FileSetup> {

    Boolean modeAutoLineBreak = true
    Boolean modeAutoMkdir = false
    Boolean modeAutoBackup = false
    Boolean modeAutoOverWrite = false

    String encoding = 'utf-8'
    String lineBreak = System.getProperty("line.seperator")
    String lastLineBreak = ''
    String backupPath

}
