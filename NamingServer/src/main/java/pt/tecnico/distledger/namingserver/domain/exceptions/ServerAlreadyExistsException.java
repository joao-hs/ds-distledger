package pt.tecnico.distledger.namingserver.domain.exceptions;

import pt.tecnico.distledger.namingserver.domain.ServerEntry;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.ServiceName;

public class ServerAlreadyExistsException extends Exception{
    public ServerAlreadyExistsException(ServerEntry serverEntry, ServiceName serviceName) {
        super("The server: \"Host: " + serverEntry.getAddress().getHost() +
                " Port: " + serverEntry.getAddress().getPort() + "\" is already associated to the service:\"" +
                serviceName + "\"");
    }
}
