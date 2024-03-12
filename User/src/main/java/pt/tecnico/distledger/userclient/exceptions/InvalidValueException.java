package pt.tecnico.distledger.userclient.exceptions;

public class InvalidValueException extends Exception {
    public InvalidValueException(int value) {
        super("The amount " + value + " set to transfer is not a valid transferable amount.");
    }
}
