package org.monroe.team.notification.bridge.android.delivery.socket;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.monroe.team.libdroid.testing.TSupport;

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
    }


    public SocketServer getTestInstance() {
        testInstance = new SocketServer(mockDelegate);
        return testInstance;
    }
}
