# 1. INSTALLER-MAKER  

인스톨러메이커는 설치프로그램을 만드는 도구입니다.

## 1-1. INSTALLER-MAKER 설치하기

필수 설치환경은 `JAVA 1.6+`이며,  [![Release](https://img.shields.io/github/release/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)를 클릭하여 `zip파일`을 다운로드하고 원하는 폴더에 `압축을 해제` 후 `환경변수`에 등록하면 설치가 완료됩니다.
   
### 1-1-1. Installer-Maker 설치하기 on Windows
                    
1. 원하는 폴더에 압축해제
    ```
    C:\installer-maker        
    ```                

2. 환경변수 설정
    - `INSTALLER_MAKER_HOME`    
        ```
        C:\installer-maker       
        ```
    - `path`에 추가    
        ```
        %INSTALLER_MAKER_HOME%\bin       
        ```

    
### 1-1-2. Installer-Maker 설치하기 on UNIX
    
1. 원하는 폴더에 압축해제
    ```bash
    /path/to/installer-maker
    ```           
    
2. 환경변수 설정
    - `INSTALLER_MAKER_HOME` 
        ```bash
        INSTALLER_MAKER_HOME=/path/to/installer-maker
        ```
    - `path`에 추가    
        ```bash
        path=$INSTALLER_MAKER_HOME/bin:$path
        ```

## 1-2. 버전확인 

```bash
installer-maker -v
``` 

버전이 표시되면 설치가 완료된 것입니다.


## 1-3. INSTALLER-MAKER 시작하기

INSTALLER를 만들기 위해서는 3가지 명령을 정의해야한다. 
- installer-maker.yml에 `build` 
- installer.yml에 `ask`, `install`

명령을 정의하기 위해서는 스크립트파일을 생성하고 규칙에 따라 정의해야한다.

```yaml
installer-maker init
```

우선, init명령으로 프로젝트의 루트에 스트립트파일(installer-maker.yml, installer.yml)을 생성하고 

```yaml
installer-maker clean build
```

1. `clean`, `build` 명령을 내리면, installer-maker.yml의 build명령에 정의된 대로 `Installer`를 생성하게 되고 
2. 생성된 Installer의 install파일을 실행하면 installer.yml의 `ask`, `install`명령에 정의된 작업들이 순차적으로 진행된다. 


또한 `installer-maker.yml`에서는 build, ask, install뿐만 아니라 `다른 명령을 정의하여 사용할 수도 있다.`
(init, clean, build, ask, install, run, test, form 처럼 특수하게 사용되는 명령은 피한다.)

그럼, 다음 장에서 기본구조를 익히고 명령을 만들어 실행해보자.



# 2. 스크립트

## 2-1. 스크립트 기본구조 파악하기

```yaml
COMMAND_NAME:
  TASK_NAME:
    task: TASK_TYPE
    property1: VALUE
    property2: VALUE
```

1. 첫번째로 명령어명(CommandName)을 정의한다. 
2. 두번째로 작업명(TaskName)을 정의한다. 
3. 그다음 `task`속성에 원하는 Task를 적고, 그에 따른 `속성`과 `값`들을 적어 넣는다.

이렇게 작성된 스크립트는 명령어를 통해 CommandLine에서 실행할 수 있다. 쉽게 예를 들어보겠다.
 
1. installer-maker.yml 스크립트 작성
    ```yaml
    hello:
      greetings:
        task: notice
        msg: Hello? nice to meet to you!
    ```    
    
2. Ccommand Line에서 실행
    ```bash
    installer-maker hello
    ```

3. 결과
    ```yaml
    Hello? nice to meet to you!
    ```

installer-maker.yml이란 파일을 만들고, hello명령(Command)에 greetings이라는 작업(Task)을 만들었다.
그 다음 CommandLine상에서 hello 명령을 실행하면 명령의 작업들이 순차적으로 실행된다.

## 2-2. 순차 작업 

```yaml
COMMAND_NAME:
  TASK_NAME:
    task: TASK_TYPE
    property1: VALUE
    property2: VALUE
        
  TASK_NAME2:
    task: TASK_TYPE
    property1: VALUE
    property2: VALUE
 
  TASK_NAME3:
    task: TASK_TYPE
    property1: VALUE
    property2: VALUE
```

하나의 명령은 여러 작업을 순차적으로 정의할 수 있다. 여러 작업들을 순차적으로 실행해보자.

1. installer-maker.yml 스크립트 작성
    ```yaml
    hello:
      greetings:
        task: notice
        msg: Hello? nice to meet to you!    
     
      who-are-you:  
        task: question
        desc: What is your name? 
     
      greeting-again:
        task: notice
        msg: Hello? ${hello.whoAreYou.answer}.
    ```

2. Ccommand Line에서 실행
    ```bash
    installer-maker hello
    ```

3. 결과
    ```yaml
    Hello? nice to meet to you!
    What is your name?
    > hoya
    Hello? hoya.  
    ```

- `순차적으로 작업`들을 정의하여 명령을 만들 수 있으며 
- who-are-you작업처럼 `질문형 작업`으로 사용자의 답을 얻을 수 있다.
- `${}`를 이용하여 변수로 활용할 수 있다.

## 2-3. 다중 명령

```yaml
COMMAND_NAME:
  TASK_NAME:
    task: TASK_TYPE
    property1: VALUE
    property2: VALUE
        
  TASK_NAME2:
    task: TASK_TYPE
    property1: VALUE
    property2: VALUE

COMMAND_NAME2:
  TASK_NAME:
    task: TASK_TYPE
    property1: VALUE
    property2: VALUE
        
  TASK_NAME2:
    task: TASK_TYPE
    property1: VALUE
    property2: VALUE
```

1개의 스크립트파일 안에 여러 명령을 정의할 수 있다.

1. installer-maker.yml 스크립트 작성
    ```yaml
    hello:
      greetings:
        task: notice
        msg: Hello?     
     
      greetings-again:
        task: notice
        msg: Hey! Hello? 
     
    introduce:
      myname:
        task: notice
        msg: My Name is Kim. 
    ```

2. Ccommand Line에서 실행
    ```bash
    installer-maker hello introduce
    ```

3. 결과
    ```yaml
    Hello?
    Hey! Hello?    
    My Name is Kim.  
    ```

## 2-4. 변수 정의

```yaml
VARIABLE: VALUE

COMMAND_NAME:
  TASK_NAME:
    task: TASK_TYPE
    property1: VALUE
    property2: VALUE
        
  TASK_NAME2:
    task: TASK_TYPE
    property1: VALUE
    property2: VALUE
```

명령 뿐만아니라 `변수를 정의할 수도 있다.` 

1. installer-maker.yml 스크립트 작성
    ```yaml
    # Define Variables
    myname: Kim
    yourname: Lee
    how.are.you: so.. How are you today?
    favorite:
      color: yellow 
      food: Kimchi 

    # Define Commands     
    introduce:
      myname:
        task: notice
        msg: My Name is ${myname}. your name is ${yourname}. ${how.are.you}. Do you know that? I like ${favorite.food}. 
    ```

2. Ccommand Line에서 실행
    ```bash
    installer-maker hello introduce
    ```

3. 결과
    ```yaml        
    My Name is Kim. your name is Lee. so.. How are you today? Do you know that? I like Kimchi  
    ```

## 2-5. 공통 속성

### 2-5-1. 설명(DESC)

- desc
    
    간단한 설명을 작성할 수 있다.
    
    ```yaml
    introduce:
      myname:
        desc: test task 
        task: notice
        msg: My Name is Kim.
    ```
    

### 2-5-2. 조건(IF)
모든 작업은 `if`, `ifoption`, `ifport`, `iffile`, `ifproperty`의 조건을 설정할 수 있다. 조건에 맞지 않으면 다음 작업을 수행하게 된다.

- if

    특정 속성의 값이 조건에 맞는지 여부에 따라 해당 작업(Task)을 수행한다. 
    
    JSON문법으로 몇가지 유연한 조건설정을 할 수 있다.
    
    - favorite.food의 값이 kimchi이고 favorite.color의 값이 yellow일 때     
        ```yaml
        introduce:
          myname:
            if: '{"favorite.food":"kimchi", "favorite.color":"yellow"}'
            task: notice
            msg: My Name is Kim.
        ```
        
    - favorite.food이 kimchi또는 sushi이고 favorite.color이 yellow일 때    
        ```yaml
        introduce:
          myname:
            if: '{"favorite.food":["kimchi", "sushi"], "favorite.color":"yellow"}'
            task: notice
            msg: My Name is Kim.
        ```
        
    - favorite.food이 kimchi 또는 sushi이고 favorite.color이 yellow이거나 myname이 Kim일 때     
        ```yaml
        introduce:
          myname:
            if: '[{"favorite.food":["kimchi", "sushi"], "favorite.color":"yellow"}, {"myname":"Kim"}]'
            task: notice
            msg: My Name is Kim.
        ```

- ifoption

    ```yaml
    installer-maker introduce --fire --wind
    ```

    Command Line에서 특정 옵션을 입력했는지(true) 하지 않았는지(false) 조건에 따라 해당 작업(Task)을 수행한다.     
    
    ```yaml
    introduce:
      myname:
        ifoption: '{"fire":true, "wind":true}'
        task: notice
        msg: My Name is Kim.
    ```
    or
    
    조건이 true만을 확인할 때는 yaml의 `-`문법으로 대체할 수 있다. 
    
    ```yaml
    introduce:
      myname:
        ifoption: 
          - fire 
          - wind
        task: notice
        msg: My Name is Kim.
    ```
    
- ifport

    특정 포트가 사용중인지(true) 아닌지(false) 조건에 따라 해당 작업(Task)을 수행한다.   
    
    ```yaml
    introduce:
      myname:
        ifport: '{"80":true, "8080":true}'
        task: notice
        msg: My Name is Kim.
    ```
    or
    ```yaml
    introduce:
      myname:
        ifport: 
          - 80
          - 8080
        task: notice
        msg: My Name is Kim.
    ```

- iffile
    
    특정 파일이 존재하는지(true) 존재하지 않는지(false) 조건에 따라 해당 작업(Task)을 수행한다.

    ```yaml
    introduce:
      myname:
        iffile: '{"./package.json":true, "./READEME.md":true}'
        task: notice
        msg: My Name is Kim.
    ```
    or
    ```yaml
    introduce:
      myname:
        iffile: 
          - ./package.json
          - ./READEME.md
        task: notice
        msg: My Name is Kim.
    ```
    
- ifproperty

    특정 속성이 정의되었는지(true) 정의되지 않았는지(false) 조건에 따라 해당 작업(Task)을 수행한다.
    
    ```yaml
    favorite:
      food: orange
      color: yellow  
  
    introduce:
      myname:
        ifproperty: '{"favorite.food":true, "favorite.color":true}'
        task: notice
        msg: My Name is Kim.
    ```
    or
    ```yaml
    introduce:
      myname:
        ifproperty: 
          - favorite.food
          - favorite.color
        task: notice
        msg: My Name is Kim.
    ```
   
## 2-5-3. 변수기호 
- variable.sign
    
    변수기호의 기본값은 `$`이다. `${}`를 그대로 출력하고 싶은 경우에 변수기호를 변경할 수 있다. 
    
    ```yaml
    favorite.color: orange 
  
    introduce:
      myname:
        desc: hello ${}               
        task: notice
        variable.sign: '@'
        msg: ${favorite.color} is not a variable expression. and... my favorite color is @{favorite.color}
    ```

## 2-5-4. 기타
- mode.only.interactive: true

    build할 때 생기는 rsp파일에 적용하지 않는다. 

- mode.variable.question.before.command: true

    해당 명령(Command)가 실행되기 전에, 변수에 값을 넣을 수 있는 질문을 어떤 작업보다 먼저 한다.

- mode.variable.question.before.task: true

    해당 작업(Task)가 실행되기 전에, 변수에 값을 넣을 수 있는 질문을 한다. 


# 3. 명령 - Command

명령어는 스크립트를 통해 만들 수 있으나 `기본적으로 예약된 명령어`들이 있다.

# 3-1. 기본 명령

- init

    ```yaml
    installer-maker init
    ```

    `init`명령은 스크립트파일 샘플을 만들어 준다.
    
    ```yaml
    installer-maker init --default
    ```
    
    또는 `--default` 옵션을 추가하면 기본속성파일이 생성된다. 기본속성값을 열람 및 수정할 수 있으며, 해당 위치에서 명령을 실행하면 수정된 속성을 적용할 수 있다. 
    
    - installer-maker.default.yml
    - installer.default.yml
    
    

- clean

    ```yaml
    installer-maker clean
    ```
    
    기존의 빌드폴더를 지운다.

- build
    
    ```yaml
    installer-maker clean build
    ```
        
    installer-maker.yml 파일에 정의된 build 명령의 작업들을 수행하여 정해진 빌드폴더에 `인스톨러를 생성`한다. 

- ask

    installer.yml 파일에 정의된 ask 명령의 작업들을 수행합니다. (생성된 인스톨러에서 자동 실행된다.)

- install

    installer.yml 파일에 정의된 install 명령의 작업들을 수행합니다. (생성된 인스톨러에서 자동 실행된다.)

- test

    build, ask, install 명령을 차례로 수행합니다.        
    `-rsp=파일명` 옵션을 같이 사용하면 미리 준비한 응답지로 질문형 작업을 통과시킬 수 있다.
    
    ```yaml
    installer-maker test -rsp=./test.rsp
    ```

- help
     
    ```yaml
    installer-maker help
    ```
    
    사용가능한 명령(command)과 작업(Task) 리스트를 열람할 수 있다.
    
    ```yaml
    installer-maker build --help
    ```
    
    하나의 명령(Command) 상세를 볼 수 있다.
    
    ```yaml
    installer-maker -notice --help
    ```
    
    하나의 작업(Task) 상세를 볼 수 있다.


# 4. 작업 - Task

## 4-1 명령 및 작업명 도움말


## 4-2 단일 Task 실행





# ※ 예제





# ※ 개발자
## 1. 작업 개발하기      