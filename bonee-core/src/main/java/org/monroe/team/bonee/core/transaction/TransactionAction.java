package org.monroe.team.bonee.core.transaction;

import org.omg.PortableInterceptor.SUCCESSFUL;

/**
 * User: MisterJBee
 * Date: 6/12/13 Time: 10:36 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public abstract class TransactionAction {

    private static final TransactionStatus TRANSACTION_STATUS_SUCCESS = new TransactionStatus(true, null);
    private TransactionAction nextAction;

    void setNext(TransactionAction action){
        if (nextAction != null){
            nextAction.setNext(action);
        } else {
            nextAction = action;
        }

    }

    TransactionAction getNext(){
        return nextAction;
    };

    TransactionStatus commit(){
        TransactionStatus nextActionSuccess = TRANSACTION_STATUS_SUCCESS;
        TransactionStatus ownStatus = commitImpl();
        if (!ownStatus.wasSuccess){
            return ownStatus;
        }
        if (nextAction != null){
             nextActionSuccess = nextAction.commit();
        }
        if (!nextActionSuccess.wasSuccess){
            commitRevertImpl();
        }
        return nextActionSuccess;
    }

    private TransactionStatus commitImpl() {
        try{
            doCommit();
            return TRANSACTION_STATUS_SUCCESS;
        } catch (Exception e){
            return new TransactionStatus(false, e);
        }
    }

    private void commitRevertImpl() {
        try{
            doRollback();
        } catch (Exception e){
            throw new TransactionRollbackException(e);
        }
    }

    protected abstract void doRollback();
    protected abstract void doCommit();


    static class TransactionStatus {
        final boolean wasSuccess;
        final Exception rootCause;

        TransactionStatus(boolean wasSuccess, Exception rootCause) {
            this.wasSuccess = wasSuccess;
            this.rootCause = rootCause;
        }
    }
}
