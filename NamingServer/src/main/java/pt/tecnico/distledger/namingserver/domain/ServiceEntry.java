package pt.tecnico.distledger.namingserver.domain;

import pt.tecnico.distledger.namingserver.domain.exceptions.ServerAlreadyExistsException;
import pt.tecnico.distledger.namingserver.domain.exceptions.ServerDoesntExistException;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Address;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceEntry {
    ServiceName serviceName;
    List<ServerEntry> servers = new ArrayList<>();

    public ServiceEntry(ServiceName serviceName){
        this.serviceName = serviceName;
    }

    public ServiceName getServiceName() {
        return serviceName;
    }

    public void setServiceName(ServiceName serviceName) {
        this.serviceName = serviceName;
    }

    public List<ServerEntry> getServers(){
        return servers;
    }

    public void setServers(List<ServerEntry> servers) {
        this.servers = servers;
    }

    public void addServer(ServerEntry serverEntry) throws ServerAlreadyExistsException{
        List<ServerEntry> ser = servers.stream().filter(se -> se.equals(serverEntry)).collect(Collectors.toList());
        if(!ser.isEmpty()){
            throw new ServerAlreadyExistsException(serverEntry, this.serviceName);
        }
        servers.add(serverEntry);
    }

    public void removeServer(Address address) throws ServerDoesntExistException{
        ServerEntry server = null;
        for(ServerEntry se : servers){
            if (address.getHost().equals(se.getAddress().getHost()) 
            && address.getPort() == se.getAddress().getPort()){
                server = se;
            }
        }
        if(server == null){
            throw new ServerDoesntExistException(address, this.serviceName);
        }
        servers.remove(server);
    }

    public List<Address> getServerAddressByQualifier(ServerQualifier serverQualifier){
        if(serverQualifier == ServerQualifier.ALL){
            return servers.stream().map(se -> se.getAddress())
                    .collect(Collectors.toList());
        }
        return servers.stream().filter(se -> se.getServerQualifier().equals(serverQualifier)).map(se -> se.getAddress())
                .collect(Collectors.toList());
    }


}
