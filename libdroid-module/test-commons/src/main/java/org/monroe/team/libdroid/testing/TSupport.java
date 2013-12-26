package org.monroe.team.libdroid.testing;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;

import java.lang.reflect.Field;

/**
 * User: MisterJBee
 * Date: 9/21/13 Time: 9:45 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
@RunWith(LibdroidTestRunner.class)
public abstract class TSupport {

    public void shouldFail() {
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

    public void should(String msg, Object object, Object object1) {
        should(msg, object.equals(object1));
    }

    public void should(Object object, Object object1) {
        should("be equals "+object+" and "+ object1, object, object1);
    }

    @Before public void setupMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @After public void verifyAll(){
        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field:fields){
            if (field.isAnnotationPresent(Mock.class)){
                //mock detected
                try{
                    Mockito.verifyNoMoreInteractions(getTestFieldValue(field));
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
            throw new RuntimeException(e);
        }
    }

    public void shouldFailIfStepHere(){
        shouldFailIfStepHere("Should, but it still here");
    }
    public void shouldFailIfStepHere(String sayWhy){
        Assert.fail(sayWhy);
    }

    public static <T> OngoingStubbing<T> when(T methodCall) {
        return Mockito.when(methodCall);
    }

    public static <T> T verify(T mock) {
        return Mockito.verify(mock);
    }

    public LazyCheck lazyCheck(String message){
        return new LazyCheck(message, null);
    }

    public LazyCheck lazyCheck(String message, Answer answer){
        return new LazyCheck(message, answer);
    }

    public static class LazyCheck implements Answer{

        private boolean checked = false;
        private final Object synch = new Object();
        private final String message;
        private final Answer answer;

        public LazyCheck(String essage, Answer answer) {
            message = essage;
            this.answer = answer;
        }

        public void check(){
            synchronized (synch){
                checked = true;
                synch.notify();
            }
        }

        public void await(long ms){
            synchronized (synch){
                if (checked) return;
                try {
                    synch.wait(ms);
                    if (!checked) Assert.fail(message);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            try{
               if (answer != null){
                   return answer.answer(invocation);
               }
               return null;
            }
            finally {
                check();
            }

        }
    }

}
