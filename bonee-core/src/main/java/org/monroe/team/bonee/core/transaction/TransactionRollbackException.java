package org.monroe.team.bonee.core.transaction;

/**
 * User: MisterJBee
 * Date: 6/12/13 Time: 11:07 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class TransactionRollbackException extends RuntimeException {

    public TransactionRollbackException(Throwable cause) {
        super(cause);
    }

    public TransactionRollbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
