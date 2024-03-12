package pt.tecnico.distledger.namingserver.domain;

import pt.tecnico.distledger.namingserver.domain.exceptions.ServerAlreadyExistsException;
import pt.tecnico.distledger.namingserver.domain.exceptions.ServerDoesntExistException;
import pt.tecnico.distledger.namingserver.domain.exceptions.ServiceNameDoesntExistException;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Address;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class NamingServer {
    private ConcurrentHashMap<ServiceName, ServiceEntry> serviceServers = new ConcurrentHashMap<>();
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    private static void debug(String debugMessage) {
        if (DEBUG_FLAG) {
            System.err.print("[DEBUG] ");
            System.err.println(debugMessage);
        }
    }

    public synchronized void register(ServiceName serviceName, ServerQualifier serverQualifier, Address address) 
        throws ServerAlreadyExistsException{
        debug("Calling remote procedure: register");
        debug("* serviceName(" + serviceName + "),serverQualifier(" + serverQualifier + "),\n" + address );
        ServiceEntry entry = serviceServers.get(serviceName);
        ServerEntry server = new ServerEntry(serverQualifier, address);
        if(entry == null){
            entry = new ServiceEntry(serviceName);
            serviceServers.put(serviceName, entry);
        }
        entry.addServer(server);
        debug("OK");
    }

    public List<Address> lookup(ServiceName serviceName, ServerQualifier serverQualifier){
        debug("Calling remote procedure: lookup");
        debug("* serviceName(" + serviceName + "),serverQualifier(" + serverQualifier + ")" );
        ServiceEntry entry = serviceServers.get(serviceName);
        if(entry == null){
            return new ArrayList<Address>();
        }
        debug("OK");
        debug(entry.getServerAddressByQualifier(serverQualifier).toString());
        return entry.getServerAddressByQualifier(serverQualifier);
    }

    public synchronized void delete(ServiceName serviceName, Address address)
        throws ServerDoesntExistException, ServiceNameDoesntExistException{
        debug("Calling remote procedure: delete");
        debug("* serviceName(" + serviceName + "),\n" + address);
        ServiceEntry entry = serviceServers.get(serviceName);
        if (entry == null){
            throw new ServiceNameDoesntExistException(serviceName);
        }
        debug(entry.toString());
        entry.removeServer(address);
        debug("OK");
    }

}
