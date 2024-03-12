package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.AccountAlreadyExistsException;
import pt.tecnico.distledger.server.grpc.LedgerVisitor;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.utils.VectorClock;


public class CreateOp extends Operation {

    public CreateOp(String account, VectorClock prev) {
        super(account, prev);
    }

    public CreateOp(String account, VectorClock prev, VectorClock operationTS) {
        super(account, prev, operationTS);
    }

    @Override
    public DistLedgerCommonDefinitions.Operation accept(LedgerVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public void execute(ServerState state) throws AccountAlreadyExistsException {
        setStable(true);
        if (state.getBalance(this.getAccount()) != null) {
            throw new AccountAlreadyExistsException(this.getAccount());
        }
        state.putAccount(getAccount());
    }

    @Override
    public String toString() {
        return "CreateOp{" +
                "account='" + getAccount() + '\'' +
                ", stable=" + isStable() +
                ", prev=" + getPrev().toString() +
                ", operationTS=" + getOperationTS().toString() +
                '}';
    }
}
