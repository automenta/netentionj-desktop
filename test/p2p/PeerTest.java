package p2p;

import jnetention.Core;
import jnetention.NObject;
import jnetention.p2p.Listener;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */
public class PeerTest {
    
    boolean received = false;
    
    @Test
    public void testNet() {
            System.out.println("START");

        try {
            Core a = new Core();
            Core b = new Core();
            NObject au = a.newUser("a");
            NObject bu = b.newUser("b");
            
            a.online(10000);
            b.online(10001);

            Thread.sleep(100);
            
            
            a.net.connect("localhost", 10001);
            b.net.connect("localhost", 10000);
            
            
            a.net.listen("topic.", new Listener() {
                @Override
                public void handleMessage(final String topic, final String message) {
                    System.err.println(topic +"=" +  message);
                    received = true;
                }
            });
            b.net.send("topic1", "abc");
            
            assertTrue(a.net.getPeers().size() == 1);
            System.err.println("finished connectoin setup");
            while (!received) {                
                Thread.sleep(50);
            }
            assertTrue(true);
        }
        catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
        
    }

}
