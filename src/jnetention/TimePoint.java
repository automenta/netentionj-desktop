package jnetention;

/**
 *
 * @author me
 */
public class TimePoint {

    public long at;    

    public TimePoint(long t) {
        this.at = t;
    }

    public static TimePoint get(NObject n) {   
        return n.firstValue(TimePoint.class);
    }

    
}
