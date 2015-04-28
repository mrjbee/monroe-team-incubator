package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.app.common.constant.SmokeCancelReason;
import org.monroe.team.smooker.app.db.DAO;
import org.monroe.team.smooker.app.db.Dao;
import org.monroe.team.smooker.app.uc.common.ActionDetails;

import java.util.Date;

public class GetLastAction extends TransactionUserCase<Void,ActionDetails,Dao> {

    public GetLastAction(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected ActionDetails transactionalExecute(Void request, Dao dao) {
        ActionDetails lastLoggedSmoke = null;
        ActionDetails lastLoggedSmokeCancellation = null;
        DAOSupport.Result result = dao.getLastLoggedSmoke();
        if (result != null){
            lastLoggedSmoke = new ActionDetails(result.get(0,Long.class), new Date(result.get(1,Long.class)), ActionDetails.Type.SMOKE_BREAK);
        }
        result= dao.getLastLoggedSmokeCancellation();
        if (result != null){
            lastLoggedSmokeCancellation = new ActionDetails(result.get(0,Long.class), new Date(result.get(1,Long.class)),
                    SmokeCancelReason.SKIP == SmokeCancelReason.byId(result.get(2,Integer.class))? ActionDetails.Type.SMOKE_CANCEL_SKIP : ActionDetails.Type.SMOKE_CANCEL_POSTPONED);
        }

        if (lastLoggedSmoke == null && lastLoggedSmokeCancellation == null){
            return null;
        }

        ActionDetails actionToUse = null;
        if (lastLoggedSmoke == null && lastLoggedSmokeCancellation != null){
            actionToUse = lastLoggedSmokeCancellation;
        }else if (lastLoggedSmoke != null && lastLoggedSmokeCancellation == null){
            actionToUse = lastLoggedSmoke;
        } else if (lastLoggedSmoke.date.after(lastLoggedSmokeCancellation.date)){
            actionToUse = lastLoggedSmoke;
        }else{
            actionToUse = lastLoggedSmokeCancellation;
        }

        if (actionToUse.date.before(DateUtils.today())){
            actionToUse = null;
        }

        return actionToUse;
    }
}
