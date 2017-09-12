# TASK

I'm Sorry, My English is Not Good. But I try to write down `How to Use Installer`. 


## Task to Manage File
- ***TAR***

    **PROPERTY**|**VALUE**
    --|--
    file.path| `File Path` to want to Compress with TAR
    dest.path| Where do you want extract TAR file to

- ***ZIP***

    **PROPERTY**|**VALUE** 
    --|--
    file.path| `File Path` to want to Compress with ZIP
    dest.path| Where do you want extract ZIP file to

- ***JAR*** 

    **PROPERTY**|**VALUE** 
    --|--
    file.path| `File Path` to want to Compress with JAR
    dest.path| Where do you want extract JAR file to

- ***UNTAR***

    **PROPERTY**|**VALUE** 
    --|--
    file.path| `TAR(.tar) File Path` on Your File System
    dest.path| Where do you want extract TAR file to
    
- ***UNZIP*** 

    **PROPERTY**|**VALUE** 
    --|--
    file.path| `ZIP(.zip) File Path` on Your File System
    dest.path| Where do you want extract ZIP file to
    
- ***UNJAR***

    **PROPERTY**|**VALUE** 
    --|--
    file.path| `JAR(.jar) File Path` on Your File System
    dest.path| Where do you want extract JAR file to

- ***MKDIR*** 

    **PROPERTY**|**VALUE** 
    --|--
    structure| Directories to want to make. Value is `JSON`.  ex) { "dir1":{}, "dir2":{}, "dir3":{} }
    dest.path| `Root Path` to Make Directories

- ***COPY*** 

    **PROPERTY**|**VALUE** 
    --|--
    file.path| file to want to make. Value is `JSON`.  ex) { "dir1":{}, "dir2":{}, "dir3":{} }
    dest.path| `Root Path` to Make Directories

- ***MERGE_ROPERTIES***
    
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

