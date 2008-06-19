package ptolemy.domains.tt.tdl.kernel;

import java.util.Comparator;

import ptolemy.actor.Actor;
import ptolemy.actor.util.Time;

/**
 * A node describing a TDL action. TODO: better to derive?.
 * @author Patricia Derler
 *
 */
public class TDLGraphNode {

    public TDLGraphNode(long time, int actionType, Object actor) {
        this.time = time;
        this.actionType = actionType;
        this.object = actor;
    }
    
    public static final int WRITEOUTPUT = 0;
    public static final int WRITEACTUATOR = 1;
    public static final int MODESWITCH = 2;
    public static final int READSENSOR = 3;
    public static final int READINPUT = 4;
    public static final int EXECUTETASK = 5;
    
    public long time;
    public int actionType;
    public Object object;
    
    
    
    /**
     * This class compares two 
     * @author Patricia Derler
     */
    public static class TDLGraphNodeComparator implements Comparator  {

        /**
         * Compare two TDLEvents. Two TDL Events are the same if all
         * attributes are the same: timestamp, actionType and actor. For
         * comparison of an actor, the actor name which has to be unique
         * in the model is used.
         * @param event1 First event.
         * @param event2 Second event.
         * @return The result of the comparison of the two TDL events.
         */
        public int compare(Object event1, Object event2) {
            TDLGraphNode tdlEvent1 = (TDLGraphNode) event1;
            TDLGraphNode tdlEvent2 = (TDLGraphNode) event2;
            long compareTime = tdlEvent1.time - tdlEvent2.time;
            if (compareTime != 0)
                return (int)compareTime;
            else if (tdlEvent1.actionType != tdlEvent2.actionType)
                return tdlEvent1.actionType - tdlEvent2.actionType;
            else
                return tdlEvent1.object.toString().compareTo(tdlEvent2.object.toString());
        }
        
        
    }
    
}
