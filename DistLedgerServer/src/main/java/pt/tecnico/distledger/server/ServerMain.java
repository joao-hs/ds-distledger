package pt.tecnico.distledger.server;

import java.io.IOException;

import io.grpc.*;
import pt.tecnico.distledger.server.grpc.NamingServerService;
import pt.tecnico.distledger.server.domain.ReplicaManager;
import pt.tecnico.distledger.server.domain.exceptions.ServerIsNotAvailableException;
import pt.tecnico.distledger.server.domain.exceptions.ServerQualifierNotValidException;
import pt.tecnico.distledger.server.grpc.ServiceImpl;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Address;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;

public class ServerMain {

    public static void main(String[] args) throws IOException, InterruptedException {

        final String host = "localhost";
        final int namingServerServicePort = 5001;

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length != 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<host> <port>");
            return;
        }

        final int port = Integer.parseInt(args[0]);
        final String serverQualifier = args[1];


        
        try{
            NamingServerService namingServerService = new NamingServerService(host, namingServerServicePort);
            // Send register request to naming server service
            ServiceName serviceName = ServiceName.DISTLEDGER;
            Address address = Address.newBuilder().setHost(host).setPort(port).build();
            ServerQualifier sq = getServerQualifier(serverQualifier);
            namingServerService.register(serviceName, sq, address);

            ReplicaManager replicaManager = new ReplicaManager(namingServerService, Address.newBuilder().setHost(host).setPort(port).build(), sq);

            ServiceImpl serviceImpl = new ServiceImpl(replicaManager);
            final BindableService userImpl = serviceImpl.new UserServiceImpl();
            final BindableService adminImpl = serviceImpl.new AdminServiceImpl();
            final BindableService crossServerImpl = serviceImpl.new CrossServerServiceImpl();

            // Create a new server to listen on port
            Server server = ServerBuilder.forPort(port).addService(userImpl).addService(adminImpl).addService(crossServerImpl).build();
            // Start the server
            server.start();
            // Pulls state from an available server
            replicaManager.propagateStatePull();


            // Exit server on SIGINT (ctrl C)
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    System.out.println("Running Shutdown Hook");
                    try {
                        replicaManager.gossip();
                    } catch (ServerIsNotAvailableException e) {
                        System.out.println("Server is not available, cannot gossip. Unsynchronized changes will be lost");
                    }
                    namingServerService.delete(serviceName, address);
                    namingServerService.close();
                    server.shutdown();
                }
            });

            // Server threads are running in the background.
            System.out.println("Server started");
            // Server is terminated when enter is pressed.
            System.out.println("Press enter to shutdown");
            System.in.read();
            System.out.println("System shutting down on enter pressed");
        }catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        } catch (ServerQualifierNotValidException sqnve) {
            System.out.println("Caught exception with description: " +
                    sqnve.getMessage());
        }
        System.exit(0);
    }
    
    public static ServerQualifier getServerQualifier(String qualifier) throws ServerQualifierNotValidException{
        if (qualifier.equals("A")){
            return ServerQualifier.PRIMARY;
        }
        else if(qualifier.equals("B")){
            return ServerQualifier.SECONDARY;
        }
        else if(qualifier.equals("C")){
            return ServerQualifier.TERTIARY;
        }
        throw new ServerQualifierNotValidException(qualifier);
    }

}

