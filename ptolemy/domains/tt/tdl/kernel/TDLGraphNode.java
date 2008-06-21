package ptolemy.domains.tt.tdl.kernel;

import java.util.Comparator;

import ptolemy.actor.Actor;
import ptolemy.actor.util.Time;

/**
 * A node describing a TDL action. 
 * 
 * TODO: better to derive?. At the moment, deriving from ptolemy.graph.Node
 * is not possible because Node is final.
 * @author Patricia Derler
 *
 */
public class TDLGraphNode {

    /**
     * Create a new TDLGraphNode.
     * @param time Time stamp the TDL action has to be done at.
     * @param actionType Type of TDL action.
     * @param actor Actor on which the TDL action has to be performed on.
     */
    public TDLGraphNode(long time, int actionType, Object actor) {
        this.time = time;
        this.actionType = actionType;
        this.object = actor;
    }
    
    /**
     * Write output of a task.
     */
    public static final int WRITEOUTPUT = 0;
    
    /**
     * Write actuator which is the output of a TDLModule.
     */
    public static final int WRITEACTUATOR = 1;
    
    /**
     * Test a mode switch guard and execute the mode switch if the guard
     * evaluates to true.
     */
    public static final int MODESWITCH = 2;
    
    /**
     * If a mode switch is not taken, this is the next action. It does not do anything but is
     * required for the TDL ActionsGraph.
     */
    public static final int MODESWITCHNOTTAKEN = 3;
    
    /**
     * Read a sensor value which is the input of a TDLModule.
     */
    public static final int READSENSOR = 4;
    
    /**
     * Read the input of a task.
     */
    public static final int READINPUT = 5;
    
    /**
     * Execute a task.
     */
    public static final int EXECUTETASK = 6;
    
    /**
     * Time stamp for the TDL action.
     */
    public long time;
    
    /**
     * Type of TDL action. This is one of the constants defined above.
     */
    public int actionType;
    
    /**
     * Actor the TDL aciton has to be performed on.
     */
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
