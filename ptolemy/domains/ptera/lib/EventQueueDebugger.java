/* An attribute to debug the event queue of a Ptera model.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.domains.ptera.lib;

import ptolemy.actor.Actor;
import ptolemy.actor.Initializable;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.domains.ptera.kernel.EventQueueDebugListener;
import ptolemy.domains.ptera.kernel.PteraController;
import ptolemy.domains.ptera.kernel.PteraDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;

//////////////////////////////////////////////////////////////////////////
//// EventQueueDebugger

/**
 An attribute to debug the event queue of a Ptera model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class EventQueueDebugger extends SingletonAttribute implements
        EventQueueDebugListener, Initializable {

    /** Construct an attribute with the given container and name.
     *  If an attribute already exists with the same name as the one
     *  specified here, that is an instance of class
     *  SingletonAttribute (or a derived class), then that
     *  attribute is removed before this one is inserted in the container.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   attribute with this name, and the class of that container is not
     *   SingletonAttribute.
     */
    public EventQueueDebugger(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        active = new Parameter(this, "active");
        active.setTypeEquals(BaseType.BOOLEAN);
        active.setToken(BooleanToken.TRUE);

        _attachText("_iconDescription", _ICON_DESCRIPTION);
    }

    /** Do nothing.
     *
     *  @param initializable Not used.
     */
    @Override
    public void addInitializable(Initializable initializable) {
    }

    /** Do nothing.
     *
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public void initialize() throws IllegalActionException {
    }

    /** Invoked when an actor is inserted into the event queue.
     *
     *  @param position The position at which the actor is inserted.
     *  @param time The time at which the actor is scheduled to fire.
     *  @param actor The actor.
     *  @param arguments Arguments to the actor, which must be either an
     *   ArrayToken or a RecordToken, or null.
     */
    @Override
    public void insertActor(int position, Time time, Actor actor,
            Token arguments) {
    }

    /** Invoked when an event is inserted into the event queue.
     *
     *  @param position The position at which the event is inserted.
     *  @param time The time at which the event is scheduled to fire.
     *  @param event The event.
     *  @param arguments Arguments to the event, which must be either an
     *   ArrayToken or a RecordToken, or null.
     */
    @Override
    public void insertEvent(int position, Time time, Event event,
            Token arguments) {
        try {
            boolean isActive = ((BooleanToken) active.getToken())
                    .booleanValue();
            if (isActive) {
                System.out.println("Schedule " + event.getName() + " at "
                        + time + " at position " + position);
            }
        } catch (IllegalActionException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Add this attribute as a debug listener to the director.
     *
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        NamedObj container = getContainer();
        if (container instanceof PteraController) {
            PteraDirector director = ((PteraController) container).director;
            director.addDebugListener(this);
        }
    }

    /** Invoked when an event or actor is removed from the event queue.
     *
     *  @param position The position of the event or actor.
     *  @param isCancelled Whether the removal is due to cancellation or
     *   successful processing.
     */
    @Override
    public void removeEvent(int position, boolean isCancelled) {
        try {
            boolean isActive = ((BooleanToken) active.getToken())
                    .booleanValue();
            if (isActive) {
                if (isCancelled) {
                    System.out.println("Cancel event at position " + position);
                } else {
                    System.out.println("Process event at position " + position);
                }
            }
        } catch (IllegalActionException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Do nothing.
     *
     *  @param initializable Not used.
     */
    @Override
    public void removeInitializable(Initializable initializable) {
    }

    /** Set the container and register this as an initializable in the
     *  container.
     *
     *  @param container The container
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute that is of class
     *   SingletonConfigurableAttribute.
     */
    @Override
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

    /** Remove this attribute from the list of debug listeners in the director.
     *
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        NamedObj container = getContainer();
        if (container instanceof PteraController) {
            PteraDirector director = ((PteraController) container).director;
            director.removeDebugListener(this);
        }
    }

    /** A Boolean parameter that determines whether this debugger is active.
     */
    public Parameter active;

    /** The icon of the event queue debugger attribute. */
    private static final String _ICON_DESCRIPTION = "<svg>"
            + "<rect x=\"0\" y=\"0\" width=\"60\" height=\"10\""
            + "  style=\"fill:#C0C0C0\"/>" + "</svg>";
}
