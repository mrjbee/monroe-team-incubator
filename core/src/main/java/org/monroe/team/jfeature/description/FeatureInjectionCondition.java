package org.monroe.team.jfeature.description;

import org.monroe.team.jfeature.utils.Pair;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 9:29 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FeatureInjectionCondition {

    public static final FeatureInjectionCondition ANY = new FeatureInjectionCondition(Collections.EMPTY_LIST);

    public final List<Pair<String, Pattern>> matcherList;

    public FeatureInjectionCondition(List<Pair<String, Pattern>> matcherList) {
       this.matcherList = Collections.unmodifiableList(matcherList);
    }
}
