package org.monroe.team.jfeature.shared.api;

import java.util.List;

/**
 * User: MisterJBee
 * Date: 6/20/13 Time: 12:36 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface ConfigFeature {
    //URI: area/section/id
    public <Type> Type getValue(String uri, Class<Type> answerType);
    public void setValue(String uri, Object value);
    public List<String> discoverUri(String uriBase);
}
