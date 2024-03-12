package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.InsufficientFundsException;
import pt.tecnico.distledger.server.domain.exceptions.InvalidValueException;
import pt.tecnico.distledger.server.domain.exceptions.TransferToSelfException;
import pt.tecnico.distledger.server.grpc.LedgerVisitor;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.utils.VectorClock;


public class TransferOp extends Operation {
    private String destAccount;
    private int amount;

    public TransferOp(String fromAccount, String destAccount, int amount, VectorClock prev) {
        super(fromAccount, prev);
        this.destAccount = destAccount;
        this.amount = amount;
    }

    public TransferOp(String fromAccount, String destAccount, int amount, VectorClock prev, VectorClock operationTS) {
        super(fromAccount, prev, operationTS);
        this.destAccount = destAccount;
        this.amount = amount;
    }

    public String getDestAccount() {
        return destAccount;
    }

    public void setDestAccount(String destAccount) {
        this.destAccount = destAccount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public DistLedgerCommonDefinitions.Operation accept(LedgerVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public void execute(ServerState state) throws InvalidValueException, TransferToSelfException, InsufficientFundsException {
        setStable(true);
        if(amount <= 0){
            throw new InvalidValueException(amount);
        }
        if(this.getAccount().equals(this.destAccount)){
            throw new TransferToSelfException();
        }
        if(state.getBalance(this.getAccount()) < amount){
            throw new InsufficientFundsException(this.getAccount());
        }
        state.transferBalance(getAccount(), getDestAccount(), getAmount());
    }

    @Override
    public String toString() {
        return "TransferOp{" +
                "account='" + getAccount() + '\'' +
                ", destAccount='" + destAccount + '\'' +
                ", amount=" + amount +
                ", stable=" + isStable() +
                ", prev=" + getPrev().toString() +
                ", operationTS=" + getOperationTS().toString() +
                '}';
    }
}
