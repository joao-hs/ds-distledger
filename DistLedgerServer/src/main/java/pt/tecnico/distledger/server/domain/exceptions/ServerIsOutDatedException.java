package pt.tecnico.distledger.server.domain.exceptions;

public class ServerIsOutDatedException extends Exception {
    public ServerIsOutDatedException() {
        super("The server is outdated and unable to perform this operation.");
    }
}
