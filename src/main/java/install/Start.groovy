package install

import install.task.TaskInstallDatabase
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.SqlMan
import install.task.TaskTestJDBCConnection

class Start {

    /**
     * START INSTALL
     * @param args
     * @throws Exception
     */
    static void main(String[] args) throws Exception{
        // 1. [prop] Set Default Properties
        Map prop = [
                'prop.path': ['./', './conf', '../conf', '~/', '/'],
                'prop.filename': 'installer.properties'
        ]
        // 2. [prop] OverWrite with External Properties
        if (args){
            args.each{
                int indexEqualMark = it.indexOf('=');
                String beforeEqualMark
                def afterEqualMark
                if (indexEqualMark != -1){
                    //COMMAND: -PROP.PERTY=VALUE
                    //RESULT: prop['PROP.PERTY'] = VALUE
                    beforeEqualMark = (it.startsWith('-')) ? it.substring(1, indexEqualMark) : ''
                    afterEqualMark = it.substring(indexEqualMark + 1)
                    prop[beforeEqualMark] = (afterEqualMark) ?: ''
                }else{
                    //COMMAND: -PROP.PERTY
                    //RESULT: prop['PROP.PERTY'] = true
                    beforeEqualMark = (it.startsWith('-')) ? it.substring(1, it.length()) : ''
                    prop[beforeEqualMark] = true
                }
            }
        }
        // 3. Start With Properties
        new Start().start(prop)
    }





    Start(){}

    TaskInstallDatabase taskInstallDB



    /**
     * START
     * @param prop
     */
    void start(Map prop){
        ///// - Check External Task
        if (runOtherFunc(prop))
            System.exit(0)

        ///// - Create Main Bean
        SqlMan sqlman = new SqlMan()
        PropMan propman = getPropertiesFile(prop)

        ///// - Run Main Task (TaskInstallDatabase)
        taskInstallDB = new TaskInstallDatabase(sqlman, propman)
        taskInstallDB.run()
    }



    /**
     * Run External Function
     * @param prop
     */
    boolean runOtherFunc(def prop){
        if (prop['getPath']){
            println new PropMan().getFullPath(prop['getPath'])
            return true
        }
        if (prop['testdb'] as boolean){
            new TaskTestJDBCConnection().run(prop)
            return true
        }
        return false
    }



    /**
     *
     * @param prop
     * @return
     */
    PropMan getPropertiesFile(def prop){
        String thisPath = new File(this.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent()
        PropMan propman = new PropMan(thisPath)
        propman.getFile(prop['prop.path'], prop['prop.filename'], prop)
               .validate(['sql.user', 'sql.password'])
        return propman
    }





}

