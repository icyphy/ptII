/* A CaseDirector governs the execution of a case actor.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.hoc;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.IOPortEvent;
import ptolemy.actor.Mailbox;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;

///////////////////////////////////////////////////////////////////
//// CaseDirector

/**
 An CaseDirector governs the execution of a Case actor.
 This director simply delegates to the refinement whose name
 matches the value of the current control input.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class CaseDirector extends Director {

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of this director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public CaseDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Schedule a firing of the given actor at the given time.
     *  If there is an executive director, this method delegates to it.
     *  Otherwise, it sets its own notion of current time to that
     *  specified in the argument. The reason for this is to enable
     *  Case to be a top-level actor and to support the design pattern
     *  where a director requests a refiring at the next time it wishes
     *  to be awakened, just prior to returning from fire(). DEDirector,
     *  for example, does that, as does the SDFDirector if the period
     *  parameter is set.
     *  @param actor The actor scheduled to be fired.
     *  @param time The scheduled time.
     *  @param microstep The microstep.
     *  @return The time returned by the executive director, or
     *   or the specified time if there isn't one.
     *  @exception IllegalActionException If by the executive director.
     */
    @Override
    public Time fireAt(Actor actor, Time time, int microstep)
            throws IllegalActionException {
        // Note that the actor parameter is ignored, because it does not
        // matter which actor requests firing.
        Nameable container = getContainer();

        if (container instanceof Actor) {
            Actor modalModel = (Actor) container;
            Director executiveDirector = modalModel.getExecutiveDirector();

            if (executiveDirector != null) {
                return executiveDirector.fireAt(modalModel, time, microstep);
            }
        }
        setModelTime(time);
        return time;
    }

    /** Fire the current refinement.
     *  @exception IllegalActionException If refinement throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Calling fire()");
        }
        Case container = (Case) getContainer();
        if (container._current == null) {
            throw new IllegalActionException(container,
                    "Has no current refinement");
        }
        if (_debugging) {
            _debug(new FiringEvent(this, container._current,
                    FiringEvent.BEFORE_FIRE));
        }
        container._current.fire();
        if (_debugging) {
            _debug("Called fire()");
            _debug(new FiringEvent(this, container._current,
                    FiringEvent.AFTER_FIRE));
        }
    }

    /** Return a receiver that is a one-place buffer. A token put into the
     *  receiver will overwrite any token already in the receiver.
     *  @return A receiver that is a one-place buffer.
     */
    @Override
    public Receiver newReceiver() {
        return new Mailbox() {
            @Override
            public boolean hasRoom() {
                return true;
            }

            @Override
            public void put(Token token) {
                try {
                    if (hasToken() == true) {
                        get();
                    }
                    super.put(token);
                } catch (NoRoomException ex) {
                    throw new InternalErrorException("One-place buffer: "
                            + ex.getMessage());
                } catch (NoTokenException ex) {
                    throw new InternalErrorException("One-place buffer: "
                            + ex.getMessage());
                }
            }
        };
    }

    /** Read the control token input, transfer input tokens,
     *  and invoke prefire() of the selected refinement.
     *
     *  @exception IllegalActionException If there is no director,
     *   or if the director's prefire() method throws it, or if this actor
     *   is not opaque.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Calling prefire()");
        }

        try {
            _workspace.getReadAccess();
            super.prefire();

            Case container = (Case) getContainer();
            // Read from port parameters, including the control port.
            Iterator portParameters = container.attributeList(
                    PortParameter.class).iterator();
            while (portParameters.hasNext()) {
                PortParameter portParameter = (PortParameter) portParameters
                        .next();
                portParameter.update();
            }

            String controlValue;
            Token controlToken = container.control.getToken();
            // If it's a string, use stringValue() otherwise there
            // are quotes around the string.
            if (controlToken instanceof StringToken) {
                controlValue = ((StringToken) controlToken).stringValue();
            } else {
                controlValue = controlToken.toString();
            }

            ComponentEntity refinement = container.getEntity(controlValue);
            if (!(refinement instanceof Refinement)) {
                refinement = container._default;
            }
            container._current = (Refinement) refinement;

            // Transfer input tokens.
            for (Iterator inputPorts = container.inputPortList().iterator(); inputPorts
                    .hasNext() && !_stopRequested;) {
                IOPort port = (IOPort) inputPorts.next();

                if (!(port instanceof ParameterPort)) {
                    Receiver[][] insideReceivers = port.deepGetReceivers();
                    for (int i = 0; i < port.getWidth(); i++) {
                        if (port.hasToken(i)) {
                            Token token = port.get(i);
                            if (insideReceivers != null
                                    && insideReceivers[i] != null) {
                                for (int j = 0; j < insideReceivers[i].length; j++) {
                                    if (insideReceivers[i][j].getContainer()
                                            .getContainer() == refinement) {

                                        if (_debugging) {
                                            _debug(new IOPortEvent(port,
                                                    insideReceivers[i][j]
                                                            .getContainer(),
                                                            true, i, false, token));
                                        }

                                        insideReceivers[i][j].put(token);

                                        if (_debugging) {
                                            _debug(new IOPortEvent(port,
                                                    insideReceivers[i][j]
                                                            .getContainer(),
                                                            false, i, false, token));
                                            _debug(getFullName(),
                                                    "transferring input from "
                                                            + port.getFullName()
                                                            + " to "
                                                            + insideReceivers[i][j]
                                                                    .getContainer()
                                                                    .getFullName());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (_stopRequested) {
                return false;
            }
            if (_debugging) {
                _debug(new FiringEvent(this, container._current,
                        FiringEvent.BEFORE_PREFIRE));
            }
            boolean result = container._current.prefire();
            if (_debugging) {
                _debug(new FiringEvent(this, container._current,
                        FiringEvent.AFTER_PREFIRE));
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Invoke the postfire() method of the current local director.
     *  @return True if the execution can continue into the next iteration.
     *  @exception IllegalActionException If there is no director,
     *   or if the director's postfire() method throws it, or if this
     *   actor is not opaque.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("Calling postfire()");
        }
        Case container = (Case) getContainer();

        if (_debugging) {
            _debug(new FiringEvent(this, container._current,
                    FiringEvent.BEFORE_POSTFIRE));
        }

        boolean result = container._current.postfire();

        if (_debugging) {
            _debug(new FiringEvent(this, container._current,
                    FiringEvent.AFTER_POSTFIRE));
        }

        return result && !_finishRequested;
    }
}
