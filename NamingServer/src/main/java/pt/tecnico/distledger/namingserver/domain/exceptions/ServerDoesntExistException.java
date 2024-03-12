package pt.tecnico.distledger.namingserver.domain.exceptions;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Address;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.ServiceName;

public class ServerDoesntExistException extends Exception{
    public ServerDoesntExistException(Address address, ServiceName serviceName) {
        super("The server: \"Host: " + address.getHost() +
                " Port: " + address.getPort() + "\" is not associated to the service:\"" +
                serviceName + "\"");
    }
}