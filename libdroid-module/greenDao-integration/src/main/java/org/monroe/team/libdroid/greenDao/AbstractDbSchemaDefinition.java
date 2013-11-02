package org.monroe.team.libdroid.greenDao;

import de.greenrobot.daogenerator.Schema;

/**
 * User: MisterJBee
 * Date: 11/3/13 Time: 12:56 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public abstract class AbstractDbSchemaDefinition implements DBSchemaDefinition {

    @Override
    final public Schema generate() {
        Schema schema = new Schema(1, this.getClass().getPackage().getName());
        define(schema);
        return schema;
    }

    protected abstract void define(Schema schema);
}
