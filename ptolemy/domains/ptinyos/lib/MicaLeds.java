/* An actor to display the LEDs of the simulated mote.

 Copyright (c) 2005 The Regents of the University of California
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.ptinyos.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;

//////////////////////////////////////////////////////////////////////////
//// MicaLeds

/** An actor to display the LEDs of the simulated mote.  There are
 three LEDS -- red, yellow, and green.

 The actor has a boolean port for each LED.  A value of true will
 cause the corresponding LED on the icon to light up.

 The actor also has a parameter for each LED, which allows the
 container to use the value.
    
 @author Elaine Cheong
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (celaine)
 @Pt.AcceptedRating Red (celaine)
 */
public class MicaLeds extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MicaLeds(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Create the node icon.
        EditorIcon node_icon = new EditorIcon(this, "_icon");

        // The icon of this actor has 3 LEDs: red, yellow, and green.
        // For each LED, create the corresponding parameter and
        // graphical icon.  Then initialize the LED to the off setting.
        red = new Parameter(this, "red");
        _ledRed = new RectangleAttribute(node_icon, "_ledRed");
        Location ledRedLoc = new Location(_ledRed, "_location");
        double[] ledRedLocVal = { -20.0, 0.0 };
        ledRedLoc.setLocation(ledRedLocVal);
        _ledRed.width.setToken("20.0");
        _ledRed.height.setToken("39.0");
        _ledRed.centered.setToken("true");
        _redOff();

        green = new Parameter(this, "green");
        _ledGreen = new RectangleAttribute(node_icon, "_ledGreen");
        Location ledGreenLoc = new Location(_ledGreen, "_location");
        double[] ledGreenLocVal = { 0.0, 0.0 };
        ledGreenLoc.setLocation(ledGreenLocVal);
        _ledGreen.width.setToken("20.0");
        _ledGreen.height.setToken("39.0");
        _ledGreen.centered.setToken("true");
        _greenOff();

        yellow = new Parameter(this, "yellow");
        _ledYellow = new RectangleAttribute(node_icon, "_ledYellow");
        Location ledYellowLoc = new Location(_ledYellow, "_location");
        double[] ledYellowLocVal = { 20.0, 0.0 };
        ledYellowLoc.setLocation(ledYellowLocVal);
        _ledYellow.width.setToken("20.0");
        _ledYellow.height.setToken("39.0");
        _ledYellow.centered.setToken("true");
        _yellowOff();

        // Create the input ports and set the token type to boolean.
        ledRed = new TypedIOPort(this, "ledRed", true, false);
        ledRed.setTypeEquals(BaseType.BOOLEAN);
        ledGreen = new TypedIOPort(this, "ledGreen", true, false);
        ledGreen.setTypeEquals(BaseType.BOOLEAN);
        ledYellow = new TypedIOPort(this, "ledYellow", true, false);
        ledYellow.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port for the red LED.  The token type is boolean.
     */
    public TypedIOPort ledRed;

    /** Input port for the green LED.  The token type is boolean.
     */
    public TypedIOPort ledGreen;

    /** Input port for the yellow LED.  The token type is boolean.
     */    
    public TypedIOPort ledYellow;

    /** The color of the "red" LED.  The initial value is set to off
     *  ({0.5,0.0,0.0,1.0}).
     */
    public Parameter red;

    /** The color of the "green" LED.  The initial value is set to off
     *  ({0.0,0.5,0.0,1.0}).
     */
    public Parameter green;

    /** The color of the "yellow" LED.  The initial value is set to off
     *  ({0.5,0.5,0.0,1.0}).
     */
    public Parameter yellow;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** For each LED port, if it is connected and has a token, read
     *  the token.  If the token is true, turn the LED on, otherwise
     *  turn the LED off.
     *  @exception IllegalActionException If any of the LED ports
     *  cannot be read.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }

        if ((ledRed.getWidth() > 0) && ledRed.hasToken(0)) {
            BooleanToken red = (BooleanToken) ledRed.get(0);

            if (red.booleanValue()) {
                _redOn();
            } else {
                _redOff();
            }
        }

        if ((ledGreen.getWidth() > 0) && ledGreen.hasToken(0)) {
            BooleanToken green = (BooleanToken) ledGreen.get(0);

            if (green.booleanValue()) {
                _greenOn();
            } else {
                _greenOff();
            }
        }

        if ((ledYellow.getWidth() > 0) && ledYellow.hasToken(0)) {
            BooleanToken yellow = (BooleanToken) ledYellow.get(0);

            if (yellow.booleanValue()) {
                _yellowOn();
            } else {
                _yellowOff();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Turn the red LED on by changing the color of the graphical
     *  icon and reflecting the color value in the corresponding
     *  parameter.
     */
    private void _redOn() throws IllegalActionException {
        _ledRed.fillColor.setToken("{1.0,0.0,0.0,1.0}");
        red.setToken("{1.0,0.0,0.0,1.0}");
    }

    /** Turn the red LED off by changing the color of the graphical
     *  icon and reflecting the color value in the corresponding
     *  parameter.
     */
    private void _redOff() throws IllegalActionException {
        _ledRed.fillColor.setToken("{0.5,0.0,0.0,1.0}");
        red.setToken("{0.5,0.0,0.0,1.0}");
    }

    /** Turn the green LED on by changing the color of the graphical
     *  icon and reflecting the color value in the corresponding
     *  parameter.
     */    
    private void _greenOn() throws IllegalActionException {
        _ledGreen.fillColor.setToken("{0.0,1.0,0.0,1.0}");
        green.setToken("{0.0,1.0,0.0,1.0}");
    }

    /** Turn the green LED off by changing the color of the graphical
     *  icon and reflecting the color value in the corresponding
     *  parameter.
     */    
    private void _greenOff() throws IllegalActionException {
        _ledGreen.fillColor.setToken("{0.0,0.5,0.0,1.0}");
        green.setToken("{0.0,0.5,0.0,1.0}");
    }

    /** Turn the yellow LED on by changing the color of the graphical
     *  icon and reflecting the color value in the corresponding
     *  parameter.
     */    
    private void _yellowOn() throws IllegalActionException {
        _ledYellow.fillColor.setToken("{1.0,1.0,0.0,1.0}");
        yellow.setToken("{1.0,1.0,0.0,1.0}");
    }

    /** Turn the yellow LED off by changing the color of the graphical
     *  icon and reflecting the color value in the corresponding
     *  parameter.
     */  
    private void _yellowOff() throws IllegalActionException {
        _ledYellow.fillColor.setToken("{0.5,0.5,0.0,1.0}");
        yellow.setToken("{0.5,0.5,0.0,1.0}");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Graphical icon for red LED */
    private RectangleAttribute _ledRed;

    /** Graphical icon for green LED */
    private RectangleAttribute _ledGreen;

    /** Graphical icon for yellow LED */
    private RectangleAttribute _ledYellow;
}
