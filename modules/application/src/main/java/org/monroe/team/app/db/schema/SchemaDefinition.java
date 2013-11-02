package org.monroe.team.app.db.schema;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * User: MisterJBee
 * Date: 11/2/13 Time: 11:18 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class SchemaDefinition {
    public Schema generate() {
        Schema schema = new Schema(1, "test.code.gen");
        Entity note = schema.addEntity("Note");
        note.addIdProperty();
        note.addStringProperty("text").notNull();
        note.addStringProperty("comment");
        note.addDateProperty("date");
        return schema;
    }
}
