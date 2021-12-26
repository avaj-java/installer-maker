package com.jaemisseo.install.exception;

public class FailedToExtractDistributionException extends Exception {

    FailedToExtractDistributionException(){
        super();
    }

    FailedToExtractDistributionException(String message){
        super(message);
    }

    FailedToExtractDistributionException(Throwable cause){
        super(cause);
    }

    FailedToExtractDistributionException(String message, Throwable cause){
        super(message, cause);
    }

}