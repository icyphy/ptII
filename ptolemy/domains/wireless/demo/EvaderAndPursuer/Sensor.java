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

package ptolemy.domains.wireless.demo.EnvaderAndPursuer;

import ptolemy.actor.TypeAttribute;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.EllipseAttribute;

//////////////////////////////////////////////////////////////////////////
//// Sensor

/**
This is a wireless sensor node that senses evaders in the sensor
feild. The sensors in the field communicate with each other to 
construct a spanning tree from the root node (where the evader is close
to) to nodes that are one hop from the root node and so on. When a 
sensor detects the evader, it sets itself as the root
and broadcast a message to its neighbor nodes. The message includes
the time when the evader is detected, the location and the depth, 
zero for the root node, of the sensor. If a sensor receives
a message from another sensor, it checks whether the root node
has been changed from last time (by check the detected time), 
or whether there is a shorter path to the root node. If so, it 
records the detected time, updates the information of its parent 
node (location and depth in the tree) and broadcast a message,
include the detected time, its location and depth in the tree, to
it neighbot nodes. By doing this in a sensor network, a spaning 
tree is constructed distributedly and sensors are indexed according
to how far it is from the root node. With the evader moving, it may
be detected by another sensor, and the tree changes dynamically.


@author Yang Zhao
@version $ $
*/
public class Sensor extends TypedAtomicActor {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Sensor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        // Create and configure the parameters.
        messageChannelName = new StringParameter(this, "messageChannelName");
        messageChannelName.setExpression("MessageChannel");

        signalChannelName = new StringParameter(this, "signalChannelName");
        signalChannelName.setExpression("SignalChannel");
        
        outputChannelName = new StringParameter(this, "outputChannelName");
        outputChannelName.setExpression("OutputChannel");
        
        range = new Parameter(this, "range");
        range.setToken("50.0");
        range.setTypeEquals(BaseType.DOUBLE);
        
        // Create and configure the ports.       
        input = new WirelessIOPort(this, "input", true, false);
        input.outsideChannel.setExpression("$messageChannelName");
        
        signal = new WirelessIOPort(this, "signal", true, false);
        signal.outsideChannel.setExpression("$signalChannelName");
        signal.setTypeEquals(BaseType.STRING);
        
        output = new WirelessIOPort(this, "output", false, true);
        output.outsideChannel.setExpression("$outputChannelName");
        // Since this actor sources the data at this port, we have to
        // declare the type.
        TypeAttribute portType = new TypeAttribute(output, "type");
        portType.setExpression("{location={double}, time=double, depth=int}");
        
        // Create an icon for this sensor node.
        EditorIcon node_icon = new EditorIcon(this, "_icon");
        
        // The icon has two parts: a circle and an antenna.
        // Create a circle that indicates the signal radius.
        _circle = new EllipseAttribute(node_icon, "_circle");
        _circle.centered.setToken("true");
        _circle.width.setToken("range*2");
        _circle.height.setToken("range*2");
        _circle.fillColor.setToken("{0.0, 0.0, 1.0, 0.05}");
        _circle.lineColor.setToken("{0.0, 0.0, 1.0, 0.05}");

        _circle2 = new EllipseAttribute(node_icon, "_circle2");
        _circle2.centered.setToken("true");
        _circle2.width.setToken("20");
        _circle2.height.setToken("20");
        _circle2.fillColor.setToken("{1.0, 1.0, 1.0, 1.0}");
        _circle2.lineColor.setToken("{0.0, 0.5, 0.5, 1.0}");
        
        node_icon.setPersistent(false);
        
        // Hide the name of this sensor node. 
        new Attribute(this, "_hideName");
        // Hide the ports.
        new Attribute(output, "_hide");
        new Attribute(input, "_hide");
        new Attribute(signal, "_hide"); 
    }
    
    /** Explicitly declare which inputs and outputs are not dependent.
     *  
     */
    public void removeDependencies() {
        super.removeDependency(input, output);
        super.removeDependency(signal, output);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** Port that receives update message for the spanning tree.
     */
    public WirelessIOPort input;
    
    /** Port that receives a signal from the envader or pursuer.
     */
    public WirelessIOPort signal;    

    /** Name of the channel for messaging between sensors.
     *  This is a string that defaults to "messageChannel".
     */
    public StringParameter messageChannelName;
    
    /** Name of the channel for sensors to detect envader or pursuer.
     *  This is a string that defaults to "signalChannel".
     */
    public StringParameter signalChannelName;    

    /** Port that transmits the update message for the spanning tree.
     *  This has type {location={double}, time=double, depth =int},
     *  a record token.
     */
    public WirelessIOPort output;

    /** Name of the output channel. This is a string that defaults to
     *  "OutputChannel".
     */
    public StringParameter outputChannelName;
    
    /** The transmition range of the sensor. The icon for this sensor
     *  node includes a circle with this as its radius. This is a 
     *  double and default to 50.0.
      */
     public Parameter range;
       

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
 
    /** When it receives token from the signal port, which is 
     *  used to receive signal from the pursuer or the evader. 
     *  it tells what the signal is from by checking the signal header.
     *  If it is from the evader, it set itself to be the root node
     *  and broadcast a message for updating the tree. Otherwise, it
     *  output a message to the pursuer to tell it the location of 
     *  its parent node, and the pursuer will move closer to the evader
     *  using this information.  
     *  When it receives token from the input port, which is used to 
     *  receive message from other sensors, it check whether the rootnode
     *  has been changed or whether there is a shorter path. If so, it
     *  performs update and broadcast a message. Otherwise, simply
     *  consumes the messge. 
     */
    public void fire() throws IllegalActionException {

        super.fire();
        
        if(signal.hasToken(0)) {
            String signalValue = ((StringToken) signal.get(0)).stringValue();
            if (_debugging) {
                _debug("signal token received: " + signalValue);
            }
            //FIXME: Assumes the pursure uses "SPIDER" in its signal header.
            if (!signalValue.equals("SPIDER")){
                //detect the envader, set this to be the root node in the
                //spanning tree.
                String[] labels = {"location", "time", "depth"};
                // Get the location and wrap each coordinate in a token.
                double[] location = _getLocation();
                Token[] locationArray = new Token[location.length];
                for (int i = 0; i < location.length; i++) {
                    locationArray[i] = new DoubleToken(location[i]);
                }
            
                double time = getDirector().getCurrentTime();
                Token[] values = {
                    new ArrayToken(locationArray),
                    new DoubleToken(time),
                    new IntToken(0)
                };
                Token result = new RecordToken(labels, values);

                output.send(0, result);                
            } else {
                // It is the pursuer. Send its parent info to the pursuer.
                if(_timeValue > 0.0) {
                    String[] labels = {"location", "time", "depth"};
            
                    Token[] values = {
                        new ArrayToken(_parentLocation),
                        new DoubleToken(_timeValue),
                        new IntToken(_parentDepth)
                    };
                    Token result = new RecordToken(labels, values);

                    output.send(0, result);
                }                
            }
        }
        
        if (input.hasToken(0)) {
            //receive message for updating the spanning tree.
            RecordToken inputToken = (RecordToken) input.get(0);
            if (_debugging) {
                _debug("message token received: ");
            }
            DoubleToken time =(DoubleToken) inputToken.get("time");
            IntToken d = (IntToken) inputToken.get("depth");
            if (time.doubleValue() > _timeValue ||
                (time.doubleValue() == _timeValue 
                 && d.intValue() < _parentDepth)) {
                //the root node may have been changed
                //or there is a shorter path.
                ArrayToken locationArray = 
                        (ArrayToken)inputToken.get("location");
                int length = locationArray.length();
                _parentLocation = new DoubleToken[length];
                for (int i = 0; i < length ; i++) {
                    _parentLocation[i] = 
                            (DoubleToken) locationArray.getElement(i);
                }
                _timeValue = time.doubleValue();
                _parentDepth = d.intValue();
                
                String[] labels = {"location", "time", "depth"};
            
                Token[] values = {
                    new ArrayToken(_parentLocation),
                    new DoubleToken(_timeValue),
                    new IntToken(_parentDepth)
                };
                Token result = new RecordToken(labels, values);

                output.send(0, result); 
            }
        }
    }
    
    /** Initialize the private varialbles of the sensor node.
     *  @exception IllegalActionException If thrown by the base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        double[] location = _getLocation();
        DoubleToken[] _parentLocation = new DoubleToken[location.length];
        for (int i = 0; i < location.length; i++) {
            _parentLocation[i] = new DoubleToken(location[i]);
        }
        _parentDepth = 0;
        _timeValue = 0.0;
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
        //Entity container = (Entity)this.getContainer();
        Location locationAttribute = (Location)getAttribute(
                "_location", Location.class);
        if (locationAttribute == null) {
            throw new IllegalActionException(this,
            "Cannot find a _location attribute of class Location.");
        }
        return locationAttribute.getLocation();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    /** Its parent's location. */
    private DoubleToken[] _parentLocation;
    
    /** The time when the root node detected the envader. */
    private double _timeValue;
    
    /** The depth of its parent.
     */   
    private int _parentDepth;
    
    /** Icon indicating the communication region. */
    private EllipseAttribute _circle;
    
    /** Icon of this actor. */
    private EllipseAttribute _circle2;
}
