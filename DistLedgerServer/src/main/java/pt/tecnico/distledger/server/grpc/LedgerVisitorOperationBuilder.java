package pt.tecnico.distledger.server.grpc;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
//import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public class LedgerVisitorOperationBuilder implements LedgerVisitor {

    @Override
    public DistLedgerCommonDefinitions.Operation visit(CreateOp createOp) {
        return DistLedgerCommonDefinitions.Operation.newBuilder()
                .setType(DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT)
                .setUserId(createOp.getAccount())
                .setPrev(createOp.getPrev().proto())
                .setOperationTS(createOp.getOperationTS().proto())
                .build();
    }

   /*  @Override
    public DistLedgerCommonDefinitions.Operation visit(DeleteOp deleteOp) {
        return DistLedgerCommonDefinitions.Operation.newBuilder()
                .setType(DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT)
                .setUserId(deleteOp.getAccount())
                .build();
    } */

    @Override
    public DistLedgerCommonDefinitions.Operation visit(TransferOp transferOp) {
        return DistLedgerCommonDefinitions.Operation.newBuilder()
                .setType(DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO)
                .setUserId(transferOp.getAccount())
                .setDestUserId(transferOp.getDestAccount())
                .setAmount(transferOp.getAmount())
                .setPrev(transferOp.getPrev().proto())
                .setOperationTS(transferOp.getOperationTS().proto())
                .build();
    }
}
