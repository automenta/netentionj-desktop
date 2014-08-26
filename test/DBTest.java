
import jnetention.Core;
import jnetention.NObject;
import jnetention.Tag;
import org.junit.Test;


/**
 *
 * @author me
 */
public class DBTest {

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

}
