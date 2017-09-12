# TASK

I'm Sorry, My English is Not Good. But I try to write down `How to Use Installer`. 


## Task to Manage File
- ***TAR***

    **PROPERTY**|**VALUE**
    --|--
    file.path| TAR로 압축하고 싶은 `파일 경로` 
    dest.path| 생성될 TAR파일의 경로

- ***ZIP***

    **PROPERTY**|**VALUE** 
    --|--
    file.path| ZIP로 압축하고 싶은 `파일 경로` 
    dest.path| 생성될 ZIP파일의 경로

- ***JAR*** 

    **PROPERTY**|**VALUE** 
    --|--
    file.path| JAR로 압축하고 싶은 `파일 경로` 
    dest.path| 생성될 JAR파일의 경로

- ***UNTAR***

    **PROPERTY**|**VALUE** 
    --|--
    file.path| 압축해제할 TAR파일의 경로 
    dest.path| 압축해제된 TAR파일의 내용물들이 위치할 경로
    
- ***UNZIP*** 

    **PROPERTY**|**VALUE** 
    --|--
    file.path| 압축해제할 ZIP파일의 경로 
    dest.path| 압축해제된 ZIP파일의 내용물들이 위치할 경로
    
- ***UNJAR***

    **PROPERTY**|**VALUE** 
    --|--
    file.path| 압축해제할 JAR파일의 경로 
    dest.path| 압축해제된 JAR파일의 내용물들이 위치할 경로

- ***MKDIR*** 

    **PROPERTY**|**VALUE** 
    --|--
    structure| 폴더구조. `JSON`.  ex) { "dir1":{}, "dir2":{}, "dir3":{} }
    dest.path| 폴더구조가 생성될 경로

- ***COPY*** 

    **PROPERTY**|**VALUE** 
    --|--
    file.path| 복사할 원본 파일 
    dest.path| 복사될 사본 파일의 위치

- ***MERGEROPERTIES***
    
    **PROPERTY**|**VALUE** 
    --|--
    from| `File Path` from Extract value from.
    into| `File Path` Where it merge into.

- ***REPLACE*** 

    **PROPERTY**|**VALUE** 
    --|--
    file.path       | `File Path` to replace content
    replace         | replace A to B. Value is `JSON Object` ex) {"A":"B"}
    replace.line    | replace A to B. Value is `JSON Object` ex) {"target sentense":"one line change"}
    replace.property| replace property's valueA to valueB. Value is `JSON Object` ex) {"test.target.property":"valueB"}

## Task to Execute SQL
- ***SQL***

    **PROPERTY**|**VALUE** 
    --|--
    file.path       | SQL(.sql) `File Path` to Load and Execute Query
    sql.user        | Connection Infomation
    sql.password    | Connection Infomation
    sql.vendor      | Connection Infomation ex) oracle
    sql.ip          | Connection Infomation ex) localhost
    sql.port        | Connection Infomation ex) 1521
    sql.db          | Connection Infomation ex) orcl
    sql.replace     | 
    sql.replace.table| 
    sql.replace.index| 
    sql.replace.sequence| 
    sql.replace.view    | 
    sql.replace.function| 
    sql.replace.user    | 
    sql.replace.password| 
    sql.replace.tablespace| {"META31_DATA":"${tablespace.data}", "META31_IDX":"${tablespace.idx}"}
    sql.replace.datafile| {"META31_DATA":"${tablespace.file.data}", "META31_IDX":"${tablespace.file.idx}"}
    

## Task to TEST Connection
- ***JDBC*** 
    
    **PROPERTY**|**VALUE** 
    --|--
    ip| IP
    port| Port
    db| DB Instance
    vendor| Vendor ex) oralce
    id| ID
    pw| Password

- ***REST*** 
    
    **PROPERTY**|**VALUE** 
    --|--
    method| GET? POST? PUT? DELETE?
    url| Address to REST Server
    param| Request Parameter
    header| Request Header Parameter

- ***SOCKET***
 
    **PROPERTY**|**VALUE** 
    --|--
    ip| IP Address to Socket Serevr
    port| Port Number to Socket Server
    msg| Message What you want to send

- ***PORT***

    **PROPERTY**|**VALUE** 
    --|--
    from| ex) 1000
    to|  ex) 1010

## Task to Ask User What user want to
- ***NOTICE***

- ***Q*** 

- ***Q_CHOICE***

- ***Q_YN*** 

- ***SET***



## MORE Property `Options`


    **PROPERTY**|**VALUE** 
    --|--
    task       | You can choose task want to run.
    if         | You can set condition. Value is `JSON` ex)  { "a.test.answer":["1","2"] }

