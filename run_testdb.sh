java -jar build/libs/installer.jar -testdb=true -ip=192.168.0.158 -port=1521 -db=da -id=meta3 -pw=meta3
#########################
##### OTHER WAYS
#########################
##### 1. You can set vendor to set driver and url automatically
##### ex)
##### java -jar build/libs/installer.jar -testdb=true -ip=192.168.0.158 -port=1521 -db=da -id=meta3 -pw=meta3 -vendor=tibero
#####
##### 2. You can set driver, url manually
##### ex)
##### java -jar build/libs/installer.jar -testdb=true -driver=oracle.jdbc.driver.OracleDriver -url=jdbc:oracle:thin:@192.168.0.158:1521:da -id=meta3 -pw=meta3
#####
##### 3. You can set other query
##### ex)
##### java -jar build/libs/installer.jar -testdb=true -ip=192.168.0.158 -port=1521 -db=da -id=meta3 -pw=meta3 -query="select * from tab where TNAME = 'HELLO_TABLE'"