package org.monroe.team.jfeature;

import org.monroe.team.jfeature.dependency.DefaultDependencyGraph;
import org.monroe.team.jfeature.dependency.DependencyGraph;
import org.monroe.team.jfeature.description.FeatureDescription;
import org.monroe.team.jfeature.description.FeatureDescriptionFactory;
import org.monroe.team.jfeature.description.FeatureInjection;
import org.monroe.team.jfeature.description.InvalidDescriptionException;
import org.monroe.team.jfeature.logging.Log;
import org.monroe.team.jfeature.utils.Command;
import org.monroe.team.jfeature.utils.Null;
import org.monroe.team.jfeature.utils.Pair;

import java.util.List;

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
            log.d("Registrate feature description {0}", featureDescription.detailsString());
            featuresRegistry.put(featureDescription, featureDescription.featureClass.newInstance());
        } catch (Exception e) {
           throw new FeatureException(featureDescription.featureClass,featureDescription.implClass,e);
        }
    }

    public void init() throws FeatureException {
        try {
            final DependencyGraph<Object> featureDependencyGraph = new DefaultDependencyGraph<Object>();
            featuresRegistry.forEachFeature(new Command<Null, Pair<FeatureDescription, Object>>() {
                @Override
                public Null call(Pair<FeatureDescription, Object> arg) throws FeatureException{
                    doInject(arg.second,arg.first, featureDependencyGraph);
                    return Null.DEF;
                }
            });
        } catch (FeatureException e) {
            throw e;
        } catch (Exception e1){
            throw new RuntimeException(e1);
        }
    }

    private void doInject(Object featureInstance, FeatureDescription featureDescription, DependencyGraph<Object> featureDependencyGraph) throws FeatureException{
        for (FeatureInjection featureInjection : featureDescription.featureInjectionList) {
             if (!featureInjection.description.conditionListFeature.isEmpty()){
                 throw featureDescription.issue(new UnsupportedOperationException("Injections by regexp not supported yet"));
             }
            List<Object> instances = featuresRegistry.lookup(featureInjection.description.dependencyClass);

            if (instances.isEmpty()) throw featureDescription.issue(new IllegalStateException("Unsatisfied dependency." + featureInjection.description));

            if (featureInjection.description.isMultiple){
                try {
                    featureInjection.setArray(featureInstance, featureInjection.description.dependencyClass, instances);
                    for (Object instance : instances) {
                        featureDependencyGraph.addDependency(featureInstance, instance);
                    }
                } catch (IllegalAccessException e) {
                    featureDescription.issue(e);
                }
            } else {
                if (instances.size() != 0 ){
                   featureDescription.issue(new IllegalStateException("Too much candidates."+featureInjection.detailsString()));
                } else {
                    try {
                        featureInjection.set(featureInstance, instances.get(0));
                        featureDependencyGraph.addDependency(featureInstance, instances.get(0));
                    } catch (IllegalAccessException e) {
                        featureDescription.issue(e);
                    }
                }
            }
        }
    }

    public void deInit() throws FeatureException {

    }
}
