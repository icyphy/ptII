/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.domains.erg.lib;

import ptolemy.actor.Actor;
import ptolemy.actor.Initializable;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.erg.kernel.ERGController;
import ptolemy.domains.erg.kernel.ERGDirector;
import ptolemy.domains.erg.kernel.Event;
import ptolemy.domains.erg.kernel.EventQueueDebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;

//////////////////////////////////////////////////////////////////////////
//// EventQueueDebugger

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class EventQueueDebugger extends SingletonAttribute
        implements EventQueueDebugListener, Initializable {

    /**
     *  @param container
     *  @param name
     *  @throws NameDuplicationException
     *  @throws IllegalActionException
     */
    public EventQueueDebugger(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        active = new Parameter(this, "active");
        active.setTypeEquals(BaseType.BOOLEAN);
        active.setToken(BooleanToken.TRUE);

        debuggerClass = new StringParameter(this, "debuggerClass");

        _attachText("_iconDescription", _ICON_DESCRIPTION);
    }

    public void addInitializable(Initializable initializable) {
    }

    public void initialize() throws IllegalActionException {
    }

    public void insertActor(int position, Time time, Actor actor,
            ArrayToken arguments) {
    }

    public void insertEvent(int position, Time time, Event event,
            ArrayToken arguments) {
        System.out.println("Schedule " + event.getName() + " at " + time +
                " at position " + position);
    }

    public void preinitialize() throws IllegalActionException {
        NamedObj container = getContainer();
        if (container instanceof ERGController) {
            ERGDirector director = ((ERGController) container).director;
            director.addDebugListener(this);
        }
    }

    public void removeEvent(int position, boolean isCancelled) {
        if (isCancelled) {
            System.out.println("Cancel event at position " + position);
        } else {
            System.out.println("Process event at position " + position);
        }
    }

    public void removeInitializable(Initializable initializable) {
    }

    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        NamedObj oldContainer = getContainer();
        if (oldContainer instanceof Initializable) {
            ((Initializable) oldContainer).removeInitializable(this);
        }
        super.setContainer(container);
        if (container instanceof Initializable) {
            ((Initializable) container).addInitializable(this);
        }
    }

    public void wrapup() throws IllegalActionException {
        NamedObj container = getContainer();
        if (container instanceof ERGController) {
            ERGDirector director = ((ERGController) container).director;
            director.removeDebugListener(this);
        }
    }

    public Parameter active;

    public StringParameter debuggerClass;

    private static final String _ICON_DESCRIPTION = "<svg>"
        + "<rect x=\"0\" y=\"0\" width=\"60\" height=\"10\""
        + "  style=\"fill:#C0C0C0\"/>" + "</svg>";
}
