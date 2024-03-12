package pt.tecnico.distledger.namingserver.domain.exceptions;

import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.ServiceName;

public class ServiceNameDoesntExistException extends Exception{
    public ServiceNameDoesntExistException(ServiceName serviceName) {
        super("The service with the name \""+ serviceName + "\" does not exist");
    }
}
