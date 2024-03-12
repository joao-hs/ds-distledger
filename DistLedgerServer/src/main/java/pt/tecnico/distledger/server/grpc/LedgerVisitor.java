package pt.tecnico.distledger.server.grpc;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
//import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public interface LedgerVisitor {
    DistLedgerCommonDefinitions.Operation visit(CreateOp createOp);

    //DistLedgerCommonDefinitions.Operation visit(DeleteOp deleteOp);

    DistLedgerCommonDefinitions.Operation visit(TransferOp transferOp);
}