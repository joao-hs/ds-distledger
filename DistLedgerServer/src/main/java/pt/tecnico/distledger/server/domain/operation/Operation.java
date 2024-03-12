package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.IllegalOperationException;
import pt.tecnico.distledger.server.grpc.LedgerVisitor;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.utils.VectorClock;

public abstract class Operation {
    private String account;
    private boolean stable=false;
    private VectorClock prev;
    private VectorClock operationTS = new VectorClock();

    public Operation(String fromAccount, VectorClock prev) {
        this.account = fromAccount;
        this.prev = prev;
    }

    public Operation(String fromAccount, VectorClock prev, VectorClock operationTS) {
        this.account = fromAccount;
        this.prev = prev;
        this.operationTS = operationTS;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public boolean isStable() {
        return stable;
    }

    protected void setStable(boolean stable) {
        this.stable = stable;
    }

    public VectorClock getPrev(){
        return this.prev;
    }

    public VectorClock getOperationTS(){
        return this.operationTS;
    }

    public void setOperationTS(VectorClock operationTS){
        this.operationTS = operationTS;
    }

    public abstract DistLedgerCommonDefinitions.Operation accept(LedgerVisitor visitor);

    /* 
     * Executes the operation. This should be run in a synchronized block
     * and only when it is sure that the operation is valid.
     * @param state - state of the server
     */
    public abstract void execute(ServerState state) throws IllegalOperationException;
}
