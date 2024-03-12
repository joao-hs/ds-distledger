package pt.tecnico.distledger.adminclient;

import pt.tecnico.distledger.adminclient.exceptions.*;
import pt.tecnico.distledger.adminclient.grpc.AdminNamingServerService;
import pt.tecnico.distledger.adminclient.grpc.AdminService;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateResponse;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Address;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;

import java.util.*;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class CommandParser {
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    private static final String SPACE = " ";
    private static final String ACTIVATE = "activate";
    private static final String DEACTIVATE = "deactivate";
    private static final String GET_LEDGER_STATE = "getLedgerState";
    private static final String GOSSIP = "gossip";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    
    private final AdminNamingServerService adminNamingServerService;

    private Map<ServerQualifier, AdminService> servers = new HashMap<>();

    public CommandParser(AdminNamingServerService adminNamingServerService) {
        this.adminNamingServerService = adminNamingServerService;
    }

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
                    case ACTIVATE:
                        this.activate(line);
                        break;

                    case DEACTIVATE:
                        this.deactivate(line);
                        break;

                    case GET_LEDGER_STATE:
                        this.dump(line);
                        break;

                    case GOSSIP:
                        this.gossip(line);
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
            } catch (NumberFormatException nfe) {
                System.err.println("Invalid amount format");
            } catch (CommandNotFoundException cnf){
                System.out.println(cnf.getMessage());
            } catch (Exception e){
                System.err.println(e.getMessage());
            }

        }
        scanner.close();
    }

    private void activate(String line) throws ServerQualifierNotValidException, ServerDoesntExistException{
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        ServerQualifier server = getServerQualifier(split[1]);

        AdminService adminService = servers.get(server);
        if(adminService == null){
            this.updateServers(server);
            adminService = servers.get(server);
        }

        try{
            debug("Calling remote procedure: activate");
            adminService.activate();
            System.out.println("OK");
        }catch(StatusRuntimeException e){
            //Erro -> o AdminServer que estava no mapa ou esta unavailable ou deixou de existir
            if(e.getStatus().getCode().equals(Status.UNAVAILABLE.getCode())){
                //tenta fazer só uma vez mais ‘update’ do map dos server e executar a função pedida
                this.updateServers(server);
                adminService = servers.get(server);
                debug("Calling remote procedure: activate");
                adminService.activate();
                System.out.println("OK");
            }
            else{
                throw e;
            }
        }
        
    }

    private void deactivate(String line) throws ServerQualifierNotValidException, ServerDoesntExistException{
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        ServerQualifier server = getServerQualifier(split[1]);

        AdminService adminService = servers.get(server);
        if(adminService == null){
            this.updateServers(server);
            adminService = servers.get(server);
        }

        try{
            debug("Calling remote procedure: deactivate");
            adminService.deactivate();
            System.out.println("OK");
        }catch(StatusRuntimeException e){
            //Erro -> o AdminServer que estava no mapa ou esta unavailable ou deixou de existir
            if(e.getStatus().getCode().equals(Status.UNAVAILABLE.getCode())){
                //tenta fazer só uma vez mais ‘update’ do map dos server e executar a função pedida
                this.updateServers(server);
                adminService = servers.get(server);
                debug("Calling remote procedure: deactivate");
                adminService.deactivate();
                System.out.println("OK");
            }
            else{
                throw e;
            }
        }
    }

    private void dump(String line) throws ServerQualifierNotValidException, ServerDoesntExistException{
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        ServerQualifier server = getServerQualifier(split[1]);

        AdminService adminService = servers.get(server);
        if(adminService == null){
            this.updateServers(server);
            adminService = servers.get(server);
        }

        try{
            debug("Calling remote procedure: getLedgerState");
            getLedgerStateResponse response = adminService.getLedgerState();
            System.out.println("OK");
            System.out.print(response);
        }catch(StatusRuntimeException e){
            //Erro -> o AdminServer que estava no mapa ou esta unavailable ou deixou de existir
            if(e.getStatus().getCode().equals(Status.UNAVAILABLE.getCode())){
                //tenta fazer só uma vez mais ‘update’ do map dos server e executar a função pedida
                this.updateServers(server);
                adminService = servers.get(server);
                debug("Calling remote procedure: getLedgerState");
                getLedgerStateResponse response = adminService.getLedgerState();
                System.out.println("OK");
                System.out.print(response);
            }
            else{
                throw e;
            }
        }
    }

    private void gossip(String line) throws ServerQualifierNotValidException, ServerDoesntExistException{
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        ServerQualifier server = getServerQualifier(split[1]);

        AdminService adminService = servers.get(server);
        if(adminService == null){
            this.updateServers(server);
            adminService = servers.get(server);
        }

        try{
            debug("Calling remote procedure: gossip");
            adminService.gossip();
            System.out.println("OK");
        }catch(StatusRuntimeException e){
            //Erro -> o AdminServer que estava no mapa ou esta unavailable ou deixou de existir
            if(e.getStatus().getCode().equals(Status.UNAVAILABLE.getCode())){
                //tenta fazer só uma vez mais ‘update’ do map dos server e executar a função pedida
                this.updateServers(server);
                adminService = servers.get(server);
                debug("Calling remote procedure: gossip");
                adminService.gossip();
                System.out.println("OK");
            }
            else{
                throw e;
            }
        }
    }

    private void printUsage() {
        System.out.println("Usage:\n" +
                "- activate <server>\n" +
                "- deactivate <server>\n" +
                "- getLedgerState <server>\n" +
                "- gossip <server>\n" +
                "- exit\n");
    }

    private ServerQualifier getServerQualifier(String qualifier) throws ServerQualifierNotValidException{
        if(qualifier.equals("A")){
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

    private void updateServers(ServerQualifier serverQualifier) throws ServerDoesntExistException{
        debug("Calling remote procedure: lookup");
        debug("* serverQualifier(" + serverQualifier + ")");
        List<Address> ser = adminNamingServerService.lookup(serverQualifier);
        debug("OK");
        debug(ser.toString());
        if(ser.isEmpty()){
            throw new ServerDoesntExistException(serverQualifier);
        }
        AdminService adminService = new AdminService(ser.get(0).getHost(), ser.get(0).getPort());
        servers.put(serverQualifier, adminService);
    }

}
