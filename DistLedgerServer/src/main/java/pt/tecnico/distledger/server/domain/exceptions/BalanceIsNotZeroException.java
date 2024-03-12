package pt.tecnico.distledger.server.domain.exceptions;

public class BalanceIsNotZeroException extends IllegalOperationException {
    public BalanceIsNotZeroException(String accountName) {
        super("The account with the name \"" + accountName + 
            "\" still has funds left.");
    }
}
