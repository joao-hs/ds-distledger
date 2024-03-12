package pt.tecnico.distledger.userclient.exceptions;

import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.ServerQualifier;

public class ServerNotAvailableException extends Exception{
    public ServerNotAvailableException(ServerQualifier qualifier) {
        super("There are no available servers with the qualifier: \"" + qualifier + "\"");
    }
}
