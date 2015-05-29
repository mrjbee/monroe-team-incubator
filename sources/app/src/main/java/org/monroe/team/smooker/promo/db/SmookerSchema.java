package org.monroe.team.smooker.promo.db;

import org.monroe.team.android.box.db.Schema;

public class SmookerSchema extends Schema{

    public SmookerSchema() {
        super(3, "Smooker.db", SmokeEntry.class, SmokeCancelEntry.class);
    }

    public static class SmokeEntry extends VersionTable{

        public final String TABLE_NAME = "smoke";
        public final ColumnID<Long> _ID = column("_id", Long.class);
        public final ColumnID<Long> _DATE = column("date", Long.class);

        public SmokeEntry() {
            define(3,TABLE_NAME)
                    .column(_ID, "INTEGER PRIMARY KEY AUTOINCREMENT")
                    .column(_DATE, "INTEGER NOT NULL");
        }
    }

    public static class SmokeCancelEntry extends VersionTable {

        public final String TABLE_NAME = "smoke_cancel";
        public final ColumnID<Long> _ID = column("_id",Long.class);
        public final ColumnID<Long> _DATE = column("date",Long.class);
        public final ColumnID<Integer> _REASON = column("reason",Integer.class);

        public SmokeCancelEntry() {
            define(3, TABLE_NAME)
                    .column(_ID, "INTEGER PRIMARY KEY AUTOINCREMENT")
                    .column(_DATE, "INTEGER NOT NULL")
                    .column(_REASON, "INTEGER NOT NULL");
        }
    }
}
