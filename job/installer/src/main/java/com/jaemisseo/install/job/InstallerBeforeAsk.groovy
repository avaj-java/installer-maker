package com.jaemisseo.install.job

import com.jaemisseo.hoya.job.JobHelper
import jaemisseo.man.PropMan
import jaemisseo.man.configuration.context.CommanderConfig
import jaemisseo.man.configuration.context.Environment
import jaemisseo.man.configuration.context.SelfAware
import jaemisseo.man.configuration.data.PropertyProvider

/**
 * Created by sujkim on 2017-02-17.
 */
public class InstallerBeforeAsk extends JobHelper {

    InstallerBeforeAsk(PropMan propman, CommanderConfig config, PropertyProvider provider, Environment environment, SelfAware selfAware){
        this.propman = propman
        this.config = config
        this.provider = provider
        this.environment = environment
        this.selfAware = selfAware
    }


    public boolean checkResponseFile(String responseFilePath){
        //Try To Load Response File
        if (responseFilePath){
            if (new File(responseFilePath).exists()){
                return true
            }else{
                logger.error " < Failed > Load Response File, Does not exists file - ${responseFilePath}"
                System.exit(0)
            }
        }
        return false
    }

    /*************************
     * Read Remeber File
     *************************/
    public void readRememberAnswer(def gOpt){
        Boolean modeRemember = gOpt.modeRemember
        String rememberFilePath = gOpt.rememberFilePath

        if (modeRemember){
            logTaskDescription('load remembered your answer')
            try{
                PropMan rememberAnswerPropman = new PropMan().readFile(rememberFilePath).properties
                propman.merge(rememberAnswerPropman)
            }catch(Exception e){
                logger.error "No Remember File!!!"
            }
        }
    }




}
