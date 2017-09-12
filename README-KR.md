[![Build Status](https://travis-ci.org/avaj-java/installer-maker.svg?branch=master)](https://travis-ci.org/avaj-java/installer-maker)
[![All Download](https://img.shields.io/github/downloads/avaj-java/installer-maker/total.svg)](https://github.com/avaj-java/installer-maker/releases)
[![Release](https://img.shields.io/github/release/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)
[![License](https://img.shields.io/github/license/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)

-----
# 사용자 사용설명서


### 1. 사양   
- OS: `WINDOWS`, `LINUX`, `UNIX`
- JDK: `JDK 1.6+`

### 2. 설치

1. https://github.com/avaj-java/installer-maker/releases에서 `zip파일`을 다운로드

2. 원하는 폴더에 압축을 해제하시고 환경변수에 등록

    - 설치 on WINDOWS
        
        원하는 폴더에 압축을 해제하시고 환경변수에 등록해 주세요.     
    
    - 설치 on UNIX
    
        ```bash
        mkdir path/to/installer-maker
        ```
        
        ```bash
        cp installer-maker-xxx.zip path/to/installer-maker 
        ```
        
        ```bash
        unzip installer-maker-xxx.zip 
        ```
    
        ```bash
        INSTALLER_MAKER_HOME=path/to/installer-maker
        path=$INSTALLER_MAKER_HOME/bin:$path
        ```

3. 버전확인
 
    ```bash
    installer-maker -v
    ```



### 3. 스크립트파일 생성
    
1. 프로젝트 루트에 다음 3개의 properties파일이 필요합니다.

    - builder.properties
    - receptionist.properties
    - installer.properties

2. 최초에 스크립트파일을 생성하려면

    ```bash
    installer-maker init
    ```

3. 3개의 Job과 여러 Task로 스크립트를 작성합니다.      
    
    - [sample here]() 
 
 
### 4. 빌드

1. 프로젝트 루트에서 3개의 스크립트를 바탕으르 빌드합니다.

    ```bash
    installer-maker clean build
    ```

2. 빌드에 성공하면 다음 3개의 폴더가 생성됩니다.

    - build/installer-temp
        
        `설치자`가 생성되면서 생기는 임시 폴더

    - build/installer-dist
    
       배포할 압축파일이 생성되는 폴더
        
    - build/installer-myproject or 설정한 폴더명
    
       bin/install을 실행하여 미리 `설치자`를 실행해 볼 수 있습니다.

                      
                      
### 5. 테스트

- 미리 만들어둔 rsp파일을 이용하여 test를 할 수 있습니다. CI를 이용하여 설치 테스트를 할 수도 있습니다.

    ```bash
    installer-maker test -response.file.path=path/to/rsp/file
    ```

        
# 개념                                                                                                
1. 커맨드(Command): 사용자가 Command Line에서 실행할 수 있는 명령어 입니다. 
    init
    clean
    build
    run
    test
    macgyver
    help
      
2. 잡(Job): 인스톨은 총 3개의 잡으로 행해지며, 여러 Task를 수행하는 객체입니다.
    builder
    receptionist
    installer

3. 타스크(Task): 직접 일을 수행하는 단위입니다. Job또는 Command Line을 통해서 수행합니다. 
    copy
    decrypt
    encrypt
    exec
    help
    jar
    mergeproperties
    mkdir
    notice
    question
    questionchoice
    questionfindfile
    questionyn
    replace
    set
    sql
    system
    tar
    testemail
    testjdbc
    testport
    testrest
    testsocket
    unjar
    untar
    unzip
    version
    zip    



-----
# 개발자 사용설명서



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
 

