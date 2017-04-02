-----
# Needs   
- OS: `WINDOWS`, `LINUX`, `UNIX`
- JDK: `JDK 1.6+`

-----
# 1. How to install Installer

1. Unzip to where you want (installer-x.x.x.zip)
    - Windows
     
        ```sh
        c:\installer
        ```
    - Linux and Unix
    
        ```sh
        /home/dev/installer
        ```        

2. set Envirionment Variable, `INSTALLER_HOME`
    - Windows
    
        ```sh
        INSTALLER_HOME=c:\installer
        ```
    - Linux and Unix
    
        ```sh
        INSTALLER_HOME=/home/dev/installer
        ```        

3. add Envirionment Variable value, `path`
    - Windows
     
        ```sh
        path=c:\installer\bin;%path%
        ```
    - Linux and Unix
    
        ```sh
        path=/home/dev/installer/bin:$path
        ```
        
4. Check Installer Version
    
    ```sh
    installer -v
    ```
    
##### NEXT: [2. How to make Your Installer](installer_build.md)