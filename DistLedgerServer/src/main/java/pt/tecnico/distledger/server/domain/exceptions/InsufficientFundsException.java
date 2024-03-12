package pt.tecnico.distledger.server.domain.exceptions;

public class InsufficientFundsException extends IllegalOperationException {
    public InsufficientFundsException(String accountName) {
        super("The account with the name \"" + accountName + "\" doesn't have" +
            " sufficient funds to perform this operation.");
    }
    
}
