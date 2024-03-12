package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.VectorClock;

public class UserService {
    private final ManagedChannel channel;
    private UserServiceGrpc.UserServiceBlockingStub stub;


    public UserService(String host, int port){
        // Channel is the abstraction to connect to a service endpoint.
        // Let us use plaintext communication because we do not have certificates.
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

        // It is up to the client to determine whether to block the call.
        // Here we create a blocking stub, but an async stub,
        // or an async stub with Future are always possible.
        stub = UserServiceGrpc.newBlockingStub(channel);
    }


    public BalanceResponse balance(String userId, VectorClock prev){
        BalanceRequest request = BalanceRequest.newBuilder().setUserId(userId).setPrev(prev).build();
        return stub.balance(request);
    }

    public CreateAccountResponse createAccount(String userId, VectorClock prev) {
        CreateAccountRequest request = CreateAccountRequest.newBuilder().setUserId(userId).setPrev(prev).build();
        return stub.createAccount(request);
    }
/* 
    public DeleteAccountResponse deleteAccount(String userId, VectorClock prev) {
        DeleteAccountRequest request = DeleteAccountRequest.newBuilder().setUserId(userId).build();
        return stub.deleteAccount(request);   
    }
 */
    public TransferToResponse transferTo(String accountFrom, String accountTo, int value, VectorClock prev) {
        TransferToRequest request = TransferToRequest.newBuilder().setAccountFrom(accountFrom).setAccountTo(accountTo)
                .setAmount(value).setPrev(prev).build();
        return stub.transferTo(request);
    }
}
