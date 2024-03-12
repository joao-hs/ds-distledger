package pt.tecnico.distledger.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.server.domain.exceptions.ServerIsNotAvailableException;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Address;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStatePullRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.VectorClock;

public class CrossServerService {
    private final ManagedChannel channel;
    private final DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub;

    public CrossServerService(String host, int port){
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);
    }

    public void propagateState(LedgerState ledger, VectorClock replicaTS) throws ServerIsNotAvailableException {
        PropagateStateRequest request = PropagateStateRequest.newBuilder()
            .setState(ledger)
            .setReplicaTS(replicaTS)
            .build();
        try {
            stub.propagateState(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode().equals(Status.UNAVAILABLE.getCode())) {
                throw new ServerIsNotAvailableException();
            } else {
                throw e;
            }
        }
    }

    public void propagateStatePull(Address ownAddress) throws ServerIsNotAvailableException {
        PropagateStatePullRequest request = PropagateStatePullRequest.newBuilder()
            .setAddress(ownAddress)
            .build();
        try {
            stub.propagateStatePull(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode().equals(Status.UNAVAILABLE.getCode())) {
                throw new ServerIsNotAvailableException();
            } else {
                throw e;
            }
        }
    }

}
