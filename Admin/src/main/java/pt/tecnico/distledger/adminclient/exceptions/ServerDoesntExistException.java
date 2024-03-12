package pt.tecnico.distledger.adminclient.exceptions;

import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.ServerQualifier;

public class ServerDoesntExistException extends Exception{
    public ServerDoesntExistException(ServerQualifier qualifier) {
        super("There are no servers with the qualifier: \"" + qualifier + "\"");
    }
}
