package org.monroe.team.app.db.schema;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Schema;

/**
 * User: MisterJBee
 * Date: 11/2/13 Time: 11:20 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class SchemaGenerator {

    public static void main(String[] args) throws Exception {
        Schema schema = new SchemaDefinition().generate();
        new DaoGenerator().generateAll(schema, "gen");
    }

}
