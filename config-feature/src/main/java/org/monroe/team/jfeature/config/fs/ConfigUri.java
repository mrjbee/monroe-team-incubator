package org.monroe.team.jfeature.config.fs;

/**
 * User: MisterJBee
 * Date: 6/23/13 Time: 4:04 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ConfigUri {

    public final String propertiesId;
    public final String propertyId;

    public ConfigUri(String[] uriParts) {
        propertiesId = uriParts[0].trim();
        if (uriParts.length > 1){
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < uriParts.length; i++) {
                builder.append(uriParts[i].trim()).append(".");
            }
            builder.deleteCharAt(builder.length());
            propertyId = builder.toString();
        } else {
            propertyId = null;
        }
    }

    public boolean isPropertyDefined(){
        return propertyId != null;
    }
}
