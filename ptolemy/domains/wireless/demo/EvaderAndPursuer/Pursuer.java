/* A class defines how the Pursuer moves.

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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Pursuer

/**
The pursuer moves from a leaf node of the spanning tree to its parent node
to track the envader. 

see the Sensor class for how the spanning tree is constructed.

FIXME: Currently, the workRange is not really used, but I plan to use it
to define a work area for a pursuer, so that when there are multi pursuers,
each one can only take care of a particular part of the field to catch
the evader more quickly.

@author Yang Zhao
@version $ $
*/
public class Pursuer extends TypedAtomicActor {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Pursuer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        // Create and configure the parameters.        
        speed = new Parameter(this, "speed");
        speed.setTypeEquals(BaseType.DOUBLE);
        
        // Create and configure the ports.       
        input = new TypedIOPort(this, "input", true, false);
        trigger = new TypedIOPort(this, "trigger", true, false);
        
        xlocation = new TypedIOPort(this, "xlocation", false, true);
        xlocation.setTypeEquals(BaseType.DOUBLE);
        
        ylocation = new TypedIOPort(this, "ylocation", false, true);
        ylocation.setTypeEquals(BaseType.DOUBLE);
        
        workRange = new Parameter(this, "workRange");
        Type rangeType = new ArrayType(new ArrayType(BaseType.DOUBLE));
        workRange.setTypeEquals(rangeType);
        workRange.setExpression("{{0.0, 500.0}, {0.0, 500.0}}");

    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** Port that receives update message for the spanning tree.
     */
    public TypedIOPort input;
    
    /** Port that receives triggers to output its location.
     * 
     */
    public TypedIOPort trigger;
    
 
    /** Port that output the current x direction location of the pursuer.
     * 
     */
    public TypedIOPort xlocation;
    
    /** Port that output the current y direction location of the pursuer.
     * 
     */
    public TypedIOPort ylocation;
    
    /** Name of the channel for messaging between sensors or pursuer.
     *  This is a string that defaults to "messageChannel".
     */
    public StringParameter messageChannelName;
    
    /** Name of the channel for sensors to detect envader or pursuer.
     *  This is a string that defaults to "signalChannel".
     */
    public StringParameter signalChannelName;    
    
    /** The speed per trigger time of the pursuer.
     * 
     */
    public Parameter speed;
    
    /** The work area of the pursuer.
     * 
     */
    public Parameter workRange;
       

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
 
    /** Calculate its speed when receives a message at its input port
     *  or output is next location when receives token from it trigger
     *  port.
     */
    public void fire() throws IllegalActionException {

        //super.fire();
        
        Entity container = (Entity)this.getContainer();
        Location locationAttribute = (Location)container.getAttribute(
                "_location", Location.class);
        if (locationAttribute == null) {
            throw new IllegalActionException(this,
            "Cannot find a _location attribute of class Location.");
        }
        _myLocation = locationAttribute.getLocation();
        
        if(trigger.hasToken(0)) {
            //System.out.println("receiving an trigger and firing the persure.");
            
            trigger.get(0);
            xlocation.send(0, new DoubleToken(_myLocation[0] + _speed[0]));
            ylocation.send(0, new DoubleToken(_myLocation[1] + _speed[1]));
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
                _parentLocation = new double[length];
                for (int i = 0; i < length ; i++) {
                    _parentLocation[i] = 
                            ((DoubleToken) locationArray
                            .getElement(i)).doubleValue();
                }
                _timeValue = time.doubleValue();
                _parentDepth = d.intValue();
                
                _speed = _getSpeed();
            }
        }
    }
    
    /** Initialize the private varialbles of the sensor node.
     *  @exception IllegalActionException If thrown by the base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        Entity container = (Entity)this.getContainer();
        Location locationAttribute = (Location)container.getAttribute(
                "_location", Location.class);
        if (locationAttribute == null) {
            throw new IllegalActionException(this,
            "Cannot find a _location attribute of class Location.");
        }
        _myLocation =locationAttribute.getLocation();
        _getWorkRange();
        _parentLocation = _myLocation;  
        _parentDepth = 0;
        _timeValue = 0.0;
        _speed = new double[2];
        _speed[0] = 0.0;
        _speed[1] = 0.0;
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Calculate the speed of the pursuer. 
     *  @return An array identifying the speed.
     *  @exception IllegalActionException If the location attribute does
     *   not exist or cannot be evaluated.
     */
    protected double[] _getSpeed() throws IllegalActionException {
        double dx = _parentLocation[0] - _myLocation[0];
        double dy = _parentLocation[1] - _myLocation[1];
        double d = Math.sqrt(dx*dx + dy*dy);
        double[] result = new double[2];
        //FIXME: this check should be given by a parameter. 
        if (d < 0.1) {
            result[0] = 0.0;
            result[1] = 0.0;
        } else {
            double spd = ((DoubleToken)speed.getToken()).doubleValue();
            result[0] = spd*dx/d;
            result[1] = spd*dy/d;
            /*if (!_inWorkRange(_myLocation[0] + result[0], 
                    _myLocation[1] + result[1])) {
                result[0] = 0.0;  
                result[1] = 0.0;           
            }*/
        }
        return result;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////
    private void _getWorkRange() throws IllegalActionException {
        ArrayToken rangeValue = (ArrayToken)workRange.getToken();
        int dimensions = rangeValue.length();
        if (dimensions < 2) {
            throw new IllegalActionException(this,
                    "Invalid range dimension: " + workRange.getExpression());
        }
        _workRange = new double[dimensions][dimensions];
        for (int i = 0; i < dimensions; i++) {
            ArrayToken lowHigh = (ArrayToken)rangeValue.getElement(i);
            if (lowHigh.length() < 2) {
                throw new IllegalActionException(this,
                        "Invalid range: " + workRange.getExpression());
            }
            double low =
                ((DoubleToken)lowHigh.getElement(0)).doubleValue();
            double high =
                ((DoubleToken)lowHigh.getElement(1)).doubleValue();
            if (high < low) {
                throw new IllegalActionException(this,
                        "Invalid range: " + workRange.getExpression());
            } 
            _workRange[i][0] = low;
            _workRange[i][1] = high;
        }
    }
    
    private boolean _inWorkRange(double xLocation, double yLocation) throws IllegalActionException {
        return ((_workRange[0][0]<=xLocation) && 
                (xLocation<=_workRange[0][1]) &&
                (_workRange[1][0]<=yLocation) &&
                (yLocation<=_workRange[1][1]));
    }
    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    /** The parent sensor node's location that it is moving toward to. */
    private double[] _parentLocation;
    
    /** The time when the root node detected the envader. */
    private double _timeValue;
    
    /** The depth of the parent sensor node.
     */   
    private int _parentDepth;
    
    /** The speed of the pursuer.
     */ 
    private double[] _speed;
    
    /** The current location of the pursuer.
     */ 
    private double[] _myLocation;
    
    /** The work area of the pursuer.
     */
    private double[][] _workRange;
}
