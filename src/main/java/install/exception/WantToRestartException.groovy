package install.exception

class WantToRestartException extends Exception {


    WantToRestartException() {
    }

    WantToRestartException(String message) {
        super(message)
    }

    WantToRestartException(String message, Throwable cause) {
        super(message, cause)
    }

    WantToRestartException(Throwable cause) {
        super(cause)
    }

}
