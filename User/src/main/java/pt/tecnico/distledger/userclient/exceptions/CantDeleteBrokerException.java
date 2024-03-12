package pt.tecnico.distledger.userclient.exceptions;

public class CantDeleteBrokerException extends Exception{
    public CantDeleteBrokerException() {
        super("Cannot delete broker account");
    }
}
