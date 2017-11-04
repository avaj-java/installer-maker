[![Build Status](https://travis-ci.org/avaj-java/installer-maker.svg?branch=master)](https://travis-ci.org/avaj-java/installer-maker)
[![All Download](https://img.shields.io/github/downloads/avaj-java/installer-maker/total.svg)](https://github.com/avaj-java/installer-maker/releases)
[![Release](https://img.shields.io/github/release/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)
[![License](https://img.shields.io/github/license/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=MCUPCPFHFYZNN&lc=KR&item_name=jaemisseo&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)
                                                                             
*Read this in other languages: [English](README.md), [한국어](README.ko.md), [日本語](README.ja.md), [中文](README.ch.md)

-----
## 목차
1. 환경
2. Installer-Maker 설치하기
3. 인스톨러 만들기



## 1. 환경   
- OS: `WINDOWS`, `LINUX`, `UNIX`
- JDK: `JDK 1.6+`



## 2. Installer-Maker 설치하기

`Release`를 클릭하여 `zip파일`을 다운로드하고 원하는 폴더에 `압축을 해제` 후 `환경변수`에 등록합니다.

[![Release](https://img.shields.io/github/release/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)

### 2-1-1. 설치 on Windows
                    
1. 원하는 폴더에 압축해제
    ```
    C:\installer-maker        
    ```                

2. 환경변수 INSTALLER_MAKER_HOME에 등록
    ```
    C:\installer-maker       
    ```

3. 환경변수 path에 등록
    ```
    path=%INSTALLER_MAKER_HOME%\bin       
    ```
    
### 2-1-2. 설치 on UNIX
    
1. 원하는 폴더에 압축해제
    ```bash
    path/to/installer-maker
    ```           
    
2. 환경변수 INSTALLER_MAKER_HOME에 등록
    ```bash
    INSTALLER_MAKER_HOME=path/to/installer-maker
    ```
    
3. 환경변수 path에 등록
    ```bash
    path=$INSTALLER_MAKER_HOME/bin:$path
    ```

### 2-2. 버전확인 

```bash
installer-maker -v
```



## 3. 인스톨러 만들기

1. 스크립트 작성

    Project Root에 다음 2개의 스크립트 파일을 작성해 둬야합니다.    
        
    - `installer-maker.yml` (Installer를 구성하기 위한 스크립트)
    - `installer.yml` (Installer가 어떻게 설치할지에 대한 스크립트)
    
    init 명령어로 간단히 견본(Sample) 파일을 생성할 수 있습니다. 
    
    ```bash
    installer-maker init
    ```
    
    자신의 환경에 맞춰서 스크립트를 작성합니다. 
    스크립트 작성은 문서를 참조해주세요.    
    
2. Installer 만들기

    Project Root에서 빌드 명령을 합니다.
    
    ```bash
    installer-maker clean build
    ```

    빌드에 성공하면 다음 3개의 폴더가 생성됩니다.

    - build/installer-temp (인스톨러를 만들면서 생긴 임시 폴더)
    - build/installer-dist (인스톨러가 압축파일로 생성되는 폴더)
    - build/installer-myproject or 설정한 폴더명 (인스톨러가 만들어진 폴더)
    
       


## 상세 도움말

[상세 도움말]()     
 
      


# 도움이 되셨다면 :D

[![paypal](https://www.paypal.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=MCUPCPFHFYZNN&lc=KR&item_name=jaemisseo&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)