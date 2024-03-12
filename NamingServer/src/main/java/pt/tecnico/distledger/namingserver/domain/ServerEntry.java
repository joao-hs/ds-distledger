package pt.tecnico.distledger.namingserver.domain;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Address;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;

public class ServerEntry {
    ServerQualifier serverQualifier;
    Address address;

    public ServerEntry(ServerQualifier serverQualifier, Address address){
        this.serverQualifier = serverQualifier;
        this.address = address;
    }

    public ServerQualifier getServerQualifier(){
        return serverQualifier;
    }

    public void setServerQualifier(ServerQualifier serverQualifier){
        this.serverQualifier = serverQualifier;
    }

    public Address getAddress(){
        return address;
    }

    public void setAddress(Address address){
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof ServerEntry) {
            ServerEntry se = (ServerEntry) o;
            return this.serverQualifier.equals(se.getServerQualifier()) &&
                this.address.getHost().equals(se.getAddress().getHost()) &&
                this.address.getPort() == se.getAddress().getPort();
        }
        return false;
    }
}
