package pt.tecnico.distledger.server.grpc;

import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Address;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;

public class NamingServerService {
    private final ManagedChannel channel;
    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;

    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    private static void debug(String debugMessage) {
        if (DEBUG_FLAG) {
            System.err.print("[DEBUG] ");
            System.err.println(debugMessage);
        }
    }

    public NamingServerService(String host, int port){
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = NamingServerServiceGrpc.newBlockingStub(channel);
    }

    public void register(ServiceName serviceName, ServerQualifier serverQualifier, Address address) {
        RegisterRequest request = RegisterRequest.newBuilder()
                .setServiceName(serviceName)
                .setServerQualifier(serverQualifier)
                .setAddress(address)
                .build();
        debug("Calling remote procedure: register");
        debug("* serviceName(" + serviceName + "), serverQualifier(" + serverQualifier + "),\n"+ address);
        RegisterResponse response = stub.register(request);
        debug("OK");
        debug(response.toString());
    }

    public List<Address> lookup(ServiceName serviceName) {
        LookupRequest request = LookupRequest.newBuilder()
                .setServiceName(serviceName)
                .setServerQualifier(ServerQualifier.ALL)
                .build();
        try {
            debug("Calling remote procedure: lookup");
            LookupResponse response = stub.lookup(request);
            debug("OK");
            debug(response.toString());
            return response.getAddressList();
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }
        return null;
    }

    public void delete(ServiceName serviceName, Address address) {
        DeleteRequest request = DeleteRequest.newBuilder()
                .setServiceName(serviceName)
                .setAddress(address)
                .build();
        try {
            debug("Calling remote procedure: delete");
            debug("* serviceName(" + serviceName + "),\n"+ address);
            DeleteResponse response = stub.delete(request);

            debug("OK");
            debug(response.toString());
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }
    }

    public void close() {
        channel.shutdown();
    }
}
