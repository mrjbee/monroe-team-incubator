package org.monroe.team.jfeature.test.support;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 10:06 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */

@RunWith(MockitoJUnitRunner.class) @Ignore
public class TSupport {

    public  void shouldFail() {
        Assert.fail();
    }

    public void should(boolean b) {
        should("be true", b);
    }

    public void should(String msg, boolean shouldBeTrue){
       if (!shouldBeTrue){
           Assert.assertTrue("Should "+msg,shouldBeTrue);
       }
    }

    public void should(String msg, Object object,  Object object1) {
        should(msg, object.equals(object1));
    }

    public void should(Object object,  Object object1) {
        should("be equals "+object+" and "+ object1, object, object1);
    }


    @After public void verifyAll(){
        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field:fields){
            if (field.isAnnotationPresent(Mock.class)){
                //mock detected
                try{
                    Mockito.verifyNoMoreInteractions(Mockito.ignoreStubs(getTestFieldValue(field)));
                } catch (RuntimeException e){
                    throw new RuntimeException("Exception during verifying field = "+field.getName(), e);
                } catch (AssertionError assertionError){
                    System.out.println("Verification error of mock = "+field.getName());
                    throw assertionError;
                }
            }
        }
    }

    private Object getTestFieldValue(Field field){
        try {
            boolean wasAccessible = field.isAccessible();
            field.setAccessible(true);
            Object answer = field.get(this);
            field.setAccessible(wasAccessible);
            return answer;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Exception in TSupport", e);
        }
    }

    public void shouldFailIfStepHere(){
        shouldFailIfStepHere("Should, but it still here");
    }
    public void shouldFailIfStepHere(String sayWhy){
        Assert.fail(sayWhy);
    }



}
