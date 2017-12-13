# JOB 
   
JOB은 여러 TASK들을 일괄적으로 수행하는 단위입니다. Command에 의해서 특정 Job이 실행됩니다.  
`build`을 명령하면, `builder`가,  `ask`을 명령하면, `receptionist`가, `install`을 명령하면, `installer`가 명령을 받아서 일련의 일들을 시작합니다.
하지만, 최종사용자의 편의를 위해 `ask`와 `install`은 자동화스크립트 파일을 자동으로 만들어지며, 쉽게 실행할 수 있도록 제공됩니다.

## Command 

Command는 Windows 또는 UNIX 환경의 Terminal에서 사용할 수 있습니다. 모든 작업은 당신의 명령에서 시작됩니다. Command에 의해 `특정 Job이 일을 수행하도록 합니다.`  


## Job 일람 
 
 
### 1. ***Builder***
    
**Command**|**JOB의 내용**
---|---
init        | `builder`, `receptionist`, `installer` 샘플 스크립트 파일을 생성합니다. 
clean       | `builder.yml`을 참조하여 사전에 Installer구성작업을 진행했던 `build폴더를 지웁니다.`
build       | `builder.yml`을 참조하여 Installer구성작업을 진행할 `TASK들을 수행합니다.`
run         | `builder.yml`을 참조하여 Installer구성작업이 완료된 `build폴더`에서 `install`파일을 실행합니다.  

1. ***INIT***
    
    - 3개의 샘플 스크립트 파일을 생성합니다. 하지만, 파일이 존재하면 생성하지 않습니다. 
    - 프로젝트 소스의 최상위경로(ROOT)에서 사용합니다.   
    - 속성
    
        **파일명**|**설명**
        ---|---
        builder.yml      | 만들어낼 설치자(인스톨러)를 구성시키는 작업들을 적는 스크립트입니다. 
        receptionist.yml | 설치 사용자에게 질문할 내용들을 적는 스크립트입니다. 
        installer.yml    | 사용자로부터 질문 받은 내용들로 시행하는 설치작업들을 적는 스크립트입니다. 

    - 예)
    
        ```
        installer-maker init
        ```

2. ***CLEAN***

    - 기존에 `BUILD`작업이 이루어졌던 구성파일들을 지웁니다. 
    - 프로젝트 소스의 최상위경로(ROOT)에서 사용합니다.   
    - 예)

        ```
        installer-maker clean
        ```
        
3. ***BUILD*** 

    - `builder`스크립트를 이용하여 인스톨러를 구성합니다.
    - `receptionist`, `installer`스크립트 또한 필요합니다. 
    - 프로젝트 소스의 최상위경로(ROOT)에서 사용합니다.  
    - 속성
    
        **속성** | **기본값** | **설명** 
        ---|---|---
        build.dir                           | ./build                        | 설치자 구성작업이 이루어지는 가장 상위경로          
        build.temp.dir                      | ${build.dir}/installer_temp    | 설치자 구성작업이 임시로 이러우지는 경로 
        build.dist.dir                      | ${build.dir}/installer_dist    | 배포가능하도록 압축된 파일이 위치되는 경로 
        build.installer.home                | ${build.dir}/${installer.name} | 빌드된 설치자가 위치되는 폴더      
        installer.name                      | installer_myproject            | 설치자 이름 
        installer.home.to.lib.relpath       | ./lib                          | 필수 라이브러리파일 경로 
        installer.home.to.bin.relpath       | ./bin                          | 필수 바이너리파일 경로
        installer.home.to.rsp.relpath       | ./rsp                          | 문답파일 경로
        mode.auto.rsp                       | true                           | 인스톨러 문답시트 생성 여부
        mode.auto.zip                       | true                           | 인스톨러 ZIP압축파일 생성 여부
        mode.auto.tar                       | false                          | 인스톨러 TAR압축파일 생성 여부
        properties.dir                      | ./                             | 스크립트파일 위치

    - 예)
        
        ```bash
        installer-maker build
        ```

4. ***RUN***

    - 이미 구성(`BUILD`)된 설치자(`Installer`)를 실행합니다.
    - 이 명령의 직접 실행할 필요가 없습니다.          
    - 속성
    
        **속성**|**설명** 
        ---|---
        response.file.path  | 준비된 문답파일(.rsp)을 이용하여 자동으로 질문과정을 통과시킬 수 있습니다.
        
    - 예
    
        ```bash
        installer-maker run -response.file.path=./installer-data/test.rsp
        ```

### 2. ***Receptionist***

**Command**|**JOB의 내용** 
---|---
ask         | `receptionist.yml`을 참조하여 질문합니다. (구성된 Installer의 자동화스크립트를 통해 실행되기 때문에 직접적으로 명령을 사용할 일은 없습니다.)  
form        | `receptionist.yml`을 참조하여 문답파일(.rsp)을 만듭니다.

1. ***ASK***

    - `ask`명령은 직접 실행할 필요가 없습니다. 구성된 Installer의 `install파일을 실행`하면, 작성했던 `receptionist.yml`을 참조하여 사용자에게 `질문TASK`들을 수행하며, `Installer`에게 전달해줍니다.          
        
2. ***FORM***

    - `form`명령은 직접 실행할 필요가 없습니다. build과정에서 `mode.auto.rsp`옵션 여부에 따라서 자동 생성됩니다.
    - `receptionist.yml`을 참조하여 `.rsp`파일을 생성합니다.
               


### 3. ***Installer*** 

**Command**|**JOB의 내용** 
---|---
install     | `installer.yml`을 참조하여 설치합니다. (구성된 Installer의 자동화스크립트를 통해 실행되기 때문에 직접적으로 명령을 사용할 없습니다.) 

1. ***INSTALL***

    - `install`명령은 직접 실행할 필요가 없습니다. 구성된 Installer의 `install파일을 실행`하면, 먼저 Receptionist가 사용자로부터 질문을 하고, Installer는 `받은 문답`들과 작성했던 `installer.yml`을 참조하여 `TASK들을 수행`합니다.



### 4. ***Hoya***         

**Command**|**JOB의 내용** 
---|---
test        | Builder에게 `clean`, `build`를 명령하고, `구성된 Installer를 실행`시킵니다.
hoya    | `TAR(.tar) File Path` on Your File System
help        | 터미널에서 Command와 Task를 목록을 일람
   

1. ***TEST*** 
        
    - TEST는 다음 명령을 차례대로 실행합니다. `CLEAN`, `BUILD`, `RUN`
    - `response.file.path`옵션으로 미리준비한 문답파일을 지정하면, `TEST환경` 또는 `CI환경`에서 설치를 미리 시험해 볼 수 있습니다. 
    - 속성
    
        **속성**|**설명** 
        ---|---
        response.file.path  | 준비된 문답파일을 이용하여 자동으로 질문과정을 통과시킬 수 있습니다.
        
    - 예)
            
        ```
        installer-maker test -response.file.path=./installer-data/test.rsp
        ```  
    
2. ***HOYA***

    - 호야를 이용하면, 하나의 작업(TASK)을 단독으로 실행할 수 있습니다.
    - 예)
        
        ```
        installer-maker hoya
        ```

3. ***HELP*** 

    - 명령어 목록을 확인 할 수 있습니다.
    - 예)
    
        ```
        installer-maker help    
        ```
