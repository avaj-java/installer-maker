package com.jaemisseo.hoya.util


import jaemisseo.man.FileMan
import jaemisseo.man.VariableMan

class FileFinderUtil {


    static private VariableMan variableMan = new VariableMan().setVariableSign("#")


    static class File {
        String searchRootPath
        String searchFileName
        List<String> resultDefaultList
        Object searchIf
        //Editor
        String editResultPath
        String editResultRefactoringPattern
        String editResultRefactoringResult
    }

    static void collectFileRecursivley(List<java.io.File> itemList, File fileFinder){
        int defaultCount = 0
        String searchRootPath = fileFinder.searchRootPath
        String searchFileName = fileFinder.searchFileName
        List<String > resultDefaultList = fileFinder.resultDefaultList
        Object searchIf = fileFinder.searchIf
        String editResultPath = fileFinder.editResultPath
        String editResultRefactoringPattern = fileFinder.editResultRefactoringPattern
        String editResultRefactoringResult = fileFinder.editResultRefactoringResult

        List<java.io.File> foundFileList = FileMan.findAllWithProgressBar(searchRootPath, searchFileName, searchIf){ data ->
            java.io.File foundFile = data.item

            if (!data.stringList && resultDefaultList){
                resultDefaultList.each{ String defaultPath ->
                    data.stringList << "  ${++defaultCount}) ${defaultPath}"
                    itemList << new java.io.File(defaultPath)
                }
            }

            //- Edit
            String editedPath = edit(foundFile, editResultPath, editResultRefactoringPattern, editResultRefactoringResult);

            //- Listing
            int count = data.count + defaultCount
            data.stringList << "  ${count}) ${editedPath}"

            //- Collecting
            itemList << new java.io.File(editedPath)

            return true
        }
    }

    static void collectFile(List<java.io.File> itemList, File fileFinder){
        int defaultCount = 0
        String searchRootPath = fileFinder.searchRootPath
        String searchFileName = fileFinder.searchFileName
        List<String > resultDefaultList = fileFinder.resultDefaultList
        Object searchIf = fileFinder.searchIf
        String editResultPath = fileFinder.editResultPath
        String editResultRefactoringPattern = fileFinder.editResultRefactoringPattern
        String editResultRefactoringResult = fileFinder.editResultRefactoringResult

        //TODO: Not Good.. ==> must be updated with more more nice logic
        if (resultDefaultList){
            resultDefaultList.each{ String defaultPath ->
                println "  ${++defaultCount}) ${defaultPath}"
                itemList << new java.io.File(defaultPath)
            }
        }

        if (!searchRootPath)
            searchRootPath = ''



        List<String> foundFilePathList = FileMan.getSubFilePathList(searchRootPath + '/*')
        foundFilePathList.eachWithIndex{ String foundFilePath, int index ->
            //- Edit
            java.io.File foundFile = new java.io.File(foundFilePath)
            String editedPath = edit(foundFile, editResultPath, editResultRefactoringPattern, editResultRefactoringResult);

            //- Listing
            int count = index + 1 + defaultCount
            println "  ${count}) ${editedPath}"

            //- Collecting
            itemList << new java.io.File(editedPath)

        }
    }



    static edit(java.io.File foundFile, String editResultPath, String editResultRefactoringPattern, String editResultRefactoringResult){
        //Edit - relativePath
        String editedPath = null;
        if (editResultPath != null){
            editedPath = FileMan.getFullPath(foundFile.path, editResultPath)
        }else{
            editedPath = foundFile.path
        }

        //Edit - format
        if (editResultRefactoringPattern != null){
            String asisCodeRule = editResultRefactoringPattern
            String slashedEditedPath = FileMan.toSlash(editedPath)
            Map<String, Object> matchedMap = variableMan.matchVariableMap(asisCodeRule, slashedEditedPath);

            String tobeCodeRule = editResultRefactoringResult
            String result = variableMan.parseString(tobeCodeRule, matchedMap);
            editedPath = result
        }else{
        }

        return editedPath
    }



}
