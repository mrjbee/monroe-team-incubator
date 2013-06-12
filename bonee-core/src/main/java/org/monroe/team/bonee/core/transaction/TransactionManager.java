package org.monroe.team.bonee.core.transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 6/12/13 Time: 10:35 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class TransactionManager {

    private final static ThreadLocal<TransactionManager> transactionManagerThreadLocal = new InheritableThreadLocal<TransactionManager>(){
        @Override
        protected TransactionManager initialValue() {
            return new TransactionManager();
        }
    };

    public static void add(TransactionAction action){
        transactionManagerThreadLocal.get().addImpl(action);
    }

    public static void commit() throws TransactionCommitException, TransactionRollbackException {
        transactionManagerThreadLocal.get().commitImpl();
    }

    public static List<TransactionAction> select(Class... actionClasses){
        return transactionManagerThreadLocal.get().selectImpl(actionClasses);
    }

    private TransactionAction actionStackHead;

    void addImpl(TransactionAction action){
        if (actionStackHead == null){
            actionStackHead = action;
        }
        actionStackHead.setNext(action);
    }

    void commitImpl() throws TransactionCommitException, TransactionRollbackException {
        if (actionStackHead == null) return;
        TransactionAction action = actionStackHead;
        actionStackHead = null;
        TransactionAction.TransactionStatus status = action.commit();
        if(!status.wasSuccess) throw new TransactionCommitException(status.rootCause);
    }

    List<TransactionAction> selectImpl(Class... actionClasses){
        List<TransactionAction> answer = new ArrayList<TransactionAction>(3);
        TransactionAction action = actionStackHead;
        while (action != null){
            if (isActionMatch(action, actionClasses)){
                  answer.add(action);
            }
            action = actionStackHead.getNext();
        }
        return answer;
    }

    private boolean isActionMatch(TransactionAction action, Class[] actionClasses) {
        for (Class actionClass : actionClasses) {
            if (actionClass.isInstance(action)) return true;
        }
        return false;
    }

}
