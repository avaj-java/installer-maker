package com.jaemisseo.man

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * User: sujkim
 * Date: 16. 11. 2
 * Time: 오후 2:14
 * To change this template use File | Settings | File Templates.
 */
class SocketMan {

    ServerSocket serverSocket
    Socket socket
    DataInputStream dis
    DataOutputStream dos
    BufferedInputStream bis
    BufferedOutputStream bos

    int THREAD_CNT = 10
//    ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_CNT)
    ThreadPoolExecutor threadPool = new ThreadPoolExecutor(THREAD_CNT, THREAD_CNT, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    boolean modeIndependent = false
    boolean modeAutoClose = true
    String charset
    int timeout = 5000

    String receivedMsg

    SocketMan setModeIndependent(boolean modeIndependent) {
        this.modeIndependent = modeIndependent
        return this
    }

    SocketMan setModeAutoClose(boolean modeAutoClose) {
        this.modeAutoClose = modeAutoClose
        return this
    }

    SocketMan setCharset(String charset) {
        this.charset = charset
        return this
    }

    SocketMan setTimeout(int timeout) {
        this.timeout = timeout
        return this
    }


    /**
     * Open  Server
     * @param port
     * @return
     */
    SocketMan server(int port, Closure closure){
        if (modeIndependent){
            new Thread(new Runnable(){
                @Override
                void run(){
                    runServer(port, closure)
                }
            }).start()
        }else{
            runServer(port, closure)
        }
       return this
    }


    SocketMan echoServer(int port){
        echoServer(port, {})
    }

    SocketMan echoServer(int port, Closure closure){
        return server(port){
            int recevableBytes = 2048
            Socket socket = it.socket
            String client = socket.getLocalAddress()
            InputStream is = socket.getInputStream()
            OutputStream os = socket.getOutputStream()

            // Receive
            int len = -1
            int data_size = 0
            byte[] data = new byte[recevableBytes]
            byte[] realBytes

            try{
                while ((len = is.read()) > 0) {
                    data[data_size] = (byte)len
                    data_size++
                    // 개행이면 while문을 빠져나감.
                    if (len == 0x0a) {
                        break
                    }
                }
            }catch(Exception e){
//                e.printStackTrace()
            }finally{
                if (data_size){
                    realBytes = new byte[data_size]
                    (0..data_size -1).each{
                        realBytes[it] = data[it]
                    }
                }
            }

            if (realBytes){
                String msg = (charset) ? new String(realBytes, charset) : new String(realBytes)
                println "[${client}] -> ${msg}"
                receivedMsg = msg
                closure(this)

                // Send
                byte[] sendBytes = (charset) ? msg.getBytes(charset) : msg.getBytes()
                os.write(sendBytes)
                os.flush()
                println "[${client}] <- ${msg}"
            }else{
                println "[${client}] - send no data to Server "
            }
        }
    }





    SocketMan runServer(int port, Closure closure){
        // Open Server
        try{
            serverSocket = new ServerSocket(port)
            println "[${serverSocket.getLocalSocketAddress()}] Server is running"
        }catch(IOException e){
            e.printStackTrace()
            throw e
        }finally{

        }

        // Circulation To Wait For Client
        while (true){

            // Wait client
            try{
                socket = serverSocket.accept()
            }catch(IOException e){
                e.printStackTrace()
            }finally{
            }

            // Create Thread
            threadPool.execute(new Thread(new Runnable(){
                @Override
                void run() {
                    String client = socket.getLocalAddress()
                    String logConnect = ""
                    String logPoolCount = ""
                    try{
                        // Log
                        logConnect = "[${client}] Connected to Server"
                        logPoolCount += "Thread Count in Pool: ${threadPool.getActiveCount()}"
                        if (threadPool.getQueue().size() != 0)
                            logPoolCount += " ...(wait: ${threadPool.getQueue().size()})"
                        println "${logConnect} (${logPoolCount})"
                        // Get stream
                        dis = new DataInputStream(socket.getInputStream())
                        dos = new DataOutputStream(socket.getOutputStream())
                        // Custom
                        closure(this)
                    }catch(Exception ex){
                        ex.printStackTrace()
                        throw ex
                    }finally{
                        if (modeAutoClose)
                            disconnect()
                        println "[${client}] Disconnected from Server"
                    }
                }
            }))

        }


        // Close Server
        try{
            threadPool.shutdown()
            serverSocket.close()
            println "[${serverSocket.getLocalSocketAddress()}] Server is down"
        }catch(IOException e){
            e.printStackTrace()
            throw e
        }finally{
        }

        return this
    }









    SocketMan connect(String ipPort, Closure closure){
        String[] ipPortArray = ipPort.split('[:]')
        return connect(ipPortArray[0], Integer.parseInt(ipPortArray[1]), closure)
    }



    /**
     * Connect to Server
     * @return
     */
    SocketMan connect(String ip, int port, Closure closure){
        Closure closureConnect = {
            socket = new Socket(ip, port)
            if (timeout)
                socket.setSoTimeout(timeout)
//            dis = new DataInputStream(socket.getInputStream())
//            dos = new DataOutputStream(socket.getOutputStream())
//            bis = new BufferedInputStream(socket.getInputStream())
//            bos = new BufferedOutputStream(socket.getOutputStream())
            closure(this)
        }
        try{
            if (modeIndependent){
                new Thread(new Runnable(){
                    @Override
                    void run(){
                        closureConnect()
                    }
                }).start()
            }else{
                closureConnect()
            }
        }catch(ConnectException ce){
            ce.printStackTrace()
            disconnect()
        }catch(IOException ie){
            ie.printStackTrace()
            disconnect()
        }catch(Exception e){
            e.printStackTrace()
            disconnect()
        }finally{
        }
        return this
    }

    SocketMan send(String msg){
        try{
            OutputStream os = socket.getOutputStream()
            byte[] bytes = (charset) ? msg.getBytes(charset) : msg.getBytes()
            os.write(bytes)
            os.flush()
            println "Message To [${socket.getInetAddress()}:${socket.port}]"
        }catch(ConnectException ce){
            ce.printStackTrace()
        }catch(IOException ie){
            ie.printStackTrace()
        }catch(Exception e){
            e.printStackTrace()
        }finally{
            if (modeAutoClose)
                disconnect()
        }
        return this
    }


    SocketMan receive(int recevableBytes){
        try{
            Socket socket = socket
            String charset = charset
            InputStream is = socket.getInputStream()

            int len = -1
            int data_size = 0
            byte[] data = new byte[recevableBytes]
            byte[] realBytes

            try{
                while ((len = is.read()) > 0) {
                    data[data_size] = (byte)len;
                    data_size++;
                    // 개행이면 while문을 빠져나감.
                    if (len == 0x0a) {
                        break;
                    }
                }
            }catch(Exception e){
//                e.printStackTrace()
            }finally{
                if (data_size){
                    realBytes = new byte[data_size]
                    (0..data_size -1).each{
                        realBytes[it] = data[it]
                    }
                }
            }

            if (realBytes){
                receivedMsg = (charset) ? new String(realBytes, charset) : new String(realBytes)
                println "Message From [${socket.getInetAddress()}:${socket.port}]"
            }

        }catch(ConnectException ce){
            ce.printStackTrace()
        }catch(IOException ie){
            ie.printStackTrace()
        }catch(Exception e){
            e.printStackTrace()
        }finally{
            if (modeAutoClose)
                disconnect()
        }
        return this
    }

    SocketMan read(def someVar){
        someVar = receivedMsg
        return this
    }



    SocketMan disconnect(){
        if (dos)
            dos.close()
        if (dis)
            dis.close()
        if (socket)
            socket.close()
        return this
    }

    String getMessage(){
        return receivedMsg
    }

}
