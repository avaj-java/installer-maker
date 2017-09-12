# Command

I'm Sorry, My English is Not Good. But I try to write down `How to Use Installer`. 


- ***INIT***
    
    This command make 3 properties files, But If already exists files, does not make files.  

    **File Name**|**Destination**
    --|--
    builder.properties| `File Path` to want to Compress with TAR
    receptionist.properties| Where do you want extract TAR file to
    installer.properties| Where do you want extract TAR file to

- ***CLEAN***

    It Clean your build Folder you build. 

    **PROPERTY**|**VALUE** 
    --|--
    file.path| `File Path` to want to Compress with ZIP
    dest.path| Where do you want extract ZIP file to

- ***BUILD*** 

    It build Your Installer. 
    It need 3 properties files(`builder.properties`, `receptionist.properties`, `installer.properties`)
    refer to builder.properties you script    

    **PROPERTY**|**VALUE** 
    --|--
    file.path| `File Path` to want to Compress with JAR
    dest.path| Where do you want extract JAR file to

- ***RUN***
        
    **PROPERTY**|**VALUE** 
    --|--
    file.path| `TAR(.tar) File Path` on Your File System
    dest.path| Where do you want extract TAR file to
    
- ***TEST*** 
        
    TEST includes thease commands `CLEAN`, `BUILD`, `RUN`

    **PROPERTY**|**VALUE** 
    --|--
    file.path| `ZIP(.zip) File Path` on Your File System
    dest.path| Where do you want extract ZIP file to
    
- ***MACGYVER***

    It can run one task
    Ask for what do you want to do?

    **PROPERTY**|**VALUE** 
    --|--
    file.path| `JAR(.jar) File Path` on Your File System
    dest.path| Where do you want extract JAR file to

- ***HELP*** 

    It help you how to type on command line.
        
    ```
    installer-maker help    
    ```
