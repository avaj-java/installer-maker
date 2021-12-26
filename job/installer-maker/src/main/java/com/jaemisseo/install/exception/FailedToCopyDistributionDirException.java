package com.jaemisseo.install.exception;

public class FailedToCopyDistributionDirException extends Exception {

    FailedToCopyDistributionDirException(){
        super();
    }

    FailedToCopyDistributionDirException(String message){
        super(message);
    }

    FailedToCopyDistributionDirException(Throwable cause){
        super(cause);
    }

    FailedToCopyDistributionDirException(String message, Throwable cause){
        super(message, cause);
    }

}