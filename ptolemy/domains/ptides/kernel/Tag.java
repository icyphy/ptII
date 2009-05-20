package ptolemy.domains.ptides.kernel;

import ptolemy.actor.util.Time;

/** Holds a timestamp microstep pair
 */
public class Tag implements Comparable{
    public Time timestamp;
    public int microstep;
    
    public Tag(Time timestamp, int microstep) {
        this.timestamp = timestamp;
        this.microstep = microstep;
    }
    
    public int compareTo(Object other) {
        Tag tag2 = (Tag) other;
        if (timestamp.compareTo(tag2.timestamp) == 1) {
            return 1;
        } else if (timestamp.compareTo(tag2.timestamp) == -1) {
            return -1;
        } else {
            if (microstep > tag2.microstep) {
                return 1;
            } else if (microstep < tag2.microstep) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}