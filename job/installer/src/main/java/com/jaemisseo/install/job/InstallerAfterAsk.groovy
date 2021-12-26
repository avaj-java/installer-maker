package com.jaemisseo.install.job

import com.jaemisseo.hoya.bean.FileSetup
import com.jaemisseo.hoya.job.JobHelper
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.configuration.context.CommanderConfig
import jaemisseo.man.configuration.context.Environment
import jaemisseo.man.configuration.context.SelfAware
import jaemisseo.man.configuration.data.PropertyProvider

/**
 * Created by sujkim on 2017-02-17.
 */
public class InstallerAfterAsk extends JobHelper {

    InstallerAfterAsk(PropMan propman, CommanderConfig config, PropertyProvider provider, Environment environment, SelfAware selfAware){
        this.propman = propman
        this.config = config
        this.provider = provider
        this.environment = environment
        this.selfAware = selfAware
    }


    /*************************
     * Backup & Write Remeber File
     *************************/
    public void writeRememberAnswer(def gOpt){
        Boolean modeRemember = gOpt.modeRemember
        String rememberFilePath = gOpt.rememberFilePath
        FileSetup fileSetup = gOpt.rememberFileSetup

        if (modeRemember){
            logTaskDescription('save your answer')
            FileMan fileman = new FileMan(rememberFilePath).set(fileSetup)
            try{
                if (fileman.exists())
                    fileman.backup()
            }catch(Exception e){
                e.printStackTrace()
            }
            try{
                fileman.read(rememberAnswerLineList).write()
            }catch(Exception e){
                e.printStackTrace()
            }
        }
    }


}
