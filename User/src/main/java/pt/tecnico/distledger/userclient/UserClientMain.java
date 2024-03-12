package pt.tecnico.distledger.userclient;


import pt.tecnico.distledger.userclient.grpc.UserNamingServerService;

public class UserClientMain {
    public static void main(String[] args) {

        System.out.println(UserClientMain.class.getSimpleName());

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);

        CommandParser parser = new CommandParser(new UserNamingServerService(host, port));
        parser.parseInput();
        System.exit(0);
    }
}
