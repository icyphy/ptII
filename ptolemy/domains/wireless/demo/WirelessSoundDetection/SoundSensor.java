/* A class modeling a general wireless Sensor.

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

package ptolemy.domains.wireless.demo.WirelessSoundDetection;

import java.awt.Polygon;
import java.awt.Shape;

import ptolemy.actor.Director;
import ptolemy.actor.TypeAttribute;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.EllipseAttribute;
import ptolemy.vergil.kernel.attributes.FilledShapeAttribute;

//////////////////////////////////////////////////////////////////////////
//// SoundSensor

/**
This class represents a general primarary wireless sensor node. The sensor node
is directed by a sensor director.  The node is able to broadcast and receive 
messages from other sensor nodes that are in reach.  The sensor node has three
modes, BROADCAST, RECEIVE, and SLEEP. 

In BROADCAST mode the sensor node sends or passes on the data(token)to all 
nodes in reach.  When broadcast is done by the sensor node, the battery power
of that node is reduced.  After 5 broadcast the signal radius is updated based
on how much battery power is left.

In RECEIVE mode the sensor interprets the data(token) at it's wirless IO port.
A confimation message is then sent to the sensor director.

In SLEEP mode the sensor saves battery power, and wait for message to receive.
Must go to RECEIVE mode before it can go to BROADCAST mode.  Also, in SLEEP
mode the battery power is slightly restored if it broadcasted last.

@author Philip Baldwin
@version $Id$
*/
public class SoundSensor extends TypedAtomicActor {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SoundSensor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        // Creat new parameters and ports, then set default values and/or 
        // types of parameters and ports.
       
       	wirelessOPort = new WirelessIOPort(this, "wirelessOPort", false, true);
        wirelessOPort.outsideChannel.setExpression("RadioChannel");
        wirelessOPort.outsideTransmitProperties.setExpression("signalRadius");
        TypeAttribute portType = new TypeAttribute(wirelessOPort, "type");
        portType.setExpression("{location=[double], time=double}");
        wirelessIPort = new WirelessIOPort(this, "wirelessIPort", true, false);
        wirelessIPort.outsideChannel.setExpression("RadioChannel");
        //wirelessIPort.setTypeEquals(BaseType.GENERAL);
        
        soundIPort = new WirelessIOPort(this, "soundIPort", true, false);
        soundIPort.outsideChannel.setExpression("SoundChannel");
        soundIPort.setTypeEquals(BaseType.DOUBLE);
        soundOPort = new WirelessIOPort(this, "soundOPort", false, true);
        soundOPort.outsideChannel.setExpression("SoundChannel");
        soundOPort.outsideTransmitProperties.setExpression("soundRange");
        soundOPort.setTypeEquals(BaseType.DOUBLE);
        
        // Hide the ports in Vergil.
        _hidePort = new Parameter(wirelessOPort, "_hide");
        _hidePort = new Parameter(wirelessIPort, "_hide");
        _hidePort = new Parameter(soundOPort, "_hide");
        _hidePort = new Parameter(soundIPort, "_hide");
        
        signalRadius = new Parameter(this, "signalRadius");
        signalRadius.setToken("100.0");
        signalRadius.setTypeEquals(BaseType.DOUBLE);
        
        soundRange = new Parameter(this, "soundRange");
        soundRange.setToken("200.0");
        soundRange.setTypeEquals(BaseType.DOUBLE);
        
        // Create an icon for this sensor node.
        EditorIcon node_icon = new EditorIcon(this, "_icon");
        
        // The icon has two parts: a circle and an antenna.
        // Create a circle that indicates the signal radius.
        _circle = new EllipseAttribute(node_icon, "_circle");
        _circle.centered.setToken("true");
        _circle.width.setToken("signalRadius*2");
        _circle.height.setToken("signalRadius*2");
        _circle.fillColor.setToken("{0.0, 0.0, 1.0, 0.08}");
        _circle.lineColor.setToken("{0.0, 0.5, 0.5, 1.0}");

        // Create the green antenna shape.
        FilledShapeAttribute  antenna
                = new FilledShapeAttribute(node_icon, "antenna2") {
            protected Shape _newShape() {
                int[] xpoints = {0, -5, 5, 0, 0};
                int[] ypoints = {-5, -15, -15, -5, 15};
                return new Polygon(xpoints, ypoints, 5);
            }
        };
        // Set the color to green.
        antenna.fillColor.setToken("{0.0, 1.0, 0.0, 1.0}");
        antenna.width.setToken("10");
        
        // Hide the name of this sensor node. 
        new Attribute(this, "_hideName");
    }
    
	///////////////////////////////////////////////////////////////////
	////                     ports and parameters                  ////
	
	/** The radius of the radio signal range. This is a double that
     *  defaults to 100.0.  The icon for the sensor node includes
     *  a circle with the radius.
	 */
	public Parameter signalRadius;
    
    public Parameter soundRange;
	
	/** Port that receives a token from the sensor director signifying a
	 * message was received. 
	 */
	public WirelessIOPort wirelessOPort; 
    public WirelessIOPort wirelessIPort;   
    public WirelessIOPort soundIPort;
    public WirelessIOPort soundOPort;
    
    /** Parameter to hide the port associated with the actor
     */
	private Parameter _hidePort;

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////
 
  	/** If the wireless input port has a token, change the icon's
     *  circle color to yellow, and read and discard the input token.
     *  If the sound input port has a token, change the icon's
     *  color to red.  If both have a token, change the icon's color
     *  to orange.  Whether or not a sound is received, generate
     *  a wireless signal that indicates the position and time
     *  when the last sound was heard.
  	 */
    public void fire() throws IllegalActionException {

        super.fire();
        
        Location myLocation = (Location)getAttribute("_location");
        
        boolean haveWireless = false;
        if (wirelessIPort.hasToken(0)) {
            haveWireless = true;
            // Change the color of the icon to yellow.
            _circle.fillColor.setToken("{1.0, 1.0, 0.0, 0.6}");
            // Read and discard the input token.
            Token token = wirelessIPort.get(0);
            if (_debugging) {
                _debug("Wireless signal received: " + token.toString());
            }
        }

        if (soundIPort.hasToken(0)) {
            if (haveWireless) {
                // Change the color of the icon to orange.
                _circle.fillColor.setToken("{1.0, 0.5, 0.1, 0.7}");                
            } else {
                // Change the color of the icon to red.
                _circle.fillColor.setToken("{1.0, 0.0, 0.1, 0.7}");
            }
            Token sound = soundIPort.get(0);
            if (_debugging) {
                _debug("Sound signal received: " + sound.toString());
            }
                
            // Construct the message about the sound signal detected.
            String[] labels = {"location", "time"};
            double[][] locationMatrix = new double[1][0];
            locationMatrix[0] = myLocation.getLocation();
            double time = getDirector().getCurrentTime();
            Token[] values = {
                new DoubleMatrixToken(locationMatrix),
                new DoubleToken(time)
            };
            _mostRecentSound = new RecordToken(labels, values);
        }
        
        // FIXME: Transmit regardless...
        if (getDirector().getCurrentTime() == _lastTimeFired) {

// FIXME: Go to green for one time unit, then go back to blue...
            if (_mostRecentSound != null) {
                // Change the color of the icon to blue.
                _circle.fillColor.setToken("{0.0, 0.6, 0.8, 0.3}");
                wirelessOPort.send(0, _mostRecentSound);
            }
                
            _lastTimeFired += 1.0;
            getDirector().fireAt(this, _lastTimeFired);
        }    
    }

    /** Initialize the sensor node. 
     *
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        Director director = getDirector();
        director.fireAt(this, director.getCurrentTime());
      
        // Set initial icon color to blue.
        _circle.fillColor.setToken("{0.0, 0.0, 1.0, 0.05}");
    }
  
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
  
    public static final int INVALID_ID = -1;
   
    // Identifier of this node in network
    public int ID;
    
    // variable indicating if a message was recieved
    public boolean receive = false;

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    /** Icon indicating the communication region. */
    private EllipseAttribute _circle;
    
    // FIXME: Are these needed?
    private double _lastTimeFired = 0.0;
    private Token _mostRecentSound;
}
