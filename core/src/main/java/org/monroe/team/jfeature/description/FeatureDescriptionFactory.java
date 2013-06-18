package org.monroe.team.jfeature.description;

import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.FeatureInject;
import org.monroe.team.jfeature.utils.Pair;
import sun.dc.path.PathError;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 9:35 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FeatureDescriptionFactory {

    public FeatureDescription getBy(Class featureClass) throws InvalidDescriptionException{
        try{
        //TODO: check that interface are accepted by class
        Feature featureAnnotation = (Feature) featureClass.getAnnotation(Feature.class);
        return new FeatureDescription(featureClass,
                featureAnnotation.impl(),
                parseDetails(featureAnnotation.details()),
                parseInjections(featureClass));
        } catch (Exception e){
           throw new InvalidDescriptionException(featureClass, e);
        }
    }

    private List<FeatureInjection> parseInjections(Class featureClass) {
        List<FeatureInjection> featureInjectionList = new ArrayList<FeatureInjection>(2);
        Field[] fields = featureClass.getDeclaredFields();
        for (Field field : fields) {
            FeatureInject featureInjectAnn = field.getAnnotation(FeatureInject.class);
            if (featureInjectAnn != null){
                featureInjectionList.add(
                        new FeatureInjection(field,
                               parseInjectionMetaData(featureInjectAnn, field)));
            }
        }
        return featureInjectionList;
    }

    private FeatureInjectionDescription parseInjectionMetaData(FeatureInject featureInjectAnn, Field field) {
        return new FeatureInjectionDescription(
                (field.getType().isArray())? field.getType().getComponentType():field.getType(),
                field.getType().isArray(),
                parseConditions(featureInjectAnn.value()));
    }

    private List<FeatureInjectionCondition> parseConditions(String[] value) {
        List<FeatureInjectionCondition> featureInjectionConditions = new ArrayList<FeatureInjectionCondition>();
        for (String pair : value) {
           if ("".equals(pair)){
               featureInjectionConditions.add(FeatureInjectionCondition.ANY);
           } else {
               String[] parsedConditions = pair.split(";");
               List<Pair<String,Pattern>> matcherList = new ArrayList<Pair<String, Pattern>>(parsedConditions.length);
               for (String parsedCondition : parsedConditions) {
                   //TODO add pattern condition
                   String[] conditionPair = pair.split("=");
                   Pattern pattern = Pattern.compile(conditionPair[1]);
                   matcherList.add(new Pair<String, Pattern>(conditionPair[0],pattern));
               }
               featureInjectionConditions.add(new FeatureInjectionCondition(matcherList));
           }
        }
        return featureInjectionConditions;
    }

    private Map<String,String> parseDetails(String details) {
      if ("".equals(details))
        return Collections.EMPTY_MAP;
      String[] parsedPairs = details.split(";");
      Map<String,String> detailsMap = new HashMap<String, String>(parsedPairs.length);
      for (String parsedPair : parsedPairs) {
          String[] parsed = parsedPair.split("=");
          //TODO; check that two parts
          detailsMap.put(parsed[0].trim(),parsed[1].trim());
      }
      return detailsMap;
    }
}
