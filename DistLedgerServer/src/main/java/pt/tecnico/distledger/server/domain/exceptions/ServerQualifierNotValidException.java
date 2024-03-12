package pt.tecnico.distledger.server.domain.exceptions;

public class ServerQualifierNotValidException extends Exception{
    public ServerQualifierNotValidException(String qualifier) {
        super("The server qualifier: \"" + qualifier + "\" is not valid");
    }
}