[![Build Status](https://travis-ci.org/avaj-java/installer-maker.svg?branch=master)](https://travis-ci.org/avaj-java/installer-maker)
[![All Download](https://img.shields.io/github/downloads/avaj-java/installer-maker/total.svg)](https://github.com/avaj-java/installer-maker/releases)
[![Release](https://img.shields.io/github/release/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)
[![License](https://img.shields.io/github/license/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=MCUPCPFHFYZNN&lc=KR&item_name=jaemisseo&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)
                                                                             
*Read this in other languages: [English](../../README.md), [한국어](../kor/README.ko.md), [日本語](../jpn/README.ja.md), [中文](README.ch.md) 

-----
## 内容
1. 环境
2. 安装Installer-Maker
3. 创建一个安装程序



## 1. 环境
- 操作系统：`WINDOWS`，`LINUX`，`UNIX`
- JDK：`JDK 1.6 +`



## 2. 安装Installer-Maker

然后点击Release 下载zip文件，禁用压缩您要注册的环境variables的文件夹。

[![Release](https://img.shields.io/github/release/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)

### 2-1-1. 在Windows上安装
                    
1. 解压缩到所需的文件夹
    ```
    C:\installer-maker        
    ```

2. 注册环境变量INSTALLER_MAKER_HOME
    ```
    C:\installer-maker       
    ```


3. 在环境变量路径中注册
    ```
    path=%INSTALLER_MAKER_HOME%\bin       
    ```
    
### 2-1-2. 在UNIX上安装
    
1. 解压缩到所需的文件夹

    ```bash
    path/to/installer-maker
    ```       

2. 注册环境变量INSTALLER_MAKER_HOME

    ```bash
    INSTALLER_MAKER_HOME=path/to/installer-maker
    ```
    
3. 在环境变量路径中注册

    ```bash
    path=$INSTALLER_MAKER_HOME/bin:$path
    ```


### 2-2. 检查版本

```bash
installer-maker -v
```



## 3. 创建安装程序

1. 脚本

    您需要在Project Root中创建以下两个脚本文件。
        
    - `installer-maker.yml`（配置安装程序的脚本）
    - `installer.yml`（安装程序将如何安装的脚本）
    
    您可以使用init命令简单地创建一个示例文件。
    
    ```bash
    installer-maker init
    ```
    
    创建适合您的环境的脚本。
    编写脚本请参考文档。
    
2. 创建一个安装程序

    在Project Root中生成命令。
    
    ```bash
    installer-maker clean build
    ```
    
    如果构建成功，则创建以下三个文件夹。
    
    - build/installer-temp（创建安装程序时创建的临时文件夹）
    - build/installer-dist（将安装程序创建为压缩文件的文件夹）
    - build/installer-myproject或您设置的文件夹名称（安装程序的创建目录）
    
       


## 详细的帮助

[更多帮助]() 
      


# :D

[![paypal](https://www.paypal.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=MCUPCPFHFYZNN&lc=KR&item_name=jaemisseo&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)