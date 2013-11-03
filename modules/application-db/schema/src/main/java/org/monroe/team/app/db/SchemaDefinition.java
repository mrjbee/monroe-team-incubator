package org.monroe.team.app.db;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;
import org.monroe.team.libdroid.greenDao.AbstractDbSchemaDefinition;

/**
 * User: MisterJBee
 * Date: 11/2/13 Time: 11:18 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class SchemaDefinition extends AbstractDbSchemaDefinition {
    @Override
    protected void define(Schema schema) {
        Entity note = schema.addEntity("Note");
        note.addIdProperty();
        note.addStringProperty("text").notNull();
        note.addStringProperty("comment");
        note.addDateProperty("date");
    }
}
