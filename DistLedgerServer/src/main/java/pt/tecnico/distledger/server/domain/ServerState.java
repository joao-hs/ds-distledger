package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.Operation;

import pt.tecnico.distledger.server.domain.exceptions.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerState {
    private List<Operation> ledger;
    private Map<String, Integer> accounts;

    public ServerState() {
        this.ledger = new CopyOnWriteArrayList<>();
        this.accounts = new ConcurrentHashMap<String, Integer>();

        accounts.put("broker", 1000);
    }

    /*
     ! Parameter verification should be done before calling this method.
     * Creates a new account with the given username and empty balance.
     * @param username - username of the new account
     */
    public synchronized void putAccount(String username) {
        accounts.put(username, 0);
    }

    /*
     * @param username - username of the account
     * @return balance of the account or null if the account does not exist
     */
    public Integer getBalance(String username) {
        return accounts.get(username);
    }

    /*
     ! Parameter verification should be done before calling this method.
     * Transfers the given amount from one account to the other.
     * @param fromAccount - username of the account from which the amount will be
     * transferred
     * @param toAccount - username of the account to which the amount will be
     * transferred
     * @param amount - amount to be transferred
     */
    public synchronized void transferBalance(String fromAccount, String toAccount, int amount) {
        accounts.put(fromAccount, getBalance(fromAccount) - amount);
        accounts.put(toAccount, getBalance(toAccount) + amount);
    }

    /*
     * Saves an operation to the ledger.
     */
    public synchronized void save(Operation op) {
        ledger.add(op);
    }

    /*
     * Gets the balance of an account.
     * @param username - username of the account
     * @throws AccountDoesntExistException - if the account does not exist
     */
    public int balanceAccount(String username) throws AccountDoesntExistException {
        Integer balance;
        balance = getBalance(username);
        if (balance == null) {
            throw new AccountDoesntExistException(username);
        }
        return balance;
    }

    public List<Operation> getLedgerState() {
        return ledger;
    }
    
    /* 
    ! This method is not used in the final version of the project.
    public synchronized void deleteAccount(String username) throws AccountDoesntExistException,
            BalanceIsNotZeroException, CantDeleteBrokerException, ServerIsNotAvailableException {
        if (!active) {
            throw new ServerIsNotAvailableException();
        }
        if(username.equals("broker")){
            throw new CantDeleteBrokerException();
        }
        if (balanceAccount(username) != 0) {
            throw new BalanceIsNotZeroException(username);
        }
        accounts.remove(username);
        ledger.add(new DeleteOp(username));
    }
    */

    /*
     * Transfers the given amount from one account to the other. Then saves the
     * operation to the ledger.
     * @param accountFrom - username of the account from which the amount will be transferred
     * @param accountTo - username of the account to which the amount will be transferred
     * @param amount - amount to be transferred
     * @throws InvalidValueException - if the amount is negative or zero
     * @throws TransferToSelfException - if accountFrom and accountTo are the same
     * @throws AccountDoesntExistException - if one of the accounts does not exist
     * @throws InsufficientFundsException - if the account from which the amount
     * will be transferred does not have enough funds
     */
    /* public synchronized void transferToAccount(String accountFrom, String accountTo, int amount)
            throws AccountDoesntExistException, InsufficientFundsException,
            TransferToSelfException, InvalidValueException {
        if (amount <= 0) {
            throw new InvalidValueException(amount);
        }
        if (accountFrom.equals(accountTo)) {
            throw new TransferToSelfException();
        }
        balanceAccount(accountTo); // check if account exists
        if (balanceAccount(accountFrom) < amount) {
            throw new InsufficientFundsException(accountFrom);
        }
        TransferOp op = new TransferOp(accountFrom, accountTo, amount);
        op.execute(this);
        save(op);
    } */

    /*
     * Resets the state of the server to the initial state. This method is used
     * when the server is badly synchronized with the other servers.
     */
    /* public void reset() {
        this.ledger = new CopyOnWriteArrayList<>();
        this.accounts = new ConcurrentHashMap<String, Integer>();
        accounts.put("broker", 1000);
    } */
    
    /*
     * Applies an operation to the server state.
     * There is no argument validation in this method, since it was already done by another server.
     * @param op - operation to be applied
     */
    /* public void applyOperation(Operation op) {
        if (op == null)
            return;
        op.execute(this);
        save(op);
    } */

    /*
     * Applies a list of operations to the server state.
     * There is no argument validation in this method, since it was already done by another server.
     * @param operations - list of operations to be applied
     */
    /* public void applyAllOperations(List<Operation> operations) {
        reset();
        for (Operation op : operations) {
            op.execute(this);
            save(op);
        }
    } */
}
