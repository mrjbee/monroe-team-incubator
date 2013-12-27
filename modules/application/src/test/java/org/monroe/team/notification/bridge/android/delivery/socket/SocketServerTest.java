package org.monroe.team.notification.bridge.android.delivery.socket;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.monroe.team.libdroid.testing.TSupport;

import java.io.IOException;

/**
 * User: MisterJBee
 * Date: 12/27/13 Time: 12:44 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class SocketServerTest extends TSupport{

    @Mock
    SocketServer.SocketServerDelegate mockDelegate;

    @Mock
    SocketServer.SocketServerCallback mockServerCallback;

    @Mock
    SocketClient mockClient;

    SocketServer testInstance;

    @Test
    public void shouldFailOnStartBecauseOfDelegateCouldntStart() throws Exception {
        testInstance = getTestInstance();
        Exception dummyException = new Exception();
        LazyCheck waitForServerThread = lazyCheck("There was no stop because of error");
        Mockito.doThrow(dummyException).when(mockDelegate).start();
        Mockito.doAnswer(waitForServerThread).when(mockServerCallback).onStopWithError(dummyException);
        testInstance.start(mockServerCallback);
        waitForServerThread.await(2000);

        Mockito.verify(mockServerCallback).onStopWithError(dummyException);
        Mockito.verify(mockDelegate).start();
        Mockito.verify(mockDelegate).stop();
        shouldStop();
    }


    @Test
    public void shouldFailOnAcceptBecauseOfDelegateAcceptError() throws Exception {
        testInstance = getTestInstance();
        IOException dummyException = new IOException();
        LazyCheck waitForServerThread = lazyCheck("There was no stop because of error");
        Mockito.doNothing().when(mockDelegate).start();
        Mockito.doThrow(dummyException).when(mockDelegate).accept();
        Mockito.doAnswer(waitForServerThread).when(mockServerCallback).onStopWithError(dummyException);
        testInstance.start(mockServerCallback);
        waitForServerThread.await(2000);

        Mockito.verify(mockServerCallback).onStopWithError(dummyException);
        Mockito.verify(mockDelegate).start();
        Mockito.verify(mockDelegate).accept();
        Mockito.verify(mockDelegate).stop();

        shouldStop();
    }

    @Test
    public void shouldStartAcceptAndStop() throws Exception {
        testInstance = getTestInstance();
        final LazyCheck waitForServerThreadStart = lazyCheck("There was no stop because of error");
        Mockito.doNothing().when(mockDelegate).start();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                should(Thread.currentThread(), testInstance.mExecutionThread);
                should(testInstance.mSocketServerCallback != null);
                should(testInstance.isStarted());
                return mockClient;
            }
        }).when(mockDelegate).accept();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                testInstance.stop();
                waitForServerThreadStart.check();
                return null;
            }
        }).when(mockServerCallback).onClient(Mockito.any(SocketClient.class));

        testInstance.start(mockServerCallback);

        waitForServerThreadStart.await(2000);
        Mockito.verify(mockServerCallback).onClient(Mockito.any(SocketClient.class));
        Mockito.verify(mockDelegate).start();
        Mockito.verify(mockDelegate).accept();
        Mockito.verify(mockDelegate).stop();

        shouldStop();
    }

    private void shouldStop() {
        should(!testInstance.isStarted());
        should(testInstance.mSocketServerCallback == null);
        should(testInstance.mExecutionThread == null);
    }

    public SocketServer getTestInstance() {
        testInstance = new SocketServer(mockDelegate);
        return testInstance;
    }
}
