[![Build Status](https://travis-ci.org/avaj-java/installer-maker.svg?branch=master)](https://travis-ci.org/avaj-java/installer-maker)
[![All Download](https://img.shields.io/github/downloads/avaj-java/installer-maker/total.svg)](https://github.com/avaj-java/installer-maker/releases)
[![Release](https://img.shields.io/github/release/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)
[![License](https://img.shields.io/github/license/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=MCUPCPFHFYZNN&lc=KR&item_name=jaemisseo&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)
                                                                             
*Read this in other languages: [English](README.md), [한국어](README.ko.md), [日本語](README.ja.md), [中文](README.ch.md)

-----
## table of contents
1. Environment
2. How to install Installer-Maker
3. How to build Installer



## 1. Environment   
- OS: `WINDOWS`, `LINUX`, `UNIX`
- JDK: `JDK 1.6+`



## 2. How to install

Click `Release` to download` zip file `, extract it to the folder you want and register it in` environment variable`.

[![Release](https://img.shields.io/github/release/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)

### 2-1-1. install on Windows
                    
1. Unzip to where you want
    ```
    C:\installer-maker        
    ```                

2. set Envirionment Variable `INSTALLER_MAKER_HOME`
    ```
    C:\installer-maker       
    ```

3. add Envirionment Variable `path`
    ```
    path=%INSTALLER_MAKER_HOME%\bin       
    ```
    
### 2-1-2. install on UNIX
    
1. Unzip to where you want
    ```bash
    path/to/installer-maker
    ```           
    
2. set Envirionment Variable `INSTALLER_MAKER_HOME`    
    ```bash
    INSTALLER_MAKER_HOME=path/to/installer-maker
    ```
    
3. add Envirionment Variable `path`
    ```bash
    path=$INSTALLER_MAKER_HOME/bin:$path
    ```

### 2-2. Check Version 

```bash
installer-maker -v
```



## 3. How to build Installer

1. Write Script

    You need to create the following two script files in Project Root.  
        
    - `installer-maker.yml` (Script for configuring Installer)
    - `installer.yml` (Script about how the installer will install)
    
    You can simply create a sample file with the init command. 
    
    ```bash
    installer-maker init
    ```
    
    Create scripts to suit your environment.
    Please refer to the documentation for writing scripts.
    
2. Build Installer 

    Build command in Project Root.
        
    ```bash
    installer-maker clean build
    ```

    If the build succeeds, the following three folders are created.

    - build/installer-temp (a temporary folder created when you created the installer)
    - build/installer-dist (the folder where the installer is created as a compressed file)
    - build/installer-myproject or the folder name you set (the directory where the installer was created)       


## Detailed help

[Detailed help](https://github.com/avaj-java/installer-maker/tree/master/docs/kor) - "Sorry, only korean"

 
      


# :D hehehe

[![paypal](https://www.paypal.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=MCUPCPFHFYZNN&lc=KR&item_name=jaemisseo&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)
