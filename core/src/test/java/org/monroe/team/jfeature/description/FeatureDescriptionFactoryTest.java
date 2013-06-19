package org.monroe.team.jfeature.description;

import org.junit.Test;
import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.FeatureInject;
import org.monroe.team.jfeature.test.support.TSupport;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 10:01 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FeatureDescriptionFactoryTest extends TSupport{

    FeatureDescriptionFactory descriptionFactory = new FeatureDescriptionFactory();

    @Test public void shouldCreateProperFeatureDescription() throws InvalidDescriptionException {
        FeatureDescription answer = descriptionFactory.getBy(SimpleFeature.class);
        should(answer.featureClass == SimpleFeature.class);
        should(answer.implClass == SimpleFeature.class);
        should(answer.registrationDetails.size() == 2);
        should(answer.registrationDetails.get("name"),"simple");
        should(answer.registrationDetails.get("feature"),"test");

        should(answer.featureInjectionList.size() == 2);
        should(answer.featureInjectionList.get(0).field.getName(),"simpleFeatureInject");
        should(answer.featureInjectionList.get(1).field.getName(),"simpleFeatureInjectList");
        should(answer.featureInjectionList.get(0).description.isMultiple == false);
        should(answer.featureInjectionList.get(1).description.isMultiple == true);
        should(answer.featureInjectionList.get(0).description.dependencyClass == SimpleFeature.class);
        should(answer.featureInjectionList.get(1).description.dependencyClass == SimpleFeature.class);
        should(answer.featureInjectionList.get(0).description.conditionListFeature.size() == 2);
        should(answer.featureInjectionList.get(0).description.conditionListFeature.get(0).matcherList.size() == 1);
        should(answer.featureInjectionList.get(0).description.conditionListFeature.get(0).matcherList.get(0).first,"name");
        should(answer.featureInjectionList.get(0).description.conditionListFeature.get(1), FeatureInjectionCondition.ANY);
        should(answer.featureInjectionList.get(1).description.conditionListFeature.size() == 0);
    }



    @Feature(impl = SimpleFeature.class, details = "name=simple; feature=test")
    public static class SimpleFeature{
        @FeatureInject ({"name=some*",""})
        SimpleFeature simpleFeatureInject;
        @FeatureInject
        private SimpleFeature[] simpleFeatureInjectList;
    }


}
