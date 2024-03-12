package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Address;


import java.util.List;

public class UserNamingServerService {
    private final ManagedChannel channel;
    private NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;

    public UserNamingServerService(String host, int port){
        // Channel is the abstraction to connect to a service endpoint.
        // Let us use plaintext communication because we do not have certificates.
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

        // It is up to the client to determine whether to block the call.
        // Here we create a blocking stub, but an async stub,
        // or an async stub with Future are always possible.
        stub = NamingServerServiceGrpc.newBlockingStub(channel);
    }

    public List<Address> lookup(NamingServer.ServerQualifier serverQualifier){
        NamingServer.LookupRequest request = NamingServer.LookupRequest.newBuilder()
                .setServiceName(NamingServer.ServiceName.DISTLEDGER)
                .setServerQualifier(serverQualifier)
                .build();
        NamingServer.LookupResponse response = stub.lookup(request);
        return response.getAddressList();
    }
}
