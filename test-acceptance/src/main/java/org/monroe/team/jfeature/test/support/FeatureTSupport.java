package org.monroe.team.jfeature.test.support;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.FeatureContext;
import org.monroe.team.jfeature.FeatureException;
import org.monroe.team.jfeature.FeatureInject;
import org.monroe.team.jfeature.description.FeatureDescription;
import org.monroe.team.jfeature.description.FeatureInjection;
import org.monroe.team.jfeature.description.FeatureInjectionDescription;
import org.monroe.team.jfeature.logging.Log;
import org.monroe.team.jfeature.logging.LogFactory;
import org.monroe.team.jfeature.shared.api.LoggingFeature;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 6/20/13 Time: 1:33 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public abstract class FeatureTSupport extends TSupport{


    private FeatureContext context;

    @Before public void createAndStartContext(){
        try{
            this.context = null;
            FeatureContext context = new FeatureContext(log);

            initTestedFeature(context);
            initLoggingFeature(context);

            initMockFeatures(context);

            context.init();
            this.context = context;
        } catch (FeatureException e){
            throw new AssertionError(e);
        }
    }

    protected void initMockFeatures(FeatureContext context) throws FeatureException {
        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field:fields){
            if (field.isAnnotationPresent(Mock.class)){
                try {
                    field.setAccessible(true);
                    Object instance = field.get(this);
                    context.registrate(new FeatureDescription(field.getType(), field.getType(), Collections.EMPTY_MAP, Collections.EMPTY_LIST), instance);
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }
        }
    };

    protected void initLoggingFeature(FeatureContext context) throws FeatureException {
        //init default logging
        context.registrate(new FeatureDescription(TestLoggerFeature.class,LoggingFeature.class, Collections.EMPTY_MAP, Collections.EMPTY_LIST), new TestLoggerFeature());
    }

    private void initTestedFeature(FeatureContext context) throws FeatureException {
        List<FeatureInjection> featureInjectionDescriptionList = new ArrayList<FeatureInjection>();
        featureInjectionDescriptionList.add(extractTestDetails());
        FeatureDescription featureDescription = new FeatureDescription(this.getClass(),this.getClass(), Collections.EMPTY_MAP,featureInjectionDescriptionList);
        context.registrate(featureDescription, this);
        context.registrateFeatureClass(getTestedImplClass());
    }

    @After public void stopContext(){
        if (context != null){
            try {
                context.deInit();
            } catch (FeatureException e) {
               throw new AssertionError(e);
            }
        }
    }

    private FeatureInjection extractTestDetails() {
        Field testFeatureField = getTestFeatureField();
        FeatureInjectionDescription answerDescription = new FeatureInjectionDescription(testFeatureField.getType(),false, Collections.EMPTY_LIST);
        return new FeatureInjection(testFeatureField, answerDescription);
    }

    protected abstract Class getTestedImplClass();

    private Field getTestFeatureField() {
        Field[] fields = this.getClass().getDeclaredFields();
        Field testFeatureField = null;
        for (Field field : fields) {
            if (field.getName().equals("testFeature")){
               testFeatureField = field;
               break;
            }
        }
        should("No testFeature field found", testFeatureField!=null);
        return testFeatureField;
    }

    private static class TestLoggerFeature implements LoggingFeature{

        @Override
        public Log get(String featureName) {
            return log;
        }
    }

    private final static Log log = new Log() {

        private String buildMessage(String msgPatter, Object[] args) {
            return MessageFormat.format(msgPatter, args);
        }

        @Override
        public void v(String msgPatter, Object... args) {
        }

        @Override
        public void d(String msgPatter, Object... args) {
        }

        @Override
        public void i(String msgPatter, Object... args) {
        }

        @Override
        public void w(String msgPatter, Object... args) {
            System.out.printf(buildMessage(msgPatter,args));
        }

        @Override
        public void e(String msgPatter, Object... args) {
            System.out.printf(buildMessage(msgPatter,args));
        }

        @Override
        public void w(Exception exception, String msgPatter, Object... args) {
            System.out.printf(buildMessage(msgPatter,args));
            if (exception != null) exception.printStackTrace();
        }

        @Override
        public void e(Exception exception, String msgPatter, Object... args) {
            System.out.printf(buildMessage(msgPatter,args));
            if (exception != null) exception.printStackTrace();
        }
    };
}
