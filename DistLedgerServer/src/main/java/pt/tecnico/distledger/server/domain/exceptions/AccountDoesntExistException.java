package pt.tecnico.distledger.server.domain.exceptions;

public class AccountDoesntExistException extends IllegalOperationException {
    public AccountDoesntExistException(String accountName) {
        super("There isn't an account with the name \"" + accountName + "\"");
    }
}
