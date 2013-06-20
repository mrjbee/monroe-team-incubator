package org.monroe.team.jfeature;

import org.monroe.team.jfeature.dependency.DefaultDependencyGraph;
import org.monroe.team.jfeature.dependency.DependencyGraph;
import org.monroe.team.jfeature.dependency.GraphDependencyCycleException;
import org.monroe.team.jfeature.description.FeatureDescription;
import org.monroe.team.jfeature.description.FeatureDescriptionFactory;
import org.monroe.team.jfeature.description.FeatureInjection;
import org.monroe.team.jfeature.description.InvalidDescriptionException;
import org.monroe.team.jfeature.logging.Log;
import org.monroe.team.jfeature.utils.Command;
import org.monroe.team.jfeature.utils.Null;
import org.monroe.team.jfeature.utils.Pair;

import java.util.ArrayList;
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
    private ArrayList<Object> startedFeatures;

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

    public void registrate(FeatureDescription featureDescription, Object instance) throws FeatureException {
            log.d("Registrate feature description {0}", featureDescription.detailsString());
            featuresRegistry.put(featureDescription, instance);
    }

    public void registrate(FeatureDescription featureDescription) throws FeatureException {
        try {
            registrate(featureDescription, featureDescription.featureClass.newInstance());
        } catch (Exception e) {
            throw new FeatureException(featureDescription.featureClass,featureDescription.implClass,e);
        }
    }

    public void init() throws FeatureException {
        final DependencyGraph<Object> featureDependencyGraph = new DefaultDependencyGraph<Object>();
        try {
            featuresRegistry.forEachFeature(new Command<Null, Pair<FeatureDescription, Object>>() {
                @Override
                public Null call(Pair<FeatureDescription, Object> arg) throws FeatureException{
                    log.d("Init feature {0} = {1}",arg.first.featureClass, arg.first.implClass);
                    doInject(arg.second,arg.first, featureDependencyGraph);
                    return Null.DEF;
                }
            });
        } catch (FeatureException e) {
            throw e;
        } catch (Exception e1){
            throw new RuntimeException(e1);
        }

        List<Object> featuresStartupOrder = null;
        try {
            featuresStartupOrder = featureDependencyGraph.asTopologicalSortedList();
        } catch (GraphDependencyCycleException e) {
            throw new RuntimeException(e);
        }
        startedFeatures = new ArrayList<Object>(featuresStartupOrder.size());
        boolean startupFails = false;
        for (Object feature : featuresStartupOrder) {
            try{
                doFeatureUp(feature);
            } catch (Exception e){
              startupFails = true;
              log.e(e,"Feature startup fail = {0}. Going to stop, started features.", feature);
              break;
            }
            startedFeatures.add(feature);
        }
        if(startupFails){
            deInit();
            throw new RuntimeException("Features startup fails.");
        }
    }

    private void doFeatureUp(Object feature) {
        log.i("Starting feature ({0})",feature.getClass().getName());
        if (feature instanceof ServiceFeature){
            log.i("Feature going up ({0})",feature.getClass().getName());
            ((ServiceFeature) feature).onUp();
        }
    }

    private void doFeatureDown(Object feature) {
        log.i("Stopping feature ({0})",feature.getClass().getName());
        if (feature instanceof ServiceFeature){
            log.i("Feature going down ({0})",feature.getClass().getName());
            ((ServiceFeature) feature).onDown();
        }
    }

    private void doInject(Object featureInstance, FeatureDescription featureDescription, DependencyGraph<Object> featureDependencyGraph) throws FeatureException{
        if (featureDescription.featureInjectionList.isEmpty()){
            featureDependencyGraph.addNode(featureInstance);
            return;
        }

        for (FeatureInjection featureInjection : featureDescription.featureInjectionList) {
             if (!featureInjection.description.conditionListFeature.isEmpty()){
                 throw featureDescription.issue(new UnsupportedOperationException("Injections by regexp not supported yet"));
             }
            List<Object> instances = featuresRegistry.lookup(featureInjection.description.dependencyClass);

            if (instances.isEmpty()) throw featureDescription.issue(new IllegalStateException("Unsatisfied dependency. " + featureInjection.detailsString()));

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
                if (instances.size() != 1 ){
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
        if(startedFeatures == null) return;
        for (int i = startedFeatures.size()-1; i > -1; i--) {
            try{
                doFeatureDown(startedFeatures.get(i));
            } catch (Exception e){
                log.e(e, "Feature shutdown fails = {0}",startedFeatures.get(i));
            }
        }

    }


}
