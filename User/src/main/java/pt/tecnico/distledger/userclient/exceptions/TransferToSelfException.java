package pt.tecnico.distledger.userclient.exceptions;

public class TransferToSelfException extends Exception{
    public TransferToSelfException() {
        super("Transfers must be between different accounts");
    }
}
