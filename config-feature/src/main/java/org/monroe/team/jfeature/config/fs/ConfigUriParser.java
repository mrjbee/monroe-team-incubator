package org.monroe.team.jfeature.config.fs;

/**
 * User: MisterJBee
 * Date: 6/23/13 Time: 4:03 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ConfigUriParser {

    public ConfigUri parse(String uri) {
        String[] uriParts = uri.split("/");
        validateParts(uriParts);
        validateUriSize(uriParts);
        return new ConfigUri(uriParts);
    }

    private void validateUriSize(String[] uriParts) {
        if (uriParts.length < 1){
            throw new RuntimeException("Invalid config URI format. At least one part should be specified.");
        }
    }

    private void validateParts(String[] uriParts) {
        for (String uriPart : uriParts) {
            if (uriPart.trim().isEmpty()){
                throw new RuntimeException("Invalid config URI format. Invalid part.");
            }
        }
    }
}
