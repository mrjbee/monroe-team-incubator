package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.app.db.Dao;

public class RemoveData extends TransactionUserCase<Boolean, Void, Dao>{

    public RemoveData(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Void transactionalExecute(Boolean todayOnly, Dao dao) {
        if (todayOnly) {
            dao.removeSmokesAfter(DateUtils.today());
            dao.removeSmokesCancellationAfter(DateUtils.today());
        }
        return null;
    }
}
