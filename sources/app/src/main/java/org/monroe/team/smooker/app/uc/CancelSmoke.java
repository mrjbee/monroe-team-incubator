package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.manager.EventMessenger;
import org.monroe.team.android.box.manager.ServiceRegistry;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.smooker.app.common.constant.SmokeCancelReason;
import org.monroe.team.smooker.app.db.DAO;
import org.monroe.team.smooker.app.uc.common.TransactionUserCase;

public class CancelSmoke extends TransactionUserCase<SmokeCancelReason, Void> {

    public CancelSmoke(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Void transactionalExecute(SmokeCancelReason reason, DAO dao) {
        dao.addSmokeCancellation(reason);
        using(EventMessenger.class).send(Events.SMOKE_CANCELED,reason);
        return null;
    }

}
