package org.monroe.team.smooker.app.uc.common;

import org.monroe.team.android.box.manager.ServiceRegistry;
import org.monroe.team.smooker.app.db.DAO;
import org.monroe.team.smooker.app.db.TransactionManager;

public abstract class TransactionUserCase<RequestType,ResponseType> extends UserCaseSupport<RequestType,ResponseType> {

    public TransactionUserCase(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    final public ResponseType execute(final RequestType request) {
        return using(TransactionManager.class).execute(new TransactionManager.TransactionAction<ResponseType>() {
            @Override
            public ResponseType execute(DAO dao) {
                return transactionalExecute(request,dao);
            }
        });
    }

    protected abstract ResponseType transactionalExecute(RequestType request, DAO dao);
}
