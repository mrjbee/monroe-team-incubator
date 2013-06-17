package org.monroe.team.jfeature;

import org.monroe.team.jfeature.description.FeatureDescription;
import org.monroe.team.jfeature.description.FeatureDescriptionFactory;
import org.monroe.team.jfeature.description.FeatureInjection;
import org.monroe.team.jfeature.description.InvalidDescriptionException;
import org.monroe.team.jfeature.logging.Log;
import org.monroe.team.jfeature.utils.Command;
import org.monroe.team.jfeature.utils.Null;
import org.monroe.team.jfeature.utils.Pair;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 12:34 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FeatureContext {

    private final Log log;
    private FeatureDescriptionFactory featureDescriptionFactory = new FeatureDescriptionFactory();
    private FeaturesRegistry featuresRegistry = new FeaturesRegistry();

    public FeatureContext(Log contextLogger) {
         log = contextLogger;
    }

    public void registrateFeatureClass(Class featureClass) throws FeatureException {
        log.v("Start processing feature class {0}", featureClass);
        try {
           FeatureDescription featureDescription = featureDescriptionFactory.getBy(featureClass);
           registrate(featureDescription);
        } catch (InvalidDescriptionException e) {
            throw new FeatureException(featureClass, Object.class, e);
        }
    }

    private void registrate(FeatureDescription featureDescription) throws FeatureException {
        try {
            featuresRegistry.put(featureDescription, featureDescription.featureClass.newInstance());
        } catch (Exception e) {
           throw new FeatureException(featureDescription.featureClass,featureDescription.implClass,e);
        }
    }

    public void init() throws FeatureException {
        try {
            featuresRegistry.forEachFeature(new Command<Null, Pair<FeatureDescription, Object>>() {
                @Override
                public Null call(Pair<FeatureDescription, Object> arg) throws FeatureException{
                    doInject(arg.second,arg.first);
                    return Null.DEF;
                }
            });
        } catch (FeatureException e) {
            throw e;
        } catch (Exception e1){
            throw new RuntimeException(e1);
        }
    }

    private void doInject(Object featureInstance, FeatureDescription featureDescription) throws FeatureException{
        for (FeatureInjection featureInjection : featureDescription.featureInjectionList) {
        }
    }

    public void deInit() throws FeatureException {

    }
}
