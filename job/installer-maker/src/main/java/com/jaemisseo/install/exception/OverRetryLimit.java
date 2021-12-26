package com.jaemisseo.install.exception;

public class OverRetryLimit extends Exception {

    OverRetryLimit(){
        super();
    }

    OverRetryLimit(String message){
        super(message);
    }

    OverRetryLimit(Throwable cause){
        super(cause);
    }

    OverRetryLimit(String message, Throwable cause){
        super(message, cause);
    }

}