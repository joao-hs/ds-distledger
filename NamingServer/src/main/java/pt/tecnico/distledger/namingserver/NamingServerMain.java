package pt.tecnico.distledger.namingserver;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.distledger.namingserver.grpc.NamingServerServiceImpl;

import java.io.IOException;

public class NamingServerMain {

    public static void main(String[] args) throws IOException, InterruptedException {

        final BindableService namingServerImpl = new NamingServerServiceImpl();

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(5001).addService(namingServerImpl).build();

        // Start the server
        server.start();

        // Server threads are running in the background.
        System.out.println("Server started");

        // Server is terminated when enter is pressed.
        System.out.println("Press enter to shutdown");
        System.in.read();
        server.shutdown();
    }

}
