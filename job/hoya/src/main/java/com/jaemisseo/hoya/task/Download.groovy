package com.jaemisseo.hoya.task

import com.jaemisseo.hoya.bean.FileSetup
import jaemisseo.man.FileMan
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol

import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel

/**
 * Created by sujkim on 2017-02-22.
 */
@Task
@TerminalValueProtocol(['url', 'to'])
class Download extends TaskHelper{

    @Value(name='url', required=true)
    List<String> urls

    @Value(name='to', filter='findAllFilePaths')
    List<String> destPathList

    @Value
    FileSetup fileSetup



    @Override
    Integer run(){

        urls.each{ String sourceFilePath ->

            destPathList.each{ String destFilePath ->

                FileMan.copy(sourceFilePath, destFilePath, fileSetup)

            }

        }

        return STATUS_TASK_DONE
    }

//    private void download(URL downloadUrl, File downloadDestFile){
//        //Check downloadedFile
//        if (!downloadDestFile.exists()){
//            java.lang.System.out.println( "  > Download distribution");
//            java.lang.System.out.println( "    - from: ${downloadUrl.getPath()}" );
//            java.lang.System.out.println( "    -   to: ${downloadDestFile.getPath()}" );
//            FileUtils.copyURLToFile(downloadUrl, downloadDestFile);
//        }
//
//        //Check downloadedFile
//        if (!downloadDestFile.exists()){
////            throw new FailedToDownloadDistributionException()
//        }
//    }

    private downloadInNio(URL downloadUrl, File downloadDestFile){
        ReadableByteChannel readableByteChannel = Channels.newChannel(downloadUrl.openStream());
        String fileName = downloadDestFile.getName()

        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        FileChannel fileChannel = fileOutputStream.getChannel();
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    }

    private download2(URL downloadUrl, File downloadDestFile){
        String fileName = downloadDestFile.getName()

        HttpURLConnection httpConnection = (HttpURLConnection) downloadUrl.openConnection();
        httpConnection.setRequestMethod("HEAD");
        long removeFileSize = httpConnection.getContentLengthLong();

        long existingFileSize = outputFile.length();
        if (existingFileSize < fileLength) {
            httpConnection.setRequestProperty(
                    "Range",
                    "bytes=" + existingFileSize + "-" + fileLength
            );
        }

        OutputStream os = new FileOutputStream(fileName, true);
    }

}
