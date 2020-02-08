# Example

## fetch-maker

- 두 버전의 폴더를 비교하여 변경된 파일만 선별적으로 카피하여 패치패키지를 만드는 예제
- hoya fetch-maker
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