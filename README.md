[![build status](http://192.168.0.208/dg/installer/badges/master/build.svg)](http://192.168.0.208/dg/installer/commits/master)

-----
# Developer Menual

### Build

1. Gradle Build

    - Make a ZIP file
        ```sh
        gradle clean distZip
        ```
    - Make a ZIP file and Update INSTALLER_HOME
        ```sh
        gradle clean deployLocal
        ```

2. Deploy to Your User        

    - ZIP file path      
        ```sh
        {PROJECT_DIRECTORY}/build/distributions/installer-x.x.x.zip
        ```

3. Then, Make Your User read Menuals

    - [1. How to install Installer](docs/eng/installer.md) 
    - [2. How to make Your Installer](docs/eng/installer_insta.md) 


### Needs   
- OS: `WINDOWS`, `LINUX`, `UNIX`
- JDK: `JDK 1.6+`

### DEPENDENCIES
- ojdbc6-11.2.0.3
- tibero-jdbc-5.0
- groovy-all-2.1.3
- commons-compress-1.11
- jersey-client-1.9.1
- poi-3.9

