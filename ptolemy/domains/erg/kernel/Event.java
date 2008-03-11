/*

 Copyright (c) 1997-2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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

package ptolemy.domains.erg.kernel;

import java.util.List;

import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Event extends State {

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public Event(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    public void attributeChanged(Attribute attribute)
    throws IllegalActionException {
        if (attribute != isInitialState) {
            super.attributeChanged(attribute);
        }
    }

    public void fire() throws IllegalActionException {
        actions.execute();

        ERGDirector director = null;
        List<?> schedules = nonpreemptiveTransitionList();
        for (Object scheduleObject : schedules) {
            SchedulingRelation schedule = (SchedulingRelation) scheduleObject;
            if (schedule.isEnabled()) {
                double delay = schedule.getDelay();
                Event nextEvent = (Event) schedule.destinationState();
                if (director == null) {
                    ERGController controller = (ERGController) getContainer();
                    director = (ERGDirector) controller.getDirector();
                }
                if (schedule.isCanceling()) {
                    director.cancel(nextEvent);
                } else {
                    director.fireAt(nextEvent, director.getModelTime().add(
                            delay));
                }
            }
        }
    }

    public ActionsAttribute actions;

    private void _init() throws IllegalActionException,
    NameDuplicationException {
       refinementName.setVisibility(Settable.NONE);
       isInitialState.setDisplayName("isInitialEvent");
       isFinalState.setDisplayName("isFinalEvent");

       actions = new ActionsAttribute(this, "actions");
       Variable variable = new Variable(actions, "_textHeightHint");
       variable.setExpression("5");
       variable.setPersistent(false);
    }
}
