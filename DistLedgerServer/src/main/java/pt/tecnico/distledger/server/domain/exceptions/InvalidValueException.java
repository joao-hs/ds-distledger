package pt.tecnico.distledger.server.domain.exceptions;

public class InvalidValueException extends IllegalOperationException {
    public InvalidValueException(int value) {
        super("The amount "+ value + "set to transfer is not a valid transferable amount.");
    }
}
