import android.util.Log;
import android.view.MotionEvent;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.monroe.team.libdroid.testing.TSupport;
import org.powermock.core.classloader.annotations.PrepareForTest;

/**
 * User: MisterJBee
 * Date: 9/22/13 Time: 1:12 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
@PrepareForTest(MotionEvent.class)
public class SimpleTest extends TSupport{

    @Mock MotionEvent eventMock;

    @Test public void shouldPrintSomething(){
        Mockito.when(eventMock.getAction()).thenReturn(1);
        should(1 == eventMock.getAction());
        Mockito.verify(eventMock).getAction();
    }
}
