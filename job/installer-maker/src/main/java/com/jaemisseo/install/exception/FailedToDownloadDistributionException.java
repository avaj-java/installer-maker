package com.jaemisseo.install.exception;

public class FailedToDownloadDistributionException extends Exception {

    FailedToDownloadDistributionException(){
        super();
    }

    FailedToDownloadDistributionException(String message){
        super(message);
    }

    FailedToDownloadDistributionException(Throwable cause){
        super(cause);
    }

    FailedToDownloadDistributionException(String message, Throwable cause){
        super(message, cause);
    }

}