package jnetention.possibility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jnetention.NObject;
import jnetention.SpacePoint;
import jnetention.TimePoint;
import jnetention.TimeRange;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.FuzzyKMeansClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

/**
 *
 * @author me
 * 
 * http://commons.apache.org/proper/commons-math/javadocs/api-3.3/org/apache/commons/math3/ml/clustering/FuzzyKMeansClusterer.html
 */
public class SpacetimeTagPlan {
    
    public final TagVectorMapping mapping;
    public final List<Goal> goals = new ArrayList();
    
    
    private final double timeWeight = 1.0;  //normalized to seconds
    private final double spaceWeight = 1.0; //meters
    private final double altWeight = 1.0; //meters
    private final double tagWeight = 1.0; 
    
    
    public static class TagVectorMapping extends ArrayList<String> {
        public final long timePeriod;
        private static final TimePoint NullTimePoint = new TimePoint(-1);
        double[] min, max;
        
        /**
         * 
         * @param timePeriod  in ms (unixtime)
         */
        public TagVectorMapping(long timePeriod) {
            this.timePeriod = timePeriod;
        }
        
        /** reset before generating a new sequence of goals */
        public void reset() {
            min = max = null;
        }
        
        public List<Goal> newGoals(NObject o) {
            boolean firstGoal = false;
            if (min == null) {
                firstGoal = true;
                min = new double[size()];
                max = new double[size()];                
            }
            
            List<Goal> goals = new LinkedList();
            Map<String,Double> ts = o.getTagStrengths();
            
            
            SpacePoint sp = null;
                        
            List<TimePoint> times = times = new ArrayList(1);
            
            if (get(0).equals("time")) {
                //convert time ranges to a set of time points
                TimeRange tr = TimeRange.get(o);
                if (tr != null) {                    
                    times.addAll(tr.discretize(timePeriod));
                }
                else {
                    TimePoint tp = TimePoint.get(o);
                    if (tp!=null) {
                        times.add(tp);
                    }
                    else {
                        //no timepoint, ignore this NObject
                        return goals;
                    }
                }
            }
            else {
                //add a null timepoint so the following iteration occurs
                times.add(NullTimePoint);
            }
            
            for (TimePoint currentTime : times) {
                
                double[] d = new double[this.size()];
                int i = 0;
                
                for (String s : this) {
                    if (s.equals("lat")) {
                        sp = SpacePoint.get(o);
                        if (sp==null) {
                            //this nobject is invalid, return; goals will be empty
                            return goals;
                        }
                        d[i] = sp.lat;
                    }
                    else if (s.equals("lon")) {
                        d[i] = sp.lon;
                    }
                    else if (s.equals("time")) {
                        d[i] = currentTime.at;
                    }
                    else if (s.equals("alt")) {
                        d[i] = sp.alt;
                    }
                    else {
                        Double strength = ts.get(s);
                        if (strength!=null) {
                            d[i] = strength;
                        }
                    }
                    i++;                    
                }
                if (firstGoal) {
                    System.arraycopy(d, 0, min, 0, d.length);
                    System.arraycopy(d, 0, max, 0, d.length);
                }
                else {
                    for (int j = 0; j < d.length; j++) {
                        if (d[j] < min[j]) min[j] = d[j];
                        if (d[j] > max[j]) max[j] = d[j];
                    }
                }
                    
                goals.add(new Goal(this, d));
            }

            return goals;
        }

        /** normalize (to 0..1.0) a collection of Goals with respect to the min/max calculated during the prior goal generation */
        public void normalize(Collection<Goal> goals) {
            
            for (Goal g : goals) {
                double d[] = g.getPoint();
                for (int i = 0; i < d.length; i++) {
                    double MIN = min[i];
                    double MAX = max[i];
                    if (MIN!=MAX) {
                        d[i] = (d[i] - MIN) / (MAX-MIN);
                    }
                    else {
                        d[i] = 0.5;
                    }
                }
            }
        }
        
        
    }
    
    /** a point in goal-space; the t parameter is included for referencing what the dimensions mean */
    public static class Goal extends DoublePoint {
        private final TagVectorMapping mapping;

        public Goal(TagVectorMapping t, double[] v) {
            super(v);
            this.mapping = t;            
        }
        
        public NObject newNObject() {
            NObject n = new NObject();
            //TODO iterate through dimensions, add tags according to mapping
            return n;
        }
        
        
    }
    
    //TODO add a maxDimensions parameter that will exclude dimensions with low aggregate strength
    
    //TODO support negative strengths to indicate avoidance
    
    /**
     * 
     * @param n list of objects
     * @param tags whether to involve tags
     * @param timePeriod  time period of discrete minimum interval; set to zero to not involve time as a dimension
     * @param space whether to involve space latitude & longitude
     * @param spaceAltitude whether to involve space altitude
     */
    public SpacetimeTagPlan(List<NObject> n, boolean tags, long timePeriod, boolean space, boolean spaceAltitude) {
        
        //1. compute mapping
        this.mapping = new TagVectorMapping(timePeriod);
        
        boolean time = timePeriod > 0;
        
        if (time)
            mapping.add("time");
        if (space) {
            mapping.add("lat");
            mapping.add("lon");
        }
        if (spaceAltitude) {
            mapping.add("alt");
        }
        
        //TODO filter list of objects according to needed features for the clustering parameters
        
        if (tags) {
            Set<String> uniqueTags = new HashSet();
            for (NObject o : n) {
                uniqueTags.addAll(o.getTags());
            }
            mapping.addAll(uniqueTags);
        }
        
        //2. compute goal vectors 
        for (NObject o : n) {
            goals.addAll(mapping.newGoals(o));
        }
        
        //3. normalize
        mapping.normalize(goals);
        
            
    }
    
    public List<Goal> compute(int centroids, double fuzziness) {
        //4. weight
        
        
        //5. cluster
        int maxIterations = 10000;
        DistanceMeasure distanceMetric = new EuclideanDistance();
        
        
        FuzzyKMeansClusterer<Goal> clusterer = new FuzzyKMeansClusterer<Goal>(centroids, fuzziness, maxIterations, distanceMetric);
        
        
        //6. transform centroids into possiblities
        
        return null;
    }
    
    public TagVectorMapping getMapping() {
        return mapping;
    }
    
 
    
    
}
