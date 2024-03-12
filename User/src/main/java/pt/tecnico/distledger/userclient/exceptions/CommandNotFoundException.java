package pt.tecnico.distledger.userclient.exceptions;

public class CommandNotFoundException extends Exception{
    public CommandNotFoundException(String line) {
        super("There are no commands matching: \"" + line + "\"");
    }
}
