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

import ptolemy.actor.lib.Source;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ObjectToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.data.DoubleMatrixToken;
import java.util.Iterator;
import java.util.Vector;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import com.centralnexus.input.*;

//////////////////////////////////////////////////////////////////////////
////
/**
This actor reads data from a Joystick using the Joystick interface
from 
<a href="http://sourceforge.net/projects/javajoystick/" 
target="_top"><code>http://sourceforge.net/projects/javajoystick/</code></a>

<p>Currently, this actor will only work under Windows, though 
the Joystick interface also supports Linux.

<p>If you get the following error on the console:
<pre>
TBA
</pre>
then you need to be sure that the 
<code>jjstick.dll</code> is in your path.  
<p>By default, $PTII/configure looks for the Joystick
interface in <code>$PTII/vendors/misc/joystick/lib</code>, so you could either
add that directory to your path, or copy <code>jjstick.dll</code>
to <code>$PTII/bin</code>

<p> When the fire() method is called, a DoubleMatrixToken is
produced on the output.  The X value is in [0][0] and the
Y value is in [1][0] (FIXME: what is the range?)
(FIXME: it looks like there is some confusion about polling
vs. callbacks?)

@author Christopher Hylands, David Lee, Paul Yang
@version $Id$
@since Ptolemy II 3.0
@see ptolemy.actor.lib.io.comm.SerialComm
 */
public class Joystick extends Source implements 
com.centralnexus.input.JoystickListener {

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
        // FIXME: Why Double Matrix?  Why not an array or two separate ports?
        // FIXME: What is the range of the output?
	output.setTypeEquals(BaseType.DOUBLE_MATRIX);
	output.setMultiport(false);
	pollingInterval
            = new Parameter(this, "pollingInterval", new IntToken("50"));
	deadZone = new Parameter(this, "deadZone", new DoubleToken("0.01"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The polling interval of the Joystick in milliseconds.  The
     *  default value is an IntToken with a value of 50
     */  
    public Parameter pollingInterval;

    /** The deadzone of the Joystick: Under this absolute value, the
     *  joystick coordinate is 0.0.  The default value is a 
     *  DoubleToken of value 0.01
     */
    public Parameter deadZone;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is pollingInterval or deadZone and the
     *  joystick has already been initialized by calling initialize()
     *  then update the appropriate value in the joystick interface.
     *  If initialized() has not yet been called, then do nothing. 
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Maybe thrown (?)
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // FIXME: not sure about this, but it seems like a good idea.
        if (attribute == pollingInterval && _joy != null) {
            int pollingIntervalValue
                = ((IntToken)pollingInterval.getToken()).intValue();
            _joy.setPollInterval(pollingIntervalValue);
        } else if (attribute == deadZone && _joy != null) {
            double deadZoneValue
                = ((DoubleToken)deadZone.getToken()).doubleValue();
            _joy.setDeadZone(deadZoneValue);
        } else {
            super.attributeChanged(attribute);
        }
    }

    
    /** Close this JoyDriver by removing it from the JoystickListeners. */
    public void close() {
	_joy.removeJoystickListener(this);
    }

    /** Get the current location values from the joystick
     *  and generate a DoubleMatrixToken on the output. 
     */
    public synchronized void fire() throws IllegalActionException {
	super.fire();
	double currentX = _joy.getX();
	double currentY = _joy.getY();
	double[][] data = new double[2][1];
	data[0][0] = currentX;
	data[1][0] = -currentY;
	DoubleMatrixToken result = new DoubleMatrixToken(data);
	output.send(0, result);
    }

    /** Get the values of the parameters and initialize the joystick
     *
     *  @exception IllegalActionException If the joystick cannot
     *  be initialized or if the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        int pollingIntervalValue
                = ((IntToken)pollingInterval.getToken()).intValue();
        double deadZoneValue
                = ((DoubleToken)deadZone.getToken()).doubleValue();

	try {
	    _joy = com.centralnexus.input.Joystick.createInstance();
            if (_debugging){
                _debug("JoystickID: " + _joy.getID());
            }
	}
	catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to create a joystick instance");
	}
        _joy.setDeadZone(deadZoneValue);
	_joy.setPollInterval(pollingIntervalValue);
	_joy.addJoystickListener(this);
    }


    /** Indicates the joystick's input when the joystick is polled. */
    public void joystickChanged(com.centralnexus.input.Joystick j) {
        // FIXME: Why not use this method?
	//	current_X = _joy.getX();
	//	current_Y = _joy.getY();
    }

    /** Called when the joystick axis has changed.
     *  Indicates the joystick's input when the joystick is polled.
     */
    public void joystickAxisChanged(com.centralnexus.input.Joystick j) {
        // FIXME: Why not use this method?
	//	current_X = _joy.getX();
	//	current_Y = _joy.getY();
    }

    /** Called when the joystick button is changed.
    public void joystickButtonChanged(com.centralnexus.input.Joystick j) {
        // FIXME: Why not use these

    }

    /** Sets the polling interval.
     *  @param pollMillis The number of milliseconds between polls.
     */
    public void setPollInterval(int pollMillis) {
        // FIXME: Does not do anything? we are polling every time
        // we call fire.
        _joy.setPollInterval(pollMillis);
    }

    /** Close this JoyDriver by removing it from the JoystickListeners.
     *  @exception IllegalActionException Not thrown in this baseclass.
     */
    public void wrapup() throws IllegalActionException {
	close();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The joystick
    private com.centralnexus.input.Joystick _joy;
}

