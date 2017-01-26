-----
# How To Use Installer



-----
# How To Test JDBC Connection

### METHOD 1. RUN SHELLSCRIPT (run_testdb.sh)
ex)
```sh
./run_testdb.sh
```

### METHOD 2. COMMAND MANUALLY
```sh
{path/to/java} -jar {path/to/installer.jar} -testdb=true -id={USERID} -pw={PASSWORD} -ip={IP} -port={PORT} -db={DB INSTANCE}
```

```sh
java -jar ./installer.jar -testdb=true -id=meta3 -pw=meta3 -ip=127.0.0.1 -port=1521 -db=orcl
```

### MORE OPTION
- You can set `vendor` to set driver and url automatically  (oracle or tibero)
 
```sh
java -jar ./installer.jar -testdb=true -ip=192.168.0.158 -port=1521 -db=da -id=meta3 -pw=meta3 -vendor=tibero
```

- You can set `driver`, `url` manually 

```sh
java -jar ./installer.jar -testdb=true -driver=oracle.jdbc.driver.OracleDriver -url=jdbc:oracle:thin:@192.168.0.158:1521:da -id=meta3 -pw=meta3
```

- You can set other `query`

```sh
java -jar ./installer.jar -testdb=true -ip=192.168.0.158 -port=1521 -db=da -id=meta3 -pw=meta3 -query="select * from tab where TNAME = 'HELLO_TABLE'"
```

### DEPENDENCIES
- ojdbc6-11.2.0.3.jar
- tibero-jdbc-5.0.jar
- groovy-all-1.8.3.jar