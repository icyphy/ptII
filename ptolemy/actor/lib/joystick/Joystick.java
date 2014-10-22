/* Reads data from a Joystick

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.joystick;

import java.io.IOException;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.centralnexus.input.JoystickListener;

///////////////////////////////////////////////////////////////////
////

/**
 This actor reads data from a Joystick using the Joystick interface
 from
 <a href="http://sourceforge.net/projects/javajoystick/"
 target="_top"><code>http://sourceforge.net/projects/javajoystick/</code></a>
 and generates output ranging between -1.0 and 1.0 on the <i>x</i>
 and <i>x</i> ports.

 <p>Currently, this actor will only work under Windows, though
 the Joystick interface also supports Linux.

 <p>Under Windows, <code>jjstick.dll</code> must be in your path
 and <code>joystick.jar</code> must be in the classpath.

 <p>By default, $PTII/configure looks for the Joystick
 interface in <code>$PTII/vendors/misc/joystick/lib</code>, so you could either
 add that directory to your path, or copy <code>jjstick.dll</code>
 to <code>$PTII/bin</code>:
 <pre>
 cp $PTII/vendors/misc/joystick/lib/jjstick.dll $PTII/bin
 </pre>
 By default, if configure finds <code>joystick.jar</code>, then
 vergil will include <code>joystick.jar</code> in the classpath.

 @author Christopher Hylands, David Lee, Paul Yang
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating red (cxh)
 @Pt.AcceptedRating red (cxh)
 @see ptolemy.actor.lib.io.comm.SerialComm
 */
public class Joystick extends TypedAtomicActor implements JoystickListener {
    // The com.centralnexus.input.Joystick says that there are two
    // ways to update the axis and button values: 1) use JoystickListener
    // or 2) call poll() from a separate thread.

    /** Construct a Joystick actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Joystick(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        deadZone = new Parameter(this, "deadZone", new DoubleToken("0.01"));

        isPolling = new Parameter(this, "isPolling", new BooleanToken("true"));

        pollingInterval = new Parameter(this, "pollingInterval", new IntToken(
                "50"));

        x = new TypedIOPort(this, "x", false, true);
        x.setTypeEquals(BaseType.DOUBLE);

        y = new TypedIOPort(this, "y", false, true);
        y.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The deadzone of the Joystick: Under this absolute value, the
     *  joystick coordinate is 0.0.  The default value is a
     *  DoubleToken of value 0.01
     */
    public Parameter deadZone;

    /** Set to true if polling is used to access the Joystick, false if
     *  we use a JoystickListener.  The initial value is a BooleanToken
     *  with value true.
     */
    public Parameter isPolling;

    /** The polling interval in milliseconds of how often the
     *  JoystickListeners get notified of joystick events.  The default value
     *  is an IntToken with a value of 50.
     */
    public Parameter pollingInterval;

    /** The output port for the x coordinate, which has type DoubleToken. */
    public TypedIOPort x;

    /** The output port for the y coordinate, which has type DoubleToken. */
    public TypedIOPort y;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is deadZone and the joystick has already been
     *  initialized by calling initialize() then update the
     *  appropriate value in the joystick interface.  If the attribute
     *  is deadZone and initialized() has not yet been called, then do
     *  nothing.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (_debugging) {
            _debug("Joystick.attributeChanged(): " + attribute);
        }

        // FIXME: not sure about this, but it seems like a good idea.
        if (attribute == deadZone && _joy != null) {
            double deadZoneValue = ((DoubleToken) deadZone.getToken())
                    .doubleValue();
            _joy.setDeadZone(deadZoneValue);
        } else if (attribute == isPolling) {
            boolean oldIsPollingValue = _isPollingValue;

            _isPollingValue = ((BooleanToken) isPolling.getToken())
                    .booleanValue();

            // If necessary, add or remove this as a JoystickListener.
            if (_joy != null && _isPollingValue != oldIsPollingValue) {
                if (!_isPollingValue) {
                    _joy.addJoystickListener(this);
                } else {
                    _joy.removeJoystickListener(this);
                }
            }
        } else if (attribute == pollingInterval && _joy != null) {
            int pollingIntervalValue = ((DoubleToken) pollingInterval
                    .getToken()).intValue();
            _joy.setPollInterval(pollingIntervalValue);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Get the current location values from the joystick
     *  and generate a DoubleMatrixToken on the output.
     */
    @Override
    public synchronized void fire() throws IllegalActionException {
        super.fire();

        if (_isPollingValue) {
            _joy.poll();
        }

        x.send(0, new DoubleToken(_joy.getX()));
        y.send(0, new DoubleToken(_joy.getY()));
    }

    /** Get the values of the parameters and initialize the joystick.
     *
     *  @exception IllegalActionException If the joystick cannot
     *  be initialized or if the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        if (_debugging) {
            _debug("Joystick.initialize() start");
        }

        double deadZoneValue = ((DoubleToken) deadZone.getToken())
                .doubleValue();
        int pollingIntervalValue = ((IntToken) pollingInterval.getToken())
                .intValue();

        try {
            _joy = com.centralnexus.input.Joystick.createInstance();

            if (_debugging) {
                _debug("JoystickID: " + _joy.getID());
            }
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to create a joystick instance");
        }

        _joy.setDeadZone(deadZoneValue);
        _joy.setPollInterval(pollingIntervalValue);

        if (!_isPollingValue) {
            _joy.addJoystickListener(this);
        }

        if (_debugging) {
            _debug("Joystick.initialize() end");
        }
    }

    /* This method gets called periodically when the joystick changes
     * its value.
     */
    @Override
    public void joystickAxisChanged(com.centralnexus.input.Joystick j) {
    }

    /** This method gets called periodically when a joystick button
     *  changes its value.
     */
    @Override
    public void joystickButtonChanged(com.centralnexus.input.Joystick j) {
    }

    /** Wrap up deallocates resources, specifically the serial port.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        if (_joy != null && !_isPollingValue) {
            _joy.removeJoystickListener(this);
        }

        _joy = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Set from the isPolling parameter.  True if we call poll(), false
    // if we add this as a listener.
    private boolean _isPollingValue;

    // The joystick
    private com.centralnexus.input.Joystick _joy;
}
