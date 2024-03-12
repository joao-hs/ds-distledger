package pt.tecnico.distledger.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ReplicaManager;
import pt.tecnico.distledger.server.domain.exceptions.*;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc.AdminServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc.UserServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase;
import pt.ulisboa.tecnico.distledger.utils.VectorClock;

import java.util.ArrayList;
import java.util.List;

import static io.grpc.Status.NOT_FOUND;
import static io.grpc.Status.UNAVAILABLE;

public class ServiceImpl {
    private final ReplicaManager replicaManager;

    public ServiceImpl(ReplicaManager replicaManager) {
        this.replicaManager = replicaManager;
    }

    public class UserServiceImpl extends UserServiceImplBase {
        @Override
        public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
            try {
                BalanceResponse response = replicaManager.balance(request.getUserId(), new VectorClock(request.getPrev().getTsList()));
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (AccountDoesntExistException adee) {
                responseObserver.onError(NOT_FOUND.withDescription(adee.getMessage()).asRuntimeException());
            } catch (ServerIsNotAvailableException | ServerIsOutDatedException sinae) {
                responseObserver.onError(UNAVAILABLE.withDescription(sinae.getMessage()).asRuntimeException());
            }
        }

        @Override
        public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
            try {
                CreateOp createOp = replicaManager.registerCreateAccountRequest(request.getUserId(), new VectorClock(request.getPrev().getTsList()));
                CreateAccountResponse response = CreateAccountResponse.newBuilder().setOperationTS(replicaManager.getNewTS(createOp).proto()).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                replicaManager.executeOperation(createOp);
            } catch (ServerIsNotAvailableException sinae) {
                responseObserver.onError(UNAVAILABLE.withDescription(sinae.getMessage()).asRuntimeException());
            }
        }

        /* 
        ! This method is not used in the final version of the project.
        @Override
        public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
            debug("Remote Procedure Call: deleteAccount");
            debug("* userId " + "(" + request.getUserId().getClass().getCanonicalName() + "): " + request.getUserId());
            try {
                stateSynchronizer.synchronizedDeleteAccount(state, request.getUserId());
                debug("**DONE**");
                DeleteAccountResponse response = DeleteAccountResponse.getDefaultInstance();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (AccountDoesntExistException adee) {
                responseObserver.onError(NOT_FOUND.withDescription(adee.getMessage()).asRuntimeException());
            } catch (BalanceIsNotZeroException binze) {
                responseObserver.onError(FAILED_PRECONDITION.withDescription(binze.getMessage()).asRuntimeException());
            } catch (ServerIsNotAvailableException sinae) {
                responseObserver.onError(UNAVAILABLE.withDescription(sinae.getMessage()).asRuntimeException());
            } catch (CantDeleteBrokerException cdbe) {
                responseObserver.onError(INVALID_ARGUMENT.withDescription(cdbe.getMessage()).asRuntimeException());
            } catch (IllegalOperationException ioe) {
                responseObserver.onError(INTERNAL.withDescription(ioe.getMessage()).asRuntimeException());
            }
        }
        */

        @Override
        public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {
            try {
                TransferOp transferOp = replicaManager.registerTransferToRequest(request.getAccountFrom(), request.getAccountTo(), request.getAmount(), new VectorClock(request.getPrev().getTsList()));
                TransferToResponse response = TransferToResponse.newBuilder().setOperationTS(replicaManager.getNewTS(transferOp).proto()).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                replicaManager.executeOperation(transferOp);
            } catch (ServerIsNotAvailableException sinae) {
                responseObserver.onError(UNAVAILABLE.withDescription(sinae.getMessage()).asRuntimeException());
            }
        }
    }

    public class AdminServiceImpl extends AdminServiceImplBase {
        @Override
        public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
            replicaManager.activate();
            ActivateResponse response = ActivateResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
            replicaManager.deactivate();
            DeactivateResponse response = DeactivateResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
            try {
                replicaManager.gossip();
                responseObserver.onNext(GossipResponse.getDefaultInstance());
                responseObserver.onCompleted();
            } catch (ServerIsNotAvailableException sinae) {
                responseObserver.onError(UNAVAILABLE.withDescription(sinae.getMessage()).asRuntimeException());
            }
        }

        @Override
        public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {
            getLedgerStateResponse response = replicaManager.getLedgerState();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    public class CrossServerServiceImpl extends DistLedgerCrossServerServiceImplBase {
        private Operation createOperation(DistLedgerCommonDefinitions.Operation operation) {
            switch (operation.getType()) {
                case OP_CREATE_ACCOUNT:
                    return new CreateOp(operation.getUserId(), new VectorClock(operation.getPrev().getTsList()), new VectorClock(operation.getOperationTS().getTsList()));
                /* case OP_DELETE_ACCOUNT:
                    return new DeleteOp(operation.getUserId()); */
                case OP_TRANSFER_TO:
                    return new TransferOp(operation.getUserId(), operation.getDestUserId(), operation.getAmount(), new VectorClock(operation.getPrev().getTsList()), new VectorClock(operation.getOperationTS().getTsList()));
                default:
                    return null;
            }
        }

        private List<Operation> createAllOperations(List<DistLedgerCommonDefinitions.Operation> operations) {
            List<Operation> allOperations = new ArrayList<>();
            Operation op;
            for (DistLedgerCommonDefinitions.Operation operation : operations) {
                op = createOperation(operation);
                if (op != null)
                    allOperations.add(op);
            }
            return allOperations;
        }

        @Override
        public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
            replicaManager.receiveState(createAllOperations(request.getState().getLedgerList()), new VectorClock(request.getReplicaTS().getTsList()));
            PropagateStateResponse response = PropagateStateResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void propagateStatePull(PropagateStatePullRequest request, StreamObserver<PropagateStatePullResponse> responseObserver) {
            try {
                replicaManager.gossip(request.getAddress());
                PropagateStatePullResponse response = PropagateStatePullResponse.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (ServerIsNotAvailableException sinae) {
                responseObserver.onError(UNAVAILABLE.withDescription(sinae.getMessage()).asRuntimeException());
            }
        }
    }
}
