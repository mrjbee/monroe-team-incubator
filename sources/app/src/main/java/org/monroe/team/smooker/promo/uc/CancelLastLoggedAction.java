package org.monroe.team.smooker.promo.uc;

import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.smooker.promo.db.Dao;
import org.monroe.team.smooker.promo.uc.common.ActionDetails;

public class CancelLastLoggedAction extends TransactionUserCase<ActionDetails,Void, Dao> {

    public CancelLastLoggedAction(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Void transactionalExecute(ActionDetails action, Dao dao) {
        if (action.type == ActionDetails.Type.SMOKE_BREAK){
            dao.removeSmokeById(action.id);
        }else{
            dao.removeSmokeCancellationById(action.id);
        }
        return null;
    }
}
