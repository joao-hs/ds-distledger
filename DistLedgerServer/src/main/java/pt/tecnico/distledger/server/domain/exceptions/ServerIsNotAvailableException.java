package pt.tecnico.distledger.server.domain.exceptions;

public class ServerIsNotAvailableException extends Exception {
    public ServerIsNotAvailableException() {
        super("The server is not available to perform this operation.");
    }
}
