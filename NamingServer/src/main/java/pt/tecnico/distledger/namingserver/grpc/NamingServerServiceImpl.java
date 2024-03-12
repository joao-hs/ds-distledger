package pt.tecnico.distledger.namingserver.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc.NamingServerServiceImplBase;
import pt.tecnico.distledger.namingserver.domain.*;
import pt.tecnico.distledger.namingserver.domain.exceptions.ServerAlreadyExistsException;
import pt.tecnico.distledger.namingserver.domain.exceptions.ServerDoesntExistException;
import pt.tecnico.distledger.namingserver.domain.exceptions.ServiceNameDoesntExistException;

import static io.grpc.Status.NOT_FOUND;
import static io.grpc.Status.ALREADY_EXISTS;

public class NamingServerServiceImpl extends NamingServerServiceImplBase {
    private NamingServer namingServer = new NamingServer();
    

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        try{
            namingServer.register(request.getServiceName(), request.getServerQualifier(), request.getAddress());
            RegisterResponse response = RegisterResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }catch(ServerAlreadyExistsException saee){
            responseObserver.onError(ALREADY_EXISTS.withDescription(saee.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
        System.out.println(request.getServerQualifier());
        LookupResponse response = LookupResponse.newBuilder()
                .addAllAddress(namingServer.lookup(request.getServiceName(), request.getServerQualifier())).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    
    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        try{
            namingServer.delete(request.getServiceName(), request.getAddress());
            DeleteResponse response = DeleteResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }catch(ServerDoesntExistException sdee){
            responseObserver.onError(NOT_FOUND.withDescription(sdee.getMessage()).asRuntimeException());
        }catch(ServiceNameDoesntExistException sndee){
            responseObserver.onError(NOT_FOUND.withDescription(sndee.getMessage()).asRuntimeException());
        }
    }

}

