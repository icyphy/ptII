/*
@Copyright (c) 2008-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                        PT_COPYRIGHT_VERSION_2
                        COPYRIGHTENDKEY


 */
package ptolemy.domains.tdl.kernel;

import java.util.Comparator;

import ptolemy.actor.util.Time;

/**
 * A TDL action. Used in the TDLActionsGraph.
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 8.0
 *
 */
public class TDLAction {

    /**
     * Create a new TDLGraphNode.
     * @param time Time stamp the TDL action has to be done at.
     * @param actionType Type of TDL action.
     * @param actor Actor on which the TDL action has to be performed on.
     */
    public TDLAction(Time time, int actionType, Object actor) {
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
    public static final int AFTERMODESWITCH = 3;

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
     * The action after a task is output.
     */
    public static final int AFTERTASKOUTPUTS = 7;

    /**
     * Time stamp for the TDL action.
     */
    public Time time;

    /**
     * Type of TDL action. This is one of the constants defined above.
     */
    public int actionType;

    /**
     * Actor the TDL action has to be performed on.
     */
    public Object object;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public String toString() {
        return actionType + "@" + time + "@" + object;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TDLAction) {
            TDLAction action = (TDLAction) obj;
            if (this.time.equals(action.time) && this.object == action.object
                    && this.actionType == action.actionType) {
                return true;
            }
        }
        return false;
    }

    /** Return a hash code value for this action.
     *  @return A hash code value for this token.
     */
    @Override
    public int hashCode() {
        // See http://www.geocities.com/technofundo/tech/java/equalhash.html
        // for suggestions on hashCode
        return 31 + time.hashCode() + actionType + object.hashCode();
    }

    /**
     * Return true if two actions are the same.
     * @param action The action to compare against this action
     * @param modePeriod The mode period.
     * @return true if the times are equal (==), the TDL actions are
     * equal (==) and the action times are equal (==).
     */
    public boolean sameActionAs(TDLAction action, Time modePeriod) {
        long time1 = this.time.getLongValue() % modePeriod.getLongValue();
        long time2 = action.time.getLongValue() % modePeriod.getLongValue();
        if (time1 == time2 && this.object == action.object
                && this.actionType == action.actionType) {
            return true;
        }
        return false;
    }

    /**
     * A class that compares two TDL actions.
     */
    public static class TDLActionComparator implements Comparator {

        /**
         * Compare two TDLEvents. Two TDL Events are the same if all
         * attributes are the same: timestamp, actionType and actor. For
         * comparison of an actor, the actor name which has to be unique
         * in the model is used.
         * @param event1 First event.
         * @param event2 Second event.
         * @return The result of the comparison of the two TDL events.
         */
        @Override
        public int compare(Object event1, Object event2) {
            TDLAction tdlEvent1 = (TDLAction) event1;
            TDLAction tdlEvent2 = (TDLAction) event2;
            long compareTime = tdlEvent1.time.compareTo(tdlEvent2.time);
            if (compareTime != 0) {
                return (int) compareTime;
            } else if (tdlEvent1.actionType != tdlEvent2.actionType) {
                return tdlEvent1.actionType - tdlEvent2.actionType;
            } else {
                return tdlEvent1.object.toString().compareTo(
                        tdlEvent2.object.toString());
            }
        }
    }
}
