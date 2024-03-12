package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;

public class AdminService {

    private final ManagedChannel channel;
    private AdminServiceGrpc.AdminServiceBlockingStub stub;

    public AdminService(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = AdminServiceGrpc.newBlockingStub(channel);
    }

    public ActivateResponse activate() {
        ActivateRequest request = ActivateRequest.newBuilder().build();
        return stub.activate(request);
    }

    public DeactivateResponse deactivate() {
        DeactivateRequest request = DeactivateRequest.newBuilder().build();
        return stub.deactivate(request);
    }

    public getLedgerStateResponse getLedgerState() {
        getLedgerStateRequest request = getLedgerStateRequest.newBuilder().build();
        return stub.getLedgerState(request);
    }

    public GossipResponse gossip() {
        GossipRequest request = GossipRequest.newBuilder().build();
        return stub.gossip(request);
    }
}
