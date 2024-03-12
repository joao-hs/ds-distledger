package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.exceptions.*;
import pt.tecnico.distledger.userclient.grpc.UserNamingServerService;
import pt.tecnico.distledger.userclient.grpc.UserService;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Address;
import pt.ulisboa.tecnico.distledger.utils.VectorClock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String CREATE_ACCOUNT = "createAccount";
    //private static final String DELETE_ACCOUNT = "deleteAccount";
    private static final String TRANSFER_TO = "transferTo";
    private static final String BALANCE = "balance";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private final UserNamingServerService userNamingServerService;
    private Map<ServerQualifier, UserService> servers = new HashMap<>();
    private VectorClock prev = new VectorClock();

    public CommandParser(UserNamingServerService userNamingServerServiceServiceService) {
        this.userNamingServerService = userNamingServerServiceServiceService;
    }

    /** Set flag to true to print debug messages.
     * The flag can be set using the -Ddebug command line option. */
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    /** Helper method to print debug messages. */
    private static void debug(String debugMessage) {
        if (DEBUG_FLAG) {
            System.err.print("[DEBUG] ");
            System.err.println(debugMessage);
        }
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];
            
            try{
                switch (cmd) {
                    case CREATE_ACCOUNT:
                        this.createAccount(line);
                        break;
                    /* 
                    case DELETE_ACCOUNT:
                        this.deleteAccount(line);
                        break;
                    */
                    case TRANSFER_TO:
                        this.transferTo(line);
                        break;

                    case BALANCE:
                        this.balance(line);
                        break;

                    case HELP:
                        this.printUsage();
                        break;

                    case EXIT:
                        exit = true;
                        break;

                    default:
                        throw new CommandNotFoundException(cmd);
                }
            }catch (NumberFormatException nfe) {
                System.err.println("Invalid amount format");
            }catch (CommandNotFoundException cnf){
                System.err.println(cnf.getMessage());
            }catch (Exception e){
                System.err.println(e.getMessage());
            }
        }
        scanner.close();
    }

    private void createAccount(String line) throws ServerQualifierNotValidException, ServerNotAvailableException{
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String sq = split[1];
        String username = split[2];
        ServerQualifier server = getServerQualifier(sq);
        
        UserService userService = servers.get(server);
        if(userService == null){
            this.updateServers(server);
            userService = servers.get(server);
        }

        try{
            debug("Calling remote procedure: createAccount");
            debug("* userId " + "(" + username.getClass().getCanonicalName() + "): " + username);
            CreateAccountResponse response =userService.createAccount(username, prev.proto());
            prev.merge(response.getOperationTS().getTsList());
            System.out.println("OK");
        }catch(StatusRuntimeException e){
            //Erro -> o UserService que estava no mapa deixou de existir
            if(e.getStatus().getCode().equals(Status.UNAVAILABLE.getCode()) && e.getStatus().getCause() != null){
                //tenta fazer só uma vez mais ‘update’ do map dos server e executar a função pedida
                this.updateServers(server);
                userService = servers.get(server);
                debug("Calling remote procedure: createAccount");
                debug("* userId " + "(" + username.getClass().getCanonicalName() + "): " + username);
                CreateAccountResponse response = userService.createAccount(username, prev.proto());
                prev.merge(response.getOperationTS().getTsList());
                System.out.println("OK");
            }
            else{
                throw e;
            }
        }
    }
/* 
    private void deleteAccount(String line) throws CantDeleteBrokerException, ServerNotAvailableException, ServerQualifierNotValidException{
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String username = split[2];
        if (username.equals("broker")){
            throw new CantDeleteBrokerException();
        }

        String sq = split[1];
        ServerQualifier server = getServerQualifier(sq);

        UserService userService = servers.get(server);
        if(userService == null){
            this.updateServers(server);
            userService = servers.get(server);
        }

        try{
            debug("Calling remote procedure: deleteAccount");
            debug("* userId " + "(" + username.getClass().getCanonicalName() + "): " + username);
            DeleteAccountResponse response = userService.deleteAccount(username, prev.proto());
            prev.merge(response.getOperationTS().getTsList());
            System.out.println("OK");
        }catch(StatusRuntimeException e){
            //Erro -> o UserServer que estava no mapa deixou de existir
            if(e.getStatus().getCode().equals(Status.UNAVAILABLE.getCode()) && e.getStatus().getCause() != null){
                //tenta fazer só uma vez mais ‘update’ do map dos server e executar a função pedida
                this.updateServers(server);
                userService = servers.get(server);
                debug("Calling remote procedure: deleteAccount");
                debug("* userId " + "(" + username.getClass().getCanonicalName() + "): " + username);
                DeleteAccountResponse response = userService.deleteAccount(username, prev.proto());
                prev.merge(response.getOperationTS().getTsList());
                System.out.println("OK");
            }
            else{
                throw e;
            }
        }
    }
 */

    private void balance(String line) throws ServerQualifierNotValidException, ServerNotAvailableException{
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String sq = split[1];
        String username = split[2];
        ServerQualifier server = getServerQualifier(sq);

        UserService userService = servers.get(server);
        if(userService == null){
            this.updateServers(server);
            userService = servers.get(server);
        }

        try{
            debug("Calling remote procedure: balance");
            debug("* userId (" + username.getClass().getCanonicalName() + "): " + username);
            BalanceResponse response = userService.balance(username, prev.proto());
            prev.merge(response.getNewPrev().getTsList());
            System.out.println("OK");
            System.out.print(response);
        }catch(StatusRuntimeException e){
            //Erro -> o UserServer que estava no mapa deixou de existir
            if(e.getStatus().getCode().equals(Status.UNAVAILABLE.getCode()) && e.getStatus().getCause() != null){
                //tenta fazer só uma vez mais ‘update’ do map dos server e executar a função pedida
                this.updateServers(server);
                userService = servers.get(server);
                debug("Calling remote procedure: balance");
                debug("* userId (" + username.getClass().getCanonicalName() + "): " + username);
                BalanceResponse response = userService.balance(username, prev.proto());
                prev.merge(response.getNewPrev().getTsList());
                System.out.println("OK");
                System.out.print(response);
            }
            else{
                throw e;
            }
        }
    }

    private void transferTo(String line) throws InvalidValueException, TransferToSelfException, ServerQualifierNotValidException, ServerNotAvailableException{
        String[] split = line.split(SPACE);

        if (split.length != 5){
            this.printUsage();
            return;
        }
        String from = split[2];
        String dest = split[3];
        int amount = Integer.parseInt(split[4]);

        if(from.equals(dest)){
            throw new TransferToSelfException();
        }
        if (amount <= 0){
            throw new InvalidValueException(amount);
        }

        String sq = split[1];
        ServerQualifier server = getServerQualifier(sq);

        UserService userService = servers.get(server);
        if(userService == null){
            this.updateServers(server);
            userService = servers.get(server);
        }

        try{
            debug("Calling remote procedure: transferTo");
            debug("* accountFrom " + "(" + from.getClass().getCanonicalName() + "): " + from);
            debug("* accountTo " + "(" + dest.getClass().getCanonicalName() + "): " + dest);
            debug("* amount (int): " + amount);
            TransferToResponse response = userService.transferTo(from, dest, amount, prev.proto());
            prev.merge(response.getOperationTS().getTsList());
            System.out.println("OK");
        }catch(StatusRuntimeException e){
            //Erro -> o UserServer que estava no mapa deixou de existir
            if(e.getStatus().getCode().equals(Status.UNAVAILABLE.getCode()) && e.getStatus().getCause() != null){
                //tenta fazer só uma vez mais ‘update’ do map dos server e executar a função pedida
                this.updateServers(server);
                userService = servers.get(server);
                debug("Calling remote procedure: transferTo");
                debug("* accountFrom " + "(" + from.getClass().getCanonicalName() + "): " + from);
                debug("* accountTo " + "(" + dest.getClass().getCanonicalName() + "): " + dest);
                debug("* amount (int): " + amount);
                TransferToResponse response = userService.transferTo(from, dest, amount, prev.proto());
                prev.merge(response.getOperationTS().getTsList());
                System.out.println("OK");
            }
            else{
                throw e;
            }
        }
    }

    public ServerQualifier getServerQualifier(String qualifier) throws ServerQualifierNotValidException{
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

    private void updateServers(ServerQualifier serverQualifier) throws ServerNotAvailableException{
        debug("Calling remote procedure: lookup");
        debug("* serverQualifier(" + serverQualifier + ")");
        List<Address> ser = userNamingServerService.lookup(serverQualifier);
        debug("OK");
        debug(ser.toString());
        if(ser.isEmpty()){
            throw new ServerNotAvailableException(serverQualifier);
        }
        UserService userService = new UserService(ser.get(0).getHost(), ser.get(0).getPort());
        servers.put(serverQualifier, userService);
    }

    private void printUsage() {
        System.out.println("Usage:\n" +
                        "- createAccount <server> <username>\n" +
                        "- deleteAccount <server> <username>\n" +
                        "- balance <server> <username>\n" +
                        "- transferTo <server> <username_from> <username_to> <amount>\n" +
                        "- exit\n");
    }
}
