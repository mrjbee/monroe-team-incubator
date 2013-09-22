import android.util.Log;
import android.view.MotionEvent;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.monroe.team.libdroid.testing.TSupport;

/**
 * User: MisterJBee
 * Date: 9/22/13 Time: 1:12 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class SimpleTest extends TSupport{

    @Mock MotionEvent eventMock;

    @Test public void shouldPrintSomething(){
        when(eventMock.getAction()).thenReturn(1);
        Log.w("TEST","Action should be 1 = "+eventMock.getAction());
        Mockito.verify(eventMock).getAction();
    }
}
