package pt.tecnico.distledger.server.domain.exceptions;

public class IllegalOperationException extends Exception {
    IllegalOperationException() {
        super();
    }

    public IllegalOperationException(String message) {
        super(message);
    }
}
