/* A class modeling a sensor that transmits location information.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (pjb2e@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.lib;

import ptolemy.actor.TypeAttribute;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Locator

/**
This is a wireless sensor node that reacts to an input event by
transmitting an output with the current location of this node and
the time of the input.  The output is a record token with type
{location={double}, time=double}.  The location is an array with
two doubles representing the X and Y positions of the sensor.
The location of the sensor is determined by the _getLocation()
protected method, which in this base class returns the location
of the icon in the visual editor, which is determined from the
_location attribute of the actor.  If there is no _location
attribute, then an exception is thrown.  Derived classes may
override this protected method to specify the location in some
other way (or in more dimensions).

@author Philip Baldwin, Xiaojun Liu and Edward A. Lee
@version $Id$
*/
public class Locator extends TypedAtomicActor {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Locator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        // Create and configure the parameters.
        inputChannelName = new StringParameter(this, "inputChannelName");
        inputChannelName.setExpression("InputChannel");

        outputChannelName = new StringParameter(this, "outputChannelName");
        outputChannelName.setExpression("OutputChannel");
        
        // Create and configure the ports.       
        input = new WirelessIOPort(this, "input", true, false);
        input.outsideChannel.setExpression("$inputChannelName");

        output = new WirelessIOPort(this, "output", false, true);
        output.outsideChannel.setExpression("$outputChannelName");
        // Since this actor sources the data at this port, we have to
        // declare the type.
        TypeAttribute portType = new TypeAttribute(output, "type");
        portType.setExpression("{location={double}, time=double}");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** Port that receives a trigger input that causes transmission
     *  of location and time information on the <i>output</i> port.
     */
    public WirelessIOPort input;

    /** Name of the input channel. This is a string that defaults to
     *  "InputChannel".
     */
    public StringParameter inputChannelName;

    /** Port that transmits the current location and the time
     *  of the event on the <i>input</i> port.  This has
     *  type {location={double}, time=double}, a record token.
     */
    public WirelessIOPort output;

    /** Name of the output channel. This is a string that defaults to
     *  "OutputChannel".
     */
    public StringParameter outputChannelName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
 
    /** Generate an event on the <i>output</i> port that indicates the
     *  current position and time of the last input on the <i>input</i>
     *  port.  The value of the input is ignored.
     */
    public void fire() throws IllegalActionException {

        super.fire();
      
        if (input.hasToken(0)) {
            Token inputValue = input.get(0);
            if (_debugging) {
                _debug("Input signal received: " + inputValue.toString());
            }

            // Construct the message about the input signal detected.
            String[] labels = {"location", "time"};
            
            // Get the location and wrap each coordinate in a token.
            double[] location = _getLocation();
            Token[] locationArray = new Token[location.length];
            for (int i = 0; i < location.length; i++) {
                locationArray[i] = new DoubleToken(location[i]);
            }
            
            double time = getDirector().getCurrentTime();
            Token[] values = {
                new ArrayToken(locationArray),
                new DoubleToken(time)
            };
            Token result = new RecordToken(labels, values);

            output.send(0, result);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the location of this sensor. In this base class,
     *  this is determined by looking for an attribute with name
     *  "_location" and class Location.  Normally, a visual editor
     *  such as Vergil will create this icon, so the location will
     *  be determined by the visual editor.  Derived classes can
     *  override this method to specify the location in some other way.
     *  @return An array identifying the location.
     *  @exception IllegalActionException If the location attribute does
     *   not exist or cannot be evaluated.
     */
    protected double[] _getLocation() throws IllegalActionException {
        Token[] result = new Token[2];
        Location locationAttribute = (Location)getAttribute(
                "_location", Location.class);
        if (locationAttribute == null) {
            throw new IllegalActionException(this,
            "Cannot find a _location attribute of class Location.");
        }
        return locationAttribute.getLocation();
    }
}
