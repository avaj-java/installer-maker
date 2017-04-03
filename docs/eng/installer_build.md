-----
# 2. How to make Your Installer

1. You need 3 properties files to make the Your Installer. 
    - `builder.properties`
    - `receptionist.properties`
    - `installer.properties`


2. So, Command '`installer init`' on root path of Your Project Directory To Create 3 properties files   
    
    ```
    installer init
    ```

3. And then, Write your process into 3 properties files (ref. [properties.md](properties.md))    
    - `builder.properties`
           
        Write how to make installer
        
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
    - `receptionist.properties`
    
        Write question what ask your user for how to install
        
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
    - `installer.properties`
    
        Write how to install
        
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


##### NEXT: [3. How to Install Your Program with Your Installer](installer_build_install.md)
