package org.monroe.team.smooker.app.uc.common;

import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.dp.DAO;
import org.monroe.team.smooker.app.dp.TransactionManager;

public abstract class TransactionUserCase<RequestType,ResponseType> extends UserCaseSupport<RequestType,ResponseType> {

    public TransactionUserCase(Registry registry) {
        super(registry);
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
