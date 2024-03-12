package pt.tecnico.distledger.userclient.exceptions;

import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer;

public class NoAvailableServer extends Exception{
    public NoAvailableServer(NamingServer.ServerQualifier serverQualifier) {
        super("The are no available servers with serverQualifier \""+ serverQualifier + "\"");
    }
}
