[![Build Status](https://travis-ci.org/avaj-java/installer-maker.svg?branch=master)](https://travis-ci.org/avaj-java/installer-maker)
[![All Download](https://img.shields.io/github/downloads/avaj-java/installer-maker/total.svg)](https://github.com/avaj-java/installer-maker/releases)
[![Release](https://img.shields.io/github/release/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)
[![License](https://img.shields.io/github/license/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=MCUPCPFHFYZNN&lc=KR&item_name=jaemisseo&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)
                                                                             
*Read this in other languages: [English](README.md), [한국어](README.ko.md), [日本語](README.ja.md), [中文](README.ch.md)

-----
## 目次
1. 環境
2. Installer-Makerをインストールする
3. インストーラ作成



## 1. 環境
- OS：`WINDOWS`,` LINUX`, `UNIX`
- JDK：`JDK 1.6 +`



## 2. Installer-Makerをインストールする

`Release`をクリックして、` zipファイル`をダウンロードして、任意のフォルダに`解除`後`環境変数`に登録します。

[![Release](https://img.shields.io/github/release/avaj-java/installer-maker.svg)](https://github.com/avaj-java/installer-maker/releases)

### 2-1-1. インストールon Windows

1. 任意のフォルダに解除
    ```
    C:\installer-maker        
    ```

2. 環境変数INSTALLER_MAKER_HOMEに登録
    ```
    C:\installer-maker       
    ```

3. 環境変数pathに登録
    ```
    path=%INSTALLER_MAKER_HOME%\bin       
    ```
    
### 2-1-2. インストールon UNIX
    
1. 任意のフォルダに解除

    ```bash
    path/to/installer-maker
    ```
    
2. 環境変数INSTALLER_MAKER_HOMEに登録

    ```bash
    INSTALLER_MAKER_HOME=path/to/installer-maker
    ```
    
3. 環境変数pathに登録

    ```bash
    path=$INSTALLER_MAKER_HOME/bin:$path
    ```

### 2-2. バージョンの確認

```bash
installer-maker -v
```



## 3.インストーラ作成

1.スクリプトの作成

   Project Rootに次の2つのスクリプトファイルを作成して置かなければならないます。
       
   - `installer-maker.yml`（Installerを構成するためのスクリプト）
   - `installer.yml`（Installerがどのようにインストールするかのスクリプト）
   
   initコマンドで簡単にサンプル（Sample）ファイルを生成することができます。
   
   ```bash
   installer-maker init
   ```
   
   自分の環境に合わせてスクリプトを作成します。
   スクリプトの作成は、ドキュメントを参照してください。
   
2. Installer作成

    Project Rootでビルドコマンドをします。
    
    ```bash
    installer-maker clean build
    ```
    
    ビルドに成功すると、次の3つのフォルダが作成されます。
    
    - build/installer-temp（インストーラを作成しながら生じた一時フォルダ）
    - build/installer-dist（インストーラが圧縮ファイルとして作成されるフォルダ）
    - build/installer-myproject or 設定したフォルダ名（インストーラが作成したフォルダ）
    
    
    
##詳細ヘルプ

[詳細ヘルプ]（）

[![paypal](https://www.paypal.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=MCUPCPFHFYZNN&lc=KR&item_name=jaemisseo&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)