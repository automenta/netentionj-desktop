
import info.monitorenter.util.StringUtil;
import java.util.LinkedList;
import java.util.List;
import jnetention.NObject;
import jnetention.SpacePoint;
import jnetention.TimeRange;
import jnetention.possibility.SpacetimeTagPlan;
import jnetention.possibility.SpacetimeTagPlan.Possibility;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author me
 */
public class SpacetimeTagPlanTest {
 
    @Test 
    public void testPlanNObjects() {
        int numObjects = 10;
        int numTags = numObjects/2;
        int numCentroids = numObjects/3;
        double fuzziness = 1.5;
        
        
        List<NObject> n = new LinkedList();
        for (int i = 0; i < numObjects; i++) {
            NObject x = new NObject();
            
            //random time within the next few hours
            long now = System.currentTimeMillis();
            long from = (long)(Math.random() * 60 * 60 * 1000);
            long to = from + (long)(Math.random() * 60 * 60 * 1000);
            x.add("when", new TimeRange(now + from, now + to));
            
            //random space location, with max variation of 1 degree lat/lng
            double lat = Math.random() + 40;
            double lon = Math.random() + 40;            
            x.add("where", new SpacePoint(lat, lon));
            
            //random letters as tags, with random strength
            x.add(String.valueOf((char)('A' + (int)(Math.random()*numTags))), Math.random() );
            
            n.add(x);            
        }
        
        
        SpacetimeTagPlan s = new SpacetimeTagPlan(n, true, 10*60*1000, true, false);
        System.out.println(s.mapping);
        System.out.println(s.goals);
        
        assert(s.mapping.size() > 3);
        assert(s.mapping.size() <= (3+numTags));
        assert(s.goals.size() >= numObjects);
        
        List<Possibility> possibilities = s.compute(numCentroids, fuzziness);
        assert(possibilities.size() == numCentroids);
        
        for (Possibility p : possibilities) {
            System.out.println(p);
            
            assert(p.value.size() >= 2);
            
            //System.out.println("Center: " + StringUtil.arrayToString(p.getCenter()));
            //System.out.println("  Points (" + c.getPoints().size() + "): " + c.getPoints());
            
            
        }
        
        
        
         
    }
}
