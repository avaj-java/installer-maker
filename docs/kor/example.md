# Example

## fetch-maker

- 두 버전의 폴더를 비교하여 변경된 파일만 선별적으로 카피하여 패치패키지를 만드는 예제
- hoya.yml
    ```yaml
    ##################################################
    ##### OPTION SETUP
    ##################################################
    mode.report: true   
    report.file.path: ./distribution/report_${var.distribution.name}
    var.distribution.name: meta_changes_${date(yyMMdd_HHmmss)} 
    
    ##################################################
    ##### COMMAND - fetch-maker
    ##################################################
    fetch-maker:
    
      ######################### 선택 - 이전버전
      selectBefore:
        task: QuestionFindFile
        desc: Finding - BEFORE Version 
        answer.default: 1
        find:
          root.path: ./
          file.name: '*'
        mode.recursive: false
        property: var.before
    
      ######################### 선택 - 최신버전
      selectAfter:
        task: QuestionFindFile
        desc: Finding - AFTER Version
        answer.default: 1
        find:
          root.path: ./
          file.name: '*'      
        mode.recursive: false
        property: var.after
    
      ######################### 비교 - 2개의 버전을 비교하여 result.root.dir에 복사
      makeFetch:
        task: DiffFile
        desc: 'Diff version - BEFORE : AFTER'
        diff:
          before.root.dir: ${var.before}
          after.root.dir: ${var.after}
          result.root.dir: ./distribution/${var.distribution.name}
        mode.copy.new: true
        mode.copy.update: true
        mode.copy.none: false
        mode.copy.remove: false
    
      ######################### 압축 - ZIP으로 압축
      autoZip:
        task: zip
        from: ./distribution/${var.distribution.name}/*
        to: ./distribution/${var.distribution.name}.zip
        # mode.exclude.file.size.zero: true
    ```
  
    ```yml
    hoya fetch-maker
    ```
  
## Docker Manager
```yaml
###########################################################################
#####
#####
##### [Manager] Docker Manager
#####
#####
###########################################################################
dockerman:

  docker-ps:
    task: exec
    command: docker ps -a

  main:
    task: questionChoice
    desc: Action?
    answer:
      default: ps
      options:
        - 0)      Back to main
        - 1)      install jelly (new WAS Container) + (new DB Container)
        - 2)      install jelly (new WAS Container) + (existing DB Container)
        - 3)      install jelly (new WAS Container)
        - 4)      install jelly (new DB  Container)
        - ps)     docker ps -a
        - exec)   docker exec -it {CONTAINER_NAME} /bin/bash
        - logs)   docker logs -f {CONTAINER_NAME}
        - stats)  docker stats
        - stop)   docker stop {CONTAINER_NAME}
        - rm)     docker stop {CONTAINER_NAME} & docker rm {CONTAINER_NAME}
        - start)  docker start {CONTAINER_NAME}
        - images) docker images
        - x)      run xrdp
        - o)      run oracle
        - t)      run tibero
        - p)      run postgreSql
        - m)      run mysql
        - q)   Exit This Program

  #########################
  ##### Control Docker
  #########################
  restart:
    if: '{"dockerman.main.answer":"0"}'
    task: restart

  exit:
    if: '{"dockerman.main.answer":"q"}'
    task: exit

  docker-start:
    if: '{"dockerman.main.answer":"start"}'
    task: exec
    mode.ignore.error: true
    mode.variable.question.before.task: true
    command:
      - docker start ${container.name}

  docker-exec:
    if: '{"dockerman.main.answer":"exec"}'
    task: exec
    mode.ignore.error: true
    mode.variable.question.before.task: true
    command:
      lin:
        - docker exec -it ${container.name} /bin/bash
      win:
        - start docker exec -it ${container.name} /bin/bash

  docker-logs:
    if: '{"dockerman.main.answer":"logs"}'
    task: exec
    mode.ignore.error: true
    mode.variable.question.before.task: true
    command:
      lin:
        - docker logs -f ${container.name}
      win:
        - start docker logs -f ${container.name}

  docker-stats:
    if: '{"dockerman.main.answer":"stats"}'
    task: exec
    command:
      lin:
        - docker stats
      win:
        - start docker stats

  docker-stop-rm:
    if: '{"dockerman.main.answer":"rm"}'
    task: exec
    mode.ignore.error: true
    mode.variable.question.before.task: true
    command:
      - docker stop ${container.name}
      - docker rm ${container.name}

  docker-stop:
    if: '{"dockerman.main.answer":"stop"}'
    task: exec
    mode.ignore.error: true
    mode.variable.question.before.task: true
    command:
      - docker stop ${container.name}

  #########################
  ##### Run (New Container)
  #########################
  docker-run-test-new-was-new-db:
    if: '{"dockerman.main.answer":"1"}'
    task: command
    command: docker-test-new-was-new-db

  docker-run-test-new-was-existing-db-con:
    if: '{"dockerman.main.answer":"2"}'
    task: command
    command: docker-test-new-was-existing-db-con

  docker-run-test-new-was:
    if: '{"dockerman.main.answer":"3"}'
    task: command
    command: docker-test-new-was

  docker-run-test-new-db:
    if: '{"dockerman.main.answer":"4"}'
    task: command
    command: docker-test-new-db

  docker-run-xrdp:
    if: '{"dockerman.main.answer":"x"}'
    desc: |
      #########################
      # <Ubuntu Account>
      # ID: ubuntu
      # PW: ubuntu
      #########################
    task: exec
    mode.ignore.error: true
    mode.variable.question.before.task: true
    command:
      - docker run -d --name shared-docker --privileged docker:stable-dind --storage-driver=overlay2
      - docker run -d --name ${xrdp.container.name} --link shared-docker:docker --shm-size 1g --hostname docker-terminal -p ${xrdp.port}:3389 -p ${xrdp.ssh.port}:22 danielguerra/ubuntu-xrdp-docker

  docker-run-oracle:
    if: '{"dockerman.main.answer":"o"}'
    desc: |
      #########################
      # <Oracle Account>
      # ID: system
      # PW: oracle
      #########################
    task: exec
    mode.ignore.error: true
    mode.variable.question.before.task: true
    command:
      - docker run -d --name ${oracle.db.name().nvl(test-oracle)} -p ${oracle.db.port().nvl(1521)}:1521 -e ORACLE_ALLOW_REMOTE=true wnameless/oracle-xe-11g

  docker-run-postgresql:
    if: '{"dockerman.main.answer":"p"}'
    desc: |
      #########################
      # <PostgreSQL Account>
      # ID: postgres
      # PW: postgres
      #########################
    task: exec
    mode.ignore.error: true
    mode.variable.question.before.task: true
    command:
      - docker run -d --name ${postgres.container.name().nvl(test-postgres)} -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p ${postgres.db.port().nvl(5432)}:5432 postgres

  docker-run-mysql:
    if: '{"dockerman.main.answer":"m"}'
    desc: |
      #########################
      # <MYSQL Account>
      # ID: root
      # PW: mysql
      #########################
    task: exec
    mode.ignore.error: true
    mode.variable.question.before.task: true
    command:
      - docker run -d --name ${mysql.container.name().nvl(test-mysql)} -e MYSQL_ROOT_PASSWORD=mysql -p ${mysql.db.port().nvl(3306)}:3306 mysql

  ##### Finish
  finishItAtOnce:
    ifoption: '{"once":true}'
    task: exit

  ##### Back to Main
  backToStart:
    task: back
    to: dockerman.docker-ps

###########################################################################
#####
##### [Docker] New WAS / New DB
#####
###########################################################################
jelly.db.ip: 192.168.0.158

jelly.db.container.name: jelly_db
jelly.db.port: 2521
jelly.db.user: jelly_test

jelly.was.container.name: jelly_was
jelly.was.port: 22225
jelly.was.path: /home/ubuntu/${jelly.db.user}

docker-test-new-was-new-db:

  fetch-choice:
    task: command
    command: find-fetch

  make-installer:
    task: exec
    command:
      - installer-maker clean build --build

  install-startup-db:
    task: exec
    mode.variable.question.before.command: true
    mode.ignore.error: true
    command:
      # RUN DB container
      - docker run --name ${jelly.db.container.name().nvl(jelly_postgres)} -d -p ${jelly.db.port().nvl(1521)}:1521 -e ORACLE_ALLOW_REMOTE=true wnameless/oracle-xe-11g

  test-db:
    task: testjdbc
    mode.variable.question.before.command: true
    vendor: oracle
    ip: localhost
    port: ${jelly.db.port}
    db: xe
    user: system
    password: oracle
    try.second: 120

  ready-to-install-metastream:
    task: exec
    mode.variable.question.before.command: true
    command:
      # RUN WAS container
      - docker run --name ${jelly.was.container.name().nvl(jelly_was)} -d --link ${jelly.db.container.name} -p ${jelly.was.port().nvl(22225)}:11111 souljungkim/base-os
      # COPY MetaStream Installer(ZIP)
      - docker cp build/installer_dist/installer_myproject.zip ${jelly.was.container.name}:/home/ubuntu/
      - docker cp ./install-data/rsp/test-all.rsp ${jelly.was.container.name}:/home/ubuntu/test-all.rsp
      - docker exec ${jelly.was.container.name} unzip /home/ubuntu/installer_myproject.zip -d /home/ubuntu
      - docker exec ${jelly.was.container.name} chmod -R 755 /home/ubuntu/installer_myproject

  copy-fetch-to-docker:
    task: exec
    mode.ignore.error: true
    command:
      - docker cp ${var.fetch.from().fullpath()} ${jelly.was.container.name}:/home/ubuntu/fetch/

  install-metastream:
    task: exec
    mode.variable.question.before.command: true
    command:
      - docker exec ${jelly.was.container.name} /home/ubuntu/installer_myproject/bin/install -rsp=/home/ubuntu/test-all.rsp -ip=${jelly.db.container.name} -db=xe -user=${jelly.db.user} -was.path=${jelly.was.path} -fetch.from=/home/ubuntu/fetch/

  startup-open-metastream:
    task: exec
    desc: '[Open] MetaStream'
    command:
      lin:
        # STARTUP MetaStream WAS
        - docker exec ${jelly.was.container.name} chmod 755 -R ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77
        - docker exec ${jelly.was.container.name} ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77/bin/startup.sh
      win:
        # STARTUP MetaStream WAS
        - docker exec ${jelly.was.container.name} chmod 755 -R ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77
        - docker exec ${jelly.was.container.name} ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77/bin/startup.sh
        # OPEN MetaStream
        - start http://localhost:${jelly.was.port}/metastream
        - start docker exec ${jelly.was.container.name} tail -f ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77/logs/catalina.out

###########################################################################
#####
##### [Docker] New WAS / existing DB
#####
###########################################################################
docker-test-new-was-existing-db-con:

  fetch-choice:
    task: command
    command: find-fetch

  make-installer:
    task: exec
    command:
      - installer-maker clean build --build

  ready-to-install-metastream:
    task: exec
    mode.variable.question.before.command: true
    command:
      # RUN WAS container
      - docker run --name ${jelly.was.container.name().nvl(jelly_was)} -d --link ${jelly.db.container.name} -p ${jelly.was.port().nvl(22225)}:11111 souljungkim/base-os
      # COPY MetaStream Installer(ZIP)
      - docker cp build/installer_dist/installer_myproject.zip ${jelly.was.container.name}:/home/ubuntu/
      - docker cp ./install-data/rsp/test-was.rsp ${jelly.was.container.name}:/home/ubuntu/test-was.rsp
      - docker exec ${jelly.was.container.name} unzip /home/ubuntu/installer_myproject.zip -d /home/ubuntu
      - docker exec ${jelly.was.container.name} chmod -R 755 /home/ubuntu/installer_myproject

  copy-fetch-to-docker:
    task: exec
    mode.ignore.error: true
    command:
      - docker cp ${var.fetch.from().fullpath()} ${jelly.was.container.name}:/home/ubuntu/fetch/

  install-metastream:
    task: exec
    mode.variable.question.before.command: true
    command:
      - docker exec ${jelly.was.container.name} /home/ubuntu/installer_myproject/bin/install -rsp=/home/ubuntu/test-was.rsp -ip=${jelly.db.container.name().nvl(jelly_db)} -db=${jelly.db.name().nvl(xe)} -user=${jelly.db.user().nvl(jelly_test)} -was.path=${jelly.was.path} -fetch.from=/home/ubuntu/fetch/

  startup-open-metastream:
    task: exec
    desc: '[Open] MetaStream'
    command:
      lin:
        # STARTUP MetaStream WAS
        - docker exec ${jelly.was.container.name} chmod 755 -R ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77
        - docker exec ${jelly.was.container.name} ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77/bin/startup.sh
      win:
        # STARTUP MetaStream WAS
        - docker exec ${jelly.was.container.name} chmod 755 -R ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77
        - docker exec ${jelly.was.container.name} ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77/bin/startup.sh
        # OPEN MetaStream
        - start http://localhost:${jelly.was.port}/metastream
        - start docker exec ${jelly.was.container.name} tail -f ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77/logs/catalina.out

###########################################################################
#####
##### [Docker] New WAS
#####
###########################################################################
docker-test-new-was:

  fetch-choice:
    task: command
    command: find-fetch

  make-installer:
    task: exec
    command:
      - installer-maker clean build --build

  ready-to-install-metastream:
    task: exec
    mode.variable.question.before.command: true
    command:
      # RUN WAS container
      - docker run --name ${jelly.was.container.name().nvl(jelly_was)} -d -p ${jelly.was.port().nvl(22225)}:11111 souljungkim/base-os
      # COPY MetaStream Installer(ZIP)
      - docker cp build/installer_dist/installer_myproject.zip ${jelly.was.container.name}:/home/ubuntu/
      - docker cp ./install-data/rsp/test-was.rsp ${jelly.was.container.name}:/home/ubuntu/test-was.rsp
      - docker exec ${jelly.was.container.name} unzip /home/ubuntu/installer_myproject.zip -d /home/ubuntu
      - docker exec ${jelly.was.container.name} chmod -R 755 /home/ubuntu/installer_myproject

  copy-fetch-to-docker:
    task: exec
    mode.ignore.error: true
    command:
      - docker cp ${var.fetch.from().fullpath()} ${jelly.was.container.name}:/home/ubuntu/fetch/

  install-metastream:
    task: exec
    mode.variable.question.before.command: true
    command:
      - docker exec ${jelly.was.container.name} /home/ubuntu/installer_myproject/bin/install -rsp=/home/ubuntu/test-was.rsp -ip=${jelly.db.ip} -db=${jelly.db.name().nvl(da)} -user=${_jelly.db.user().nvl(jelly_s)} -was.path=${jelly.was.path} -fetch.from=/home/ubuntu/fetch/

  startup-open-metastream:
    task: exec
    desc: '[Open] MetaStream'
    command:
      lin:
        # STARTUP MetaStream WAS
        - docker exec ${jelly.was.container.name} chmod 755 -R ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77
        - docker exec ${jelly.was.container.name} ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77/bin/startup.sh
      win:
        # STARTUP MetaStream WAS
        - docker exec ${jelly.was.container.name} chmod 755 -R ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77
        - docker exec ${jelly.was.container.name} ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77/bin/startup.sh
        # OPEN MetaStream
        - start http://localhost:${jelly.was.port}/metastream
        - start docker exec ${jelly.was.container.name} tail -f ${jelly.was.path().fullpath()}/apache-tomcat-7.0.77/logs/catalina.out

###########################################################################
#####
##### [Docker] New DB
#####
###########################################################################
docker-test-new-db:

  make-installer:
    task: exec
    command:
      - installer-maker clean build --build

  install-startup-db:
    task: exec
    mode.variable.question.before.command: true
    mode.ignore.error: true
    command:
      # RUN DB container
      - docker run --name ${jelly.db.container.name().nvl(jelly_postgres)} -d -p ${jelly.db.port().nvl(1521)}:1521 -e ORACLE_ALLOW_REMOTE=true wnameless/oracle-xe-11g

  test-db:
    task: testjdbc
    mode.variable.question.before.command: true
    vendor: oracle
    ip: localhost
    port: ${jelly.db.port}
    db: xe
    user: system
    password: oracle
    try.second: 120

  install-metastream:
    task: exec
    mode.variable.question.before.command: true
    command:
      - installer-maker test -rsp=./install-data/rsp/test-db.rsp -ip=127.0.0.1 -port=${jelly.db.port} -db=xe -dba.user=system -dba.password=oracle -user=${jelly.db.user}

###########################################################################
#####
##### [Docker] extract-sql from db
#####     - test..
###########################################################################
var.path.sql.autogen:
  jelly: ./install-data/sql/jelly/postgresql_autogen
  mammoth: ./install-data/sql/mammoth/postgresql_autogen
  jelly_ui: ./install-data/sql/jelly_ui/postgresql_autogen


extract-sql-jelly:
  t1:
    task: mkdir
    to: ${var.path.sql.autogen.jelly}

  t2:
    task: exec
    before-command:
      - docker exec pgsql-9.2 su postgres -c "pg_dump --schema-only -t ${listfile('./install-data/jelly_table.list').join(' -t ')} nia" > ${var.path.sql.autogen.jelly}/02_create_table.sql
    command:
      lin:
        - ls ${var.path.sql.autogen.jelly}
      win:
        - dir ${var.path.sql.autogen.jelly().winpath()}
    after-command:
      - git status

extract-sql-mammoth:
  t1:
    task: mkdir
    to:  ${var.path.sql.autogen.mammoth}

  t2:
    task: exec
    before-command:
      - docker exec pgsql-9.2 su postgres -c "pg_dump --schema-only -t ${listfile('./install-data/mammoth_table.list').join(' -t ')} nia" > ${var.path.sql.autogen.mammoth}/02_create_table.sql
      - docker exec pgsql-9.2 su postgres -c "pg_dump --column-inserts --data-only -t ${listfile('./install-data/mammoth_data.list').join(' -t ')} nia" > ${var.path.sql.autogen.mammoth}/03_insert_data.sql
    command:
      lin:
        - ls ${var.path.sql.autogen.mammoth}
      win:
        - dir ${var.path.sql.autogen.mammoth().winpath()}
    after-command:
      - git status

extract-sql-ui:
  t1:
    task: mkdir
    to:  ${var.path.sql.autogen.jelly_ui}

  t2:
    task: exec
    before-command:
      - docker exec pgsql-9.2 su postgres -c "pg_dump --schema-only -t ${listfile('./install-data/jelly_ui_table.list').join(' -t ')} nia" > ${var.path.sql.autogen.jelly_ui}/02_create_table.sql
      - docker exec pgsql-9.2 su postgres -c "pg_dump --schema-only -t ${listfile('./install-data/jelly_ui_others_table.list').join(' -t ')} nia" > ${var.path.sql.autogen.jelly_ui}/02_create_others_table.sql
    command:
      lin:
        - ls ${var.path.sql.autogen.jelly_ui}
      win:
        - dir ${var.path.sql.autogen.jelly_ui().winpath()}
    after-command:
      - git status

###########################################################################
#####
##### Find Fetch
#####
###########################################################################
find-fetch:

  findFetchDirPath:
    task: QuestionFindFile
    desc: Finding fetches
    answer.default: 1
    find:
      root.path: ./install-data/fetch-site/
      file.name: meta.part.properties
      result.edit.relpath: ./
      result.default:
        - ''
    mode.only.interactive: true
    property: var.fetch.from
```