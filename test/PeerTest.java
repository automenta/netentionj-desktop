import java.util.List;
import jnetention.NObject;
import jnetention.Core;
import jnetention.Tag;
import net.tomp2p.futures.FutureDiscover;
import org.junit.Test;

/**
 *
 * @author me
 */
public class PeerTest {
    
    @Test
    public void testDB() {
        Core p = new Core();
        int initialSize = p.data.size(); //may include a default user
        
        
        NObject x = new NObject("a");
        p.save(x);
        
        assert(p.data.size() == 1 + initialSize);
        
        p.remove(x);
        assert(p.data.size() == 0 + initialSize);
        
        NObject u = p.newUser("Anonymous");
        assert(p.getUsers().size() == 1 + initialSize);
     
        //assert(u.getTags().size() == 2 + initialSize);
        assert(u.hasTag(Tag.User.toString()));
        
    }
    
    @Test
    public void testNet() {
        try {
            Core a = new Core().online(10000);
            Core b = new Core().online(10001);
            NObject au = a.newUser("a");
            NObject bu = a.newUser("b");
            
            final FutureDiscover fd = a.connect("localhost", 10001);
            
            /*fd.addListener(new BaseFutureAdapter<BaseFuture>() {
                @Override public void operationComplete(BaseFuture f) throws Exception {
                  assert(fd.isSuccess());
                }                
            });*/
            
            fd.awaitUninterruptibly();
            assert(fd.isSuccess());
            
            a.publish(au);
            b.publish(bu);
            
            Object bau = b.netGet(au.id);
            assert(bau.getClass().equals(NObject.class));
            NObject nbau = (NObject)bau;
            
            assert(nbau.id.equals(au.id));
            
            List<NObject> bh = b.netGetTagged("Human");
            assert(bh.size()==2);
            assert(bh.get(0).id!=bh.get(1).id);
            
        }
        catch (Exception e) {
            assert(false);
        }
    }
}
