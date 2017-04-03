-----
## How to install Installer

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
    
## NEXT: [How to make Your Installer](installer_build.md)