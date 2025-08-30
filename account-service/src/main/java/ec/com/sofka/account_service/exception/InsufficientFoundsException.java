package ec.com.sofka.account_service.exception;


public class InsufficientFoundsException extends RuntimeException {

    public InsufficientFoundsException(String message) {
        super(message);
    }

}