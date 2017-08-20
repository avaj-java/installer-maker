[![Build Status](https://travis-ci.org/avaj-java/installer-maker.svg?branch=master)](https://travis-ci.org/avaj-java/installer-maker)
-----
# Developer Menual



## 1. Needs   
- OS: `WINDOWS`, `LINUX`, `UNIX`
- JDK: `JDK 1.6+`



## 2. DEPENDENCIES
- ojdbc6-11.2.0.3
- tibero-jdbc-5.0
- groovy-all-2.1.3
- commons-compress-1.11
- jersey-client-1.9.1
- poi-3.9



## 3. Build

1. Gradle Build
    - Make a ZIP file
        
        ```sh
        gradle clean distZip
        ```
    - Make a ZIP file and Update INSTALLER_HOME
        
        ```sh
        gradle clean deployLocal
        ```

2. Deploy to Your User        
    - ZIP file path
     
        ```sh
        {PROJECT_DIRECTORY}/build/distributions/installer-x.x.x.zip
        ```

3. Then, Make Your User read Menuals



## 4. How to install Installer

1. Unzip to where you want (installer-x.x.x.zip)
    - Windows
     
        ```
        c:\installer
        ```
    - Linux and Unix
    
        ```
        /home/dev/installer
        ```        

2. set Envirionment Variable, `INSTALLER_HOME`
    - Windows
    
        ```
        INSTALLER_HOME=c:\installer
        ```
    - Linux and Unix
    
        ```
        INSTALLER_HOME=/home/dev/installer
        ```        

3. add Envirionment Variable value, `path`
    - Windows
     
        ```
        path=c:\installer\bin;%path%
        ```
    - Linux and Unix
    
        ```
        path=/home/dev/installer/bin:$path
        ```
        
4. Check Installer Version
    
    ```
    installer -v
    ```



## 5. How to make Your Installer

1. You need 3 properties files to make the Your Installer. 
    - `builder.properties`
    - `receptionist.properties`
    - `installer.properties`


2. So, Command '`installer init`' on root path of Your Project Directory To Create 3 properties files   
    
    ```
    installer init
    ```

3. And then, Write your process into 3 properties files (ref. [properties.md](properties.md))    
    - `builder.properties` - Write how to make installer
        
        ```properties
        build.level=1,2
        
        build.level.1.task=COPY
        build.level.1.file.path=./build/libs/*.war
        build.level.1.dest.path=${build.installer.home}/data/war/

        build.level.2.task=ZIP
        build.level.2.file.path=${build.installer.home}
        build.level.2.dest.path=${build.dir}/${installer.name}.zip
        ..
        ```
    - `receptionist.properties` - Write question what ask your user for how to install
        
        ```properties
        ask.level=0,1,2,3
        
        ask.level.0.task=NOTICE
        ask.level.0.msg="Hi, ${user.name}. If you want to install MetaStream3? then, I Have some question."
        
        ask.level.1.task=Q-CHOICE
        ask.level.1.question=1. CHOOSE A INSTALL LEVEL
        ask.level.1.answer=1
        ask.level.1.answer.description.map={"1":"PROGRAM", "2":"PROGRAM + DATA", "3":"PROGRAM + USER, DATA", "4":"PROGRAM + TABLESPACE, USER, DATA"}
        ask.level.1.answer.value.map={"1":"f1-f5", "2":"3-4,f1-f5", "3":"2-4,f1-f5", "4":"1-4,f1-f5"}
        ask.level.1.property=install.level
        
        ask.level.2.task=Q
        ask.level.2.question=2. PROGRAM_HOME
        ask.level.2.answer=~/myprogram
        ask.level.2.property=program.home
        
        ask.level.3.task=Q
        ask.level.3.question=3. WAS_CONTEXT
        ask.level.3.answer=metastream
        ask.level.3.property=was.contextpath
        ..
        ```
    - `installer.properties` - Write how to install
        
        ```properties
        install.level=f1,f2
        
        install.level.f1.task=UNTAR
        install.level.f1.file.path=${data.dir}/tomcat/apache-tomcat-7.0.47.tar.gz
        install.level.f1.dest.path=${program.home}

        install.level.f2.task=UNJAR
        install.level.f2.file.path=${data.dir}/war/*.war
        install.level.f2.dest.path=${was.home}/webapps/${was.contextpath}
        ..
        ```

4. Build your installer for your project
    - build
    
        ```
        installer build
        ```
    - If `{YOUR_PROJECT_DIRECTORY}/build/installer_myproject` directory already exists
        
        ```
        installer clean build
        ```

5. And, deploy Your Installer to Your Users

    Installer Home is basically '`./build/installer_myproject`'

    
## 6. How to Install Your Program with Your Installer   

1. Go to INSTALLER home path you set 
    ```sh
    cd {YOUR_PROJECT_DIRECTORY}/build/installer_myproject    
    ```
    
2. Run install in bin Directory (Default bin Directory Path is `{BUILD_INSTALLER_HOME}/bin`)
    ```sh
    {YOUR_PROJECT_DIRECTORY}/build/installer_myproject/bin/install
    ```
    
3. Then, Your `Receptionist ask Your User` What user wants, And `It be installed by Your Installer`  
 

