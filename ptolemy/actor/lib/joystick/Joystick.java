/* Reads data from a Joystick

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating red (cxh@eecs.berkeley.edu)
@AcceptedRating red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.joystick;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

import java.io.IOException;

//import com.centralnexus.input.*;

//////////////////////////////////////////////////////////////////////////
////
/**
This actor reads data from a Joystick using the Joystick interface
from 
<a href="http://sourceforge.net/projects/javajoystick/" 
target="_top"><code>http://sourceforge.net/projects/javajoystick/</code></a>

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
vergil will include <code>joystick.jar<code> in the classpath.

@author Christopher Hylands, David Lee, Paul Yang
@version $Id$
@since Ptolemy II 3.0
@see ptolemy.actor.lib.io.comm.SerialComm
 */
public class Joystick extends TypedAtomicActor {

    /** Construct a Joystick actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Joystick(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException, 
            IOException {
        super(container, name);

	deadZone = new Parameter(this, "deadZone", new DoubleToken("0.01"));

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
     *  @exception IllegalActionException Maybe thrown (?)
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
	if (_debugging){
	    _debug("Joystick.attributeChanged(): " + attribute );
	}

        // FIXME: not sure about this, but it seems like a good idea.
        if (attribute == deadZone && _joy != null) {
            double deadZoneValue
                = ((DoubleToken)deadZone.getToken()).doubleValue();
            _joy.setDeadZone(deadZoneValue);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Get the current location values from the joystick
     *  and generate a DoubleMatrixToken on the output. 
     */
    public synchronized void fire() throws IllegalActionException {
	super.fire();
	x.send(0, new DoubleToken (_joy.getX()));
	y.send(0, new DoubleToken (_joy.getY()));
    }

    /** Get the values of the parameters and initialize the joystick
     *
     *  @exception IllegalActionException If the joystick cannot
     *  be initialized or if the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
	if (_debugging){
	    _debug("Joystick.initialize() start");
	}
        double deadZoneValue
                = ((DoubleToken)deadZone.getToken()).doubleValue();

	try {
	    _joy = com.centralnexus.input.Joystick.createInstance();
            if (_debugging){
                _debug("JoystickID: " + _joy.getID());
            }

            // FIXME: Also, we probably need to close the joystick,
            // but I'm not sure how.  Running the model twice would be
            // a good test

	}
	catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to create a joystick instance");
	}
        _joy.setDeadZone(deadZoneValue);
	if (_debugging){
	    _debug("Joystick.initialize() end");
	}

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The joystick
    private com.centralnexus.input.Joystick _joy;
}

