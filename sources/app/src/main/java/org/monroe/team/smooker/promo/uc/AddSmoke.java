package org.monroe.team.smooker.promo.uc;

import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.smooker.promo.db.Dao;

public class AddSmoke extends TransactionUserCase<Void, Void, Dao> {

    public AddSmoke(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Void transactionalExecute(Void request, Dao dao) {
        boolean added = -1 != dao.addSmoke();
        if (!added) throw new IllegalArgumentException("Add smoke failed");
        return null;
    }
}
