package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.smooker.app.db.Dao;

public class AddSmoke extends TransactionUserCase<Void, Boolean, Dao> {

    public AddSmoke(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Boolean transactionalExecute(Void request, Dao dao) {
        return -1 != dao.addSmoke();
    }
}
