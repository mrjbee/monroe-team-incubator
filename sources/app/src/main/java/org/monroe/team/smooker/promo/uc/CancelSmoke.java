package org.monroe.team.smooker.promo.uc;

import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.smooker.promo.common.constant.SmokeCancelReason;
import org.monroe.team.smooker.promo.db.Dao;

public class CancelSmoke extends TransactionUserCase<SmokeCancelReason, Void, Dao> {


    public CancelSmoke(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Void transactionalExecute(SmokeCancelReason reason, Dao dao) {
        dao.addSmokeCancellation(reason);
        return null;
    }

}
