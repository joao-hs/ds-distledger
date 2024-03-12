package pt.tecnico.distledger.server.domain.exceptions;

public class CantDeleteBrokerException extends IllegalOperationException {
    public CantDeleteBrokerException() {
        super("The account broker cannot be deleted");
    }
}