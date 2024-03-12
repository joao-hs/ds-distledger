package pt.tecnico.distledger.server.domain.exceptions;

public class TransferToSelfException extends IllegalOperationException {
    public TransferToSelfException() {
        super("Transfers must be between different accounts");
    }
}
