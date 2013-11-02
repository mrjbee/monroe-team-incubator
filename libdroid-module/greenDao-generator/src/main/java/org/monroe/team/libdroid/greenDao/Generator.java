package org.monroe.team.libdroid.greenDao;

import de.greenrobot.daogenerator.DaoGenerator;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * User: MisterJBee
 * Date: 11/3/13 Time: 12:45 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class Generator {

    public static void main(String[] args) throws Exception {
        List<DBSchemaDefinition> schemaDefinitions = collectSchemas();
        String generationPath = getPojoPath();
        new File(generationPath).mkdirs();
        for (DBSchemaDefinition schemaDefinition : schemaDefinitions) {
            new DaoGenerator().generateAll(schemaDefinition.generate(),
                    generationPath);
        }
   }

    private static List<DBSchemaDefinition> collectSchemas() {

        Reflections reflections = new Reflections("org");

        Set<Class<? extends DBSchemaDefinition>> subTypes =
                reflections.getSubTypesOf(DBSchemaDefinition.class);
        List<DBSchemaDefinition> answer = new ArrayList<DBSchemaDefinition>(subTypes.size());

        for (Class<? extends DBSchemaDefinition> subType : subTypes) {
            if ( !Modifier.isAbstract(subType.getModifiers()) &&
                 !Modifier.isInterface(subType.getModifiers())){
                try {
                    DBSchemaDefinition definition = subType.newInstance();
                    answer.add(definition);
                } catch (Exception e) {
                    throw new RuntimeException("Issue with allocating schema by class = "+subType,e);
                }
            }
        }


        return answer;
    }

    private static String getPojoPath() {
        return "db-pojo-src";
    }

}
