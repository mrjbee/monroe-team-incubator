package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.android.box.services.EventMessenger;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.smooker.app.common.constant.SmokeCancelReason;
import org.monroe.team.smooker.app.db.Dao;

public class CancelSmoke extends TransactionUserCase<SmokeCancelReason, Void, Dao> {


    public CancelSmoke(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Void transactionalExecute(SmokeCancelReason reason, Dao dao) {
        dao.addSmokeCancellation(reason);
        using(EventMessenger.class).send(Events.SMOKE_CANCELED,reason);
        return null;
    }

}
