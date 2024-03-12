package pt.tecnico.distledger.server.domain.exceptions;

public class AccountAlreadyExistsException extends IllegalOperationException {
    public AccountAlreadyExistsException(String accountName) {
        super("There is already an account with the name \"" + accountName + "\"");
    }
}
