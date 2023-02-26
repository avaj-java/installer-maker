package com.jaemisseo.hoya.task

import com.jaemisseo.hoya.bean.FileSetup
import jaemisseo.man.FileMan
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import org.apache.commons.io.FileUtils
import org.apache.commons.codec.binary.Base64;
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel

@Task
@TerminalValueProtocol(['url', 'to'])
class Download extends TaskHelper{

    @Value(name='url', required=true)
    List<String> urls

    @Value(name='to', filter='getFilePathList')
    List<String> destPathList

    @Value(name='auth')
    String userAndColonAndPassword_encoded

    @Value(name='auth-raw')
    String userAndColonAndPassword_raw

    @Value(name='header')
    String headerNote

    @Value(name='connectionTimeout')
    Integer connectionTimeout = 30_000

    @Value(name='readTimeout')
    Integer readTimeout = 30_000

    @Value
    FileSetup fileSetup



    @Override
    Integer run(){

        //- Header - Others..
        Map<String, String> headers = makeHeader(headerNote)
        //- Header - Basic-Authorization
        Map<String, String> headersWithBasicAuth = makeHeaderAsBasicAuth(userAndColonAndPassword_encoded, userAndColonAndPassword_raw) ?: [:]

        headers.putAll(headersWithBasicAuth)
        
        //- Do Download
        urls.each{ String sourceFilePath ->
            destPathList.each{ String destFilePath ->

                downloadFile(sourceFilePath, destFilePath, headers)

            }
        }

        return STATUS_TASK_DONE
    }
    
    private static Map<String, String> makeHeader(String headerNote){
        Map<String, String> headers = [:]
        //TODO: implements..
        return headers;
    }
    
    private static Map<String, String> makeHeaderAsBasicAuth(String userAndColonAndPassword_encoded, String userAndColonAndPassword_raw){
        Map<String, String> headers = [:]
        
        if (userAndColonAndPassword_encoded != null && !userAndColonAndPassword_encoded.isEmpty()){
            headers.put("Authorization", makeBasicAuthValue(userAndColonAndPassword_encoded))
        }else if (userAndColonAndPassword_raw != null && !userAndColonAndPassword_raw.isEmpty()) {
            String[] userAndColonAndPasswordArray = userAndColonAndPassword_raw?.split(":")
            String user = null
            String pw = null
            if (userAndColonAndPasswordArray != null){
                if (userAndColonAndPasswordArray.length == 1){
                    user = userAndColonAndPasswordArray[0]
                }else if (userAndColonAndPasswordArray.length == 2){
                    user = userAndColonAndPasswordArray[0]
                    pw = userAndColonAndPasswordArray[1]
                }
            }
            headers.put("Authorization", makeBasicAuthValue(user, pw))
        }
        
        return headers
    }
    

    private void downloadFile(String sourceFilePath, String destFilePath, Map<String, String> headers){
        //0. Log
        java.lang.System.out.println( "  > Download distribution");
        java.lang.System.out.println( "    - from: ${sourceFilePath}" );
        java.lang.System.out.println( "    -   to: ${destFilePath}" );

        //1. Validate
        FileMan.checkFile(destFilePath)

        //2. Mkdir
        FileMan.autoMkdirs(destFilePath)

        //3. Download
        URL url = new URL(sourceFilePath)
        File dest = new File(destFilePath)
        download(url, dest, headers)

//        downloadAsNio(url, dest)
//        FileMan.copy(sourceFilePath, destFilePath, fileSetup)
//        downloadAsFileUtils(url, dest)
    }



    public static void download(URL url, File downloadDestFile){
        download(url, downloadDestFile, null)
    }

    public static void download(URL url, File downloadDestFile, Map<String, String> headers){
        try {
            sun.net.www.protocol.http.HttpURLConnection connection = (sun.net.www.protocol.http.HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");

            if (headers != null){
                headers.each{String key, String v -> connection.setRequestProperty(key, v) }
            }

            connectionToFile(connection, downloadDestFile)
            println("Finished");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String makeBasicAuthValue(String username, String password){
        String plainCreds = username+":"+password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String encodedValue = new String(base64CredsBytes);
        return makeBasicAuthValue(encodedValue)
    }

    public static String makeBasicAuthValue(String basicAuthValue){
        String completeValue = basicAuthValue.startsWith("Basic ")
                                    ? basicAuthValue
                                    : new StringBuilder().append("Basic ").append(basicAuthValue).toString();

        return completeValue
    }

    public static void connectionToFile(sun.net.www.protocol.http.HttpURLConnection connection, File downloadDestFile){
        InputStream is = connection.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int ByteRead, ByteWritten=0;

        while ((ByteRead = is.read(buf)) != -1) {
            out.write(buf, 0, ByteRead);
            ByteWritten += ByteRead;
        }

        out.close();
        isr.close();
        byte[] response = out.toByteArray();

        FileOutputStream fos = new FileOutputStream(downloadDestFile);
        fos.write(response);
        fos.close();
    }





    private void download2(URL downloadUrl, File downloadDestFile){
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

    private void downloadAsFileUtils(URL downloadUrl, File downloadDestFile) throws IOException {
        downloadAsFileUtils(downloadUrl, downloadDestFile, this.connectionTimeout, this.readTimeout);
    }

    private void downloadAsFileUtils(URL downloadUrl, File downloadDestFile, int connectionTimeout, int readTimeout) throws IOException{
        FileUtils.copyURLToFile( downloadUrl, downloadDestFile, connectionTimeout, readTimeout );
    }

    private void downloadAsNio(URL downloadUrl, File downloadDestFile) throws IOException {
        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        FileChannel ch = null;
        try{
            rbc = Channels.newChannel(downloadUrl.openStream());
            fos = new FileOutputStream(downloadDestFile);
            ch = fos.getChannel();
            ch.transferFrom(rbc, 0, Long.MAX_VALUE);
        }catch(Exception e){
            throw e
        }finally{
            if (ch != null)
                ch.close()
            if (fos != null)
                fos.close()
            if (rbc != null)
                rbc.close()
        }
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

}
