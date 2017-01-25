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
{path/to/java/java} -jar installer.jar -testdb=true -id={USERID} -pw={PASSWORD} -ip={IP} -port={PORT} -db={DB INSTANCE}
```

ex)
```sh
java -jar ./installer.jar -testdb=true -id=meta3 -pw=meta3 -ip=127.0.0.1 -port=1521 -db=orcl
```

### MORE 
- You can set `driver`, `url`

ex)
```sh
java -jar ./installer.jar -testdb=true -driver=oracle.jdbc.driver.OracleDriver -url=jdbc:oracle:thin:@192.168.0.158:1521:da -id=meta3 -pw=meta3
```

- You can set other `query

ex)
```sh
java -jar ./installer.jar -testdb=true -ip=192.168.0.158 -port=1521 -db=da -id=meta3 -pw=meta3 -query="select * from tab where TNAME = 'HELLO_TABLE'"
```