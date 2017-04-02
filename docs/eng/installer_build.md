

-----
# 2. How to make Your Installer

1. You need 3 properties files to make the Your Installer. 

    - `builder.properties`
    - `receptionist.properties`
    - `installer.properties`


2. So, Command '`installer init`' on root path of Your Project Directory To Create 3 properties files   

    - init    
        ```
        installer init
        ```

3. And then, Write your process into 3 properties files (ref. [properties.md](properties.md))    

    - `builder.properties`
           
        Write how to make installer
        
        ```properties
        ```
        
    - `receptionist.properties`
    
        Write question what ask your user for how to install
        
        ```properties
        ```
        
    - `installer.properties`
    
        Write how to install
        
        ```properties
        ```

4. Build your installer for your project

    - build        
        
        ```
        installer build
        ```

# How to Install  

1. Go to INSTALLER home path you set 
    ```sh
    cd {BUILD_INSTALLER_HOME}    
    ```
    
2. Run install in bin Directory (Default bin Directory Path is `{BUILD_INSTALLER_HOME}/bin`)
    ```sh
    {BUILD_INSTALLER_HOME}/bin/install
    ```
    
3. Then, Your `Receptionist ask Your User` What user wants, And `It be installed by Your Installer`  

##### NEXT: [3. How to Install Your Program with Your Installer](installer_build_install.md)
