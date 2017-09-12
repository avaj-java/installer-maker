

# Macgyver

-----
### `zip`
- test
    ```sh
    installer -zip -file.path=~/testdir -dest.path=~/test.jar
    ```
    ```sh
    installer -zip ~/testdir ~/test.jar 
    ```

### `jar`
- test
    ```sh
    installer -jar -file.path=~/testdir -dest.path=~/test.jar
    ```
    ```sh
    installer -jar ~/testdir ~/test.jar 
    ```

### `tar`
- test
    ```sh
    installer -tar -file.path=~/testdir -dest.path=~/test.tar
    ```
    ```sh
    installer -tar ~/testdir ~/test.tar 
    ```

### `unzip`
- test
    ```sh
    installer -unzip -file.path=~/testdir -dest.path=~/test.tar
    ```
    ```sh
    installer -unzip ~/testdir ~/test.tar
    ```

### `unjar`
- test
    ```sh
    installer -unjar -file.path=~/testdir -dest.path=~/test.tar
    ```
    ```sh
    installer -unjar ~/testdir ~/test.tar
    ```

### `untar`
- test
    ```sh
    installer -untar -file.path=~/testdir -dest.path=~/test.tar
    ```    
    ```sh
    installer -untar ~/testdir ~/test.tar
    ```

### `copy`
- test
    ```sh
    installer -copy -file.path=~/testdir/* -dest.path=~/test
    ```
    ```sh
    installer -copy ~/testdir/* ~/test
    ```

### `mkdir`
- test
    ```sh
    installer -mkdir -structure={} -dest.path=~/testStructure
    ```
    ```sh
    installer -mkdir {} ~/testStructure
    ```

### `test-db`
- test-db
    ```sh
    installer -test-db oracle 127.0.0.1 1521 orcl user password
    ```
    ```sh
    installer -test-db -id={USERID} -pw={PASSWORD} -ip={IP} -port={PORT} -db={DB INSTANCE}
    ```
    ```sh
    installer -test-db -id=user -pw=password -ip=127.0.0.1 -port=1521 -db=orcl
    ```

- You can set `vendor` to set driver and url automatically  (oracle or tibero) 
    ```sh
    installer -test-db -ip=192.168.0.150 -port=1521 -db=orcl -id=user -pw=password -vendor=tibero
    ```

- You can set `driver`, `url` manually
    ```sh
    installer -test-db -driver=oracle.jdbc.driver.OracleDriver -url=jdbc:oracle:thin:@192.168.0.158:1521:da -id=user -pw=password
    ```

- You can set other `query`
    ```sh
    installer -test-db -ip=192.168.0.108 -port=1521 -db=da -id=user -pw=password -query="select * from tab where TNAME = 'HELLO_TABLE'"
    ```

### `test-rest`
- rest-test
    ```sh
    installer -test-rest
    ```

### `test-socket`
- rest-socket
    ```sh
    installer -test-socket
    ```

### `test-email`
- rest-email
    ```sh
    installer -test-email
    ```

### `test-port`
- rest-port
    ```sh
    installer -test-port  
    ```

### `merge-properties`
- merge-properties
    ```sh
    installer -merge-properties ~/a.properties ~/b.properties
    ```
    ```sh
    installer -merge-properties -from=~/a.properties -into=b.properties
    ```







#### MORE OPTION
- -x.execute.sql
- -x.check.before
- -x.report
- -x.report.console
- -report.sql
- -report.text
- -report.excel (Not yet)
- -report.file.encoding



-----
# 2. How To Just Check DB Object with SQLFile

ex)
```sh
java -jar ./installer.jar -x.execute.sql
```



-----
# 3. How To Just Generate SQLFile

ex)
```sh
java -jar ./installer.jar -x.execute.sql -report.sql -report.file.encoding=utf-8 
```

