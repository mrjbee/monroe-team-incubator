package org.monroe.team.jfeature;

import com.sun.org.apache.xalan.internal.xsltc.compiler.Closure;
import org.monroe.team.jfeature.description.FeatureDescription;
import org.monroe.team.jfeature.utils.Command;
import org.monroe.team.jfeature.utils.Null;
import org.monroe.team.jfeature.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 11:05 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FeaturesRegistry {

    private List<Pair<FeatureDescription,Object>> registryList = new ArrayList<Pair<FeatureDescription, Object>>(10);

    void put(FeatureDescription featureDescription, Object featureInstance) {
        registryList.add(new Pair<FeatureDescription, Object>(
                featureDescription,featureInstance));
    }

    void forEachFeature(Command<Null,Pair<FeatureDescription,Object>> command) throws Exception {
        for (Pair<FeatureDescription, Object> featureDescriptionObjectPair : registryList) {
           command.call(featureDescriptionObjectPair);
        }

    }

    public List<Object> lookup(final Class dependencyClass) {
        return collect(new Command<Boolean, FeatureDescription>() {
            @Override
            public Boolean call(FeatureDescription arg) throws Exception {
                return arg.implClass == dependencyClass;
            }
        });
    }

    private List<Object> collect(Command<Boolean,FeatureDescription> filter) {
        List<Object> answer = new ArrayList<Object>(4);
        for (Pair<FeatureDescription, Object> featureDescriptionObjectPair : registryList) {
            try {
                if (filter.call(featureDescriptionObjectPair.first)){
                    answer.add(featureDescriptionObjectPair.second);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return answer;
    }
}
