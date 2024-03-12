package pt.tecnico.distledger.server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pt.tecnico.distledger.server.domain.exceptions.AccountDoesntExistException;
import pt.tecnico.distledger.server.domain.exceptions.IllegalOperationException;
import pt.tecnico.distledger.server.domain.exceptions.ServerIsNotAvailableException;
import pt.tecnico.distledger.server.domain.exceptions.ServerIsOutDatedException;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.grpc.CrossServerService;
import pt.tecnico.distledger.server.grpc.LedgerVisitor;
import pt.tecnico.distledger.server.grpc.LedgerVisitorOperationBuilder;
import pt.tecnico.distledger.server.grpc.NamingServerService;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateResponse;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Address;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.ServerQualifier;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.ulisboa.tecnico.distledger.utils.VectorClock;

public class ReplicaManager {
    private final NamingServerService namingServerService;
    private final Address ownAddress;
    private final ServerQualifier serverQualifier;
    private final ServerState state = new ServerState();
    private final VectorClock replicaTS = new VectorClock();
    private final VectorClock valueTS = new VectorClock();
    private final Object lock = new Object();
    private boolean active;

    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);
    private static void debug(String debugMessage) {
        if (DEBUG_FLAG) {
            System.err.print("[DEBUG] ");
            System.err.println(debugMessage);
        }
    }

    public ReplicaManager(NamingServerService namingServerService, Address ownAddress, ServerQualifier serverQualifier) {
        this.namingServerService = namingServerService;
        this.ownAddress = ownAddress;
        this.active = true;
        this.serverQualifier = serverQualifier;
    }

    /*
     * Gets the balance of an account. Does not break causal order.
     * @param account The account to get the balance from
     * @param prev Client's vector clock
     * @throws ServerIsNotAvailableException If the server is not available
     * @throws ServerIsOutDatedException If the server is outdated in relation to the client
     * @throws AccountDoesntExistException If the account does not exist
     */
    public synchronized BalanceResponse balance(String account, VectorClock prev) throws AccountDoesntExistException, ServerIsNotAvailableException, ServerIsOutDatedException {
        debug("Remote Procedure Call: balance");
        debug("* userId: " + account);

        if (!active)
            throw new ServerIsNotAvailableException();
        if (!valueTS.GE(prev)) {
            // Waits 10 seconds before checking again and throwing exception if nothing changed
            try {
                debug("* Waiting for valueTS to be updated... (10 seconds)");
                synchronized (lock) {
                    lock.wait(10000);
                }
            } catch (InterruptedException e) {
                debug("* InterruptedException");
                throw new ServerIsNotAvailableException();
            }
            if (!valueTS.GE(prev))
                throw new ServerIsOutDatedException();
        }
        Integer value = state.balanceAccount(account);

        debug("    * value: " + value);
        debug("**DONE**");
        return BalanceResponse.newBuilder().setValue(value).setNewPrev(valueTS.proto()).build();
    }

    /*
     * Register an operation in the ledger. Updates replicaTS.
     * @param op The operation to be registered
     */
    private void registerOperation(Operation op) {
        replicaTS.setTS(serverQualifier.getNumber(), replicaTS.getTS(serverQualifier.getNumber())+1);
        op.setOperationTS(replicaTS);
        state.save(op);
    }

    /*
     * Creates a new vector clock to be returned to the client after a write.
     * @param op The operation that was registered
     */
    public VectorClock getNewTS(Operation op){
        debug("Creating new TS to return to client");

        VectorClock newTS = new VectorClock();
        newTS.merge(op.getPrev());
        newTS.merge(op.getOperationTS());

        debug("    * newTS: " + newTS);
        debug("**DONE**");

        return newTS;
    }

    /*
     * Register a create account request.
     * @param userId The user id of the account to be created
     * @param prev Client's vector clock
     * @throws ServerIsNotAvailableException If the server is not available
     */
    public CreateOp registerCreateAccountRequest(String userId, VectorClock prev) throws ServerIsNotAvailableException {
        debug("Remote Procedure Call: createAccount");
        debug("* userId " + "(" + userId.getClass().getCanonicalName() + "): " + userId);

        if (!active){
            throw new ServerIsNotAvailableException();
        }
        CreateOp createOp = new CreateOp(userId, prev);
        registerOperation(createOp);

        debug("**DONE**");
        return createOp;
    }

    /*
     * Register a transferTo request.
     * @param accountFrom The account to transfer from
     * @param accountTo The account to transfer to
     * @param amount The amount to transfer
     * @param prev Client's vector clock
     * @throws ServerIsNotAvailableException If the server is not available
     */
    public TransferOp registerTransferToRequest(String accountFrom, String accountTo, int amount, VectorClock prev) throws ServerIsNotAvailableException {
        debug("Remote Procedure Call: transferTo");
        debug("* accountFrom " + "(" + accountFrom.getClass().getCanonicalName() + "): " + accountFrom);
        debug("* accountTo " + "(" + accountTo.getClass().getCanonicalName() + "): " + accountTo);
        debug("* amount (int): " + amount);

        if (!active){
            throw new ServerIsNotAvailableException();
        }
        TransferOp tranferOp = new TransferOp(accountFrom, accountTo, amount, prev);
        registerOperation(tranferOp);

        debug("**DONE**");

        return tranferOp;
    }

    /*
     * Tries to stablize an operation. Updates valueTS.
     * If the operation is illegal, it is considered stable but it is not executed.
     * @param op The operation to be stablized
     */
    public void executeOperation(Operation op) {
        debug("Operation to be executed: " + op.toString());

        if (valueTS.GE(op.getPrev())) {
            if (!op.isStable()) {
                try {
                    debug("Operation with OperationTS " + op.getOperationTS().toString() + " is unstable and will be executed");
                    op.execute(state);
                } catch (IllegalOperationException e) {
                    debug("Operation with OperationTS " + op.getOperationTS().toString() + " failed to execute with description " + e.getMessage());
                }
                valueTS.merge(op.getOperationTS());
            } else {
                debug("Operation " + op.toString() + " was not executed because it is stable");
            }
        } else {
            debug("Operation " + op.toString() + " was not executed because server is outdated");
        }
    }

    /*
     * Activates the server.
     */
    public synchronized void activate() {
        debug("Remote Procedure Call: activate");

        this.active = true;

        debug("* isActive: " + this.active);
        debug("**DONE**");
    }

    /*
     * Deactivates the server.
     */
    public synchronized void deactivate() {
        debug("Remote Procedure Call: deactivate");

        this.active = false;

        debug("* isActive: " + this.active);
        debug("**DONE**");
    }

    /*
     * Creates a LedgerState protobuf object from a list of domain's operations.
     * @param ledger the list of operations 
     */
    private DistLedgerCommonDefinitions.LedgerState createLedgerState(List<Operation> ledger) {
        debug("Remote Procedure Call: getLedgerState");

        List<DistLedgerCommonDefinitions.Operation> operations = new ArrayList<>();
        LedgerVisitor visitor = new LedgerVisitorOperationBuilder();
        for (Operation operation : ledger) {
            operations.add(operation.accept(visitor));
        }

        debug("    * ledger: " + DistLedgerCommonDefinitions.LedgerState.newBuilder().addAllLedger(operations).build());
        debug("**DONE**");

        return DistLedgerCommonDefinitions.LedgerState.newBuilder().addAllLedger(operations).build();
    }

    /*
     * Returns the LedgerState protobuf object.
     */
    public getLedgerStateResponse getLedgerState() {
        getLedgerStateResponse response = getLedgerStateResponse.newBuilder()
                    .setLedgerState(createLedgerState(state.getLedgerState()))
                    .setValueTS(this.valueTS.proto())
                    .setReplicaTS(this.replicaTS.proto())
                    .build();
        return response;
    }

    /*
     * Returns the list of available servers.
     */
    private List<CrossServerService> lookupForAvailableServers() {
        List<Address> reachableServers = new ArrayList<>();
        List<CrossServerService> crossServerServices = new ArrayList<>();
        reachableServers.addAll(namingServerService.lookup(NamingServer.ServiceName.DISTLEDGER));
        reachableServers.remove(ownAddress);
        crossServerServices.addAll(
            reachableServers.stream()
                .map(a -> new CrossServerService(a.getHost(), a.getPort()))
                .collect(Collectors.toList()));
        return crossServerServices;
    }

    /*
     * Undirected gossip. Sends the state to all available servers.
     * @throws ServerIsNotAvailableException If the server is not available
     */
    public void gossip() throws ServerIsNotAvailableException {
        if (!active)
            throw new ServerIsNotAvailableException();
        List<CrossServerService> crossServerServices = lookupForAvailableServers();
        if (crossServerServices.isEmpty())
            return;
        
        LedgerState ledger = createLedgerState(state.getLedgerState());
        
        for (CrossServerService crossServerService : crossServerServices) {
            crossServerService.propagateState(ledger, replicaTS.proto());
        }
    }

    /*
     * Directed gossip to a specific server.
     * @param receiver The server to send the state to
     * @throws ServerIsNotAvailableException If the server is not available
     */
    public void gossip(Address receiver) throws ServerIsNotAvailableException {
        if (!active)
            throw new ServerIsNotAvailableException();
        CrossServerService crossServerService = new CrossServerService(receiver.getHost(), receiver.getPort());
        LedgerState ledger = createLedgerState(state.getLedgerState());
        crossServerService.propagateState(ledger, replicaTS.proto());
    }

    /*
     * Receives the state from other server and updates itself.
     * @param propagatedOperations The list of operations to be applied locally
     * @param propagatedReplicaTS replicaTS of the server that sent the state
     */
    public void receiveState(List<Operation> propagatedOperations, VectorClock propagatedReplicaTS) {
        debug("Remote Procedure Call: propagateState");
        debug("* state (LedgerState): " + propagatedOperations);
        debug("* replicaTS (VectorClock): " + propagatedReplicaTS.toString());

        if (!active)
            return;
        debug("Receiving propagated operations");
        for (Operation operation : propagatedOperations) {
            if (!replicaTS.GE(operation.getOperationTS())) {
                debug("Saving operation and executing it");
                state.save(operation);
                executeOperation(operation);
            }
        }
        replicaTS.merge(propagatedReplicaTS);
        debug("Stablizing all operations");
        for (Operation operation : state.getLedgerState()) {
            executeOperation(operation);
        }
        synchronized (lock) {
            lock.notifyAll();
        }

        debug("**DONE**");
    }

    /*
     * Asks for the first available server to gossip.
     */
    public void propagateStatePull() {
        debug("Remote Procedure Call: propagateStateRequest");

        if (!active)
            return;
        List<CrossServerService> crossServerServices = lookupForAvailableServers();
        if (crossServerServices.isEmpty())
            return;
        // just needs one server to gossip
        for (CrossServerService crossServerService : crossServerServices) {
            try {
                crossServerService.propagateStatePull(ownAddress);
                break;
            } catch (ServerIsNotAvailableException sinae) {
                continue; // tries to ask for another server to gossip
            }
        }

        debug("**DONE**");
    }
    
}
