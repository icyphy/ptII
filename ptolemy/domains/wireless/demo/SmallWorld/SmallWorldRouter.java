/* A actor that routes a message via a short path.

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

package ptolemy.domains.wireless.demo.SmallWorld;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.EllipseAttribute;

//////////////////////////////////////////////////////////////////////////
//// SmallWorldRouter

/**
This actor implements a routing algrithm to route a message to the destination
via a short path based only on local information. It assumes that it knows which
nodes are in range and the location of that node. It also assumes that the
location of the destination is known. Based on this information, it finds the
node that is closest to the destination from its connected node set.
<p>
We assume that the actor are connected to nodes inside a particular range, 
specified by the <i>sureRange<i> parameter, for sure. Outside this range, 
it may connected to a node with probability propotional to the r-th inverse power
of the distance between them. Whether it is connected to a particular
node is independent of whether it is connected to any other node.
<p>
For convenience, a variable named "distance" is available and
equal to the distance between this actor and other actors. The 
loss probability can be given as an expression that depends
on this distance.
<p>
The distance between the transmitter and receiver is determined
by the protected method _distanceBetween(), which is also used
to set the value of the <i>distance</i> variable that can be
used in the expression for loss probability.

@author Yang Zhao
@version $ $
*/
public class SmallWorldRouter extends TypedAtomicActor {

    /** Construct a channel with the given name and container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown. If the name argument
     *  is null, then the name is set to the empty string.
     *  @param container The container.
     *  @param name The name of the channel.
     *  @exception IllegalActionException If the container is incompatible.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */
    public SmallWorldRouter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create and configure the parameters.
        inputChannelName = new StringParameter(this, "inputChannelName");
        inputChannelName.setExpression("InputChannel");

        outputChannelName = new StringParameter(this, "outputChannelName");
        outputChannelName.setExpression("OutputChannel");
        
        testChannelName = new StringParameter(this, "testChannelName");
        testChannelName.setExpression("testChannel");
        
        // Create and configure the ports.
        input = new WirelessIOPort(this, "input", true, false);
        input.outsideChannel.setExpression("$inputChannelName");
        //FIXME: this type configuration doesn't work.
        //TypeAttribute inputPortType = new TypeAttribute(input, "type");
        //inputPortType.setExpression
        //    ("{data=double, destination=String, routeTo=String, hops=int}");
        
        output = new WirelessIOPort(this, "output", false, true);
        output.outsideChannel.setExpression("$outputChannelName");
        //TypeAttribute outputPortType = new TypeAttribute(output, "type");
        //outputPortType.setExpression
        //    ("{data=double, destination=String, routeTo=String, hops=int}");
        
        test = new WirelessIOPort(this, "test", false, true);
        test.outsideChannel.setExpression("$testChannelName");
        test.setTypeEquals(BaseType.INT);
                    
        lossProbability = new Parameter(this, "lossProbability");
        lossProbability.setTypeEquals(BaseType.DOUBLE);
        lossProbability.setExpression("0.0");

        sureRange = new Parameter(this, "sureRange");
        sureRange.setToken("100.0");
        sureRange.setTypeEquals(BaseType.DOUBLE);
        
        output.outsideTransmitProperties.setExpression("{range=Infinity}");        
 
        delay = new Parameter(this, "delay", new DoubleToken(1.0));
        delay.setTypeEquals(BaseType.DOUBLE);
               
        seed = new Parameter(this, "seed", new LongToken(0));
        seed.setTypeEquals(BaseType.LONG);
        
        doublePath = new Parameter(this, "doublePath", new LongToken(0));
        doublePath.setToken("false");
        doublePath.setTypeEquals(BaseType.BOOLEAN);
        
        _distance = new Variable(this, "distance");
        _distance.setExpression("Infinity");
        
        // Hide the ports in Vergil.
        new Attribute(output, "_hide");
        new Attribute(input, "_hide");
        new Attribute(test, "_hide");
        
        // Create an icon for this sensor node.
        EditorIcon node_icon = new EditorIcon(this, "_icon");
        
        // The icon has two parts: a circle and an antenna.
        // Create a circle that indicates the signal radius.
        _circle = new EllipseAttribute(node_icon, "_circle");
        _circle.centered.setToken("true");
        _circle.width.setToken("sureRange*2");
        _circle.height.setToken("sureRange*2");
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
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** The input port for messages to route. It requires the received
     *  token to be a record token has type:
     *  {data=double, destination=String, routeTo=String, hops=int}.
     */
    public WirelessIOPort input;

    /** Name of the input channel. This is a string that defaults to
     *  "InputChannel".
     */
    public StringParameter inputChannelName;

    /** The output port that send a message to connected nodes.
     *  This has type:
     *  {data=double, destination=String, routeTo=String, hops=int}.    
     */
    public WirelessIOPort output;
    
    /** This port is for analysis uses. When the destination node 
     *  receives the message, it outputs an int token to indicate
     *  how many hops from the source.
     */
    public WirelessIOPort test;

    /** Name of the output channel. This is a string that defaults to
     *  "OutputChannel".
     */
    public StringParameter outputChannelName;

    /** Name of the test channel. This is a string that defaults to
     *  "testChannel".
     */
    public StringParameter testChannelName;
    
    /** The probability that a connection between two node will fail 
     *  to happen. This is a double that defaults to 0.0, which means that
     *  no loss occurs.
     * FIXME: get a better name for it.
     */
    public Parameter lossProbability;
    
    /** The for sure connected range between two nodes. This is a double that
     *  defaults to 100.0.  The icon for this sensor node includes
     *  a circle with this as its radius.
     */
    public Parameter sureRange;
    
    /** The time required for relaying a message. This is a double that
     *  defaults to 1.0.  
     */
    public Parameter delay;
    
    /** The seed that controls the random number generation.
     *  A seed of zero is interpreted to mean that no seed is specified,
     *  which means that each execution of the model could result in
     *  distinct data. For the value 0, the seed is set to
     *  System.currentTimeMillis() + hashCode(), which means that
     *  with extremely high probability, two distinct actors will have
     *  distinct seeds.  However, current time may not have enough
     *  resolution to ensure that two subsequent executions of the
     *  same model have distinct seeds.
     *  This parameter contains a LongToken, initially with value 0.
     */
    public Parameter seed;
    
    /** If true, then this actor will also route the message to the node that
     *  is the second closest to the destination among all its connected nodes.
     *  FIXME: This is still under experiment. The issue I try to address is that
     *  some links may fail and long links may fail with higher probability. If so,
     *  routing a message to two paths may improve the hit probability. However, this
     *  will also cost more energy and also lower the capacity of the network.
     *  There is a tradeoff. A simple idea is to use a threshold to control the 
     *  exponentially increased branches. For example, with a threshold equals 2, 
     *  it only route to two pathes for the first hop.
     */
    public Parameter doublePath;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Check whether it is the destination of the massage, if so,
     *  change the color of the icon to red and schedule
     *  another firing after 1.0 time unit to change it back to white.
     *  If not, check whether it is on the short path (hops ==0 means
     *  it detected a signal and would initiate a message). If yes, change
     *  its icon to green, calculate the distance between a node connected
     *  to it and the destination node, choose the one closest the the 
     *  destination to be the next node on the short path, and schedule 
     *  another firing after some delay time to output the message and 
     *  change its icon back to white.  
     */
    public void fire() throws IllegalActionException {
        
        if (input.hasToken(0)) {
            RecordToken in = (RecordToken)input.get(0);
            double data = ((DoubleToken)in.get("data")).doubleValue();
            String destination = ((StringToken)in.get("destination"))
                    .stringValue();
            String routeTo = ((StringToken)in.get("routeTo")).stringValue();
            int hops = ((IntToken)in.get("hops")).intValue();
            /*System.out.println(getName() + " receive a event with : " + "\n"
                             + "destination = " + destination + "\n"
                             + "routeTo = " + routeTo + "\n"
                             + "hops = " + hops);
            */
            
            if (getName().equals(destination)){
                // Change the color of the icon to red.
                _circle2.fillColor.setToken("{1.0, 0.0, 0.1, 0.7}");
                //_isRed = true;
                test.send(0, new IntToken(hops+1));
                //Call fireAt to set the color back to white after the delay time.
                Director director = getDirector();
                double delayTime = ((DoubleToken)delay.getToken()).doubleValue();
                double time = director.getCurrentTime() + delayTime;
                director.fireAt(this, time);    
            } else if (getName().equals(routeTo) || hops == 0){
                // Change the color of the icon to green.
                _circle2.fillColor.setToken("{0.0, 1.0, 0.0, 1.0}");
                CompositeEntity container = (CompositeEntity)getContainer();
                Entity destNode = container.getEntity(destination);
                Locatable destLocation = (Locatable)destNode.getAttribute(
                        "_location", Locatable.class);
                Locatable myLocation = (Locatable)this.getAttribute(
                        "_location", Locatable.class);
                if (destLocation == null || myLocation == null) {
                    throw new IllegalActionException(
                            "Cannot determine location for node "
                            + destNode.getName()
                            + ".");
                    
                }
                Iterator nodes = _connectedNodes.iterator();
                double minDistance = _distanceBetween(destLocation, myLocation);
                String to = " ";
                boolean multi = ((BooleanToken)doublePath.getToken()).booleanValue();
                double nextMinDistance = _distanceBetween(destLocation, myLocation);
                String to2 = " ";
                while(nodes.hasNext()) {
                    Entity node = (Entity) nodes.next();
                    Locatable location = (Locatable)node.getAttribute(
                                        "_location", Locatable.class);
                    if (location == null) {
                        throw new IllegalActionException(
                                "Cannot determine location for node "
                                + node.getName()
                                + ".");
                    
                    }
                    double d = _distanceBetween(destLocation, location);
                    if (multi) {
                        if (d < minDistance){
                            nextMinDistance = minDistance;
                            to2 = to;
                            minDistance = d;
                            to = node.getName();    
                        } else if (d < nextMinDistance) {
                            nextMinDistance = d;
                            to2 = node.getName();
                        }
                    } else {
                        if (d < minDistance){
                            minDistance = d;
                            to = node.getName();
                        }      
                    }
                }
                
                // Request refiring after a certain amount of time specified
                // by the <i>delay<i> parameter.
                Director director = getDirector();
                Token[] values = {new DoubleToken(data),
                                  new StringToken(destination),
                                  new StringToken(to),
                                  new IntToken(hops+1)};
                double delayTime = ((DoubleToken)delay.getToken()).doubleValue();
                double time = director.getCurrentTime() + delayTime;
                if (_receptions == null) {
                    _receptions = new HashMap();
                }
                Double timeDouble = new Double(time);
                String[] labels = {"data", "destination", "routeTo", "hops"};
                RecordToken result = new RecordToken(labels, values);
                _receptions.put(timeDouble, result);
            
                director.fireAt(this, time);
                if(multi) {
                    Token[] values2 = {new DoubleToken(data),
                                      new StringToken(destination),
                                      new StringToken(to2),
                                      new IntToken(hops+1)};
                    if (_receptions == null) {
                        _receptions = new HashMap();
                    }
                    RecordToken result2 = new RecordToken(labels, values2);
                    _receptions.put(timeDouble, result2);
            
                    director.fireAt(this, time + delayTime);
                }
                //output.send(0, result);
            } 
        } else {
            if (_receptions != null) {
                // We may be getting fired because of an impending event.
                double currentTime = getDirector().getCurrentTime();
                Double timeDouble = new Double(currentTime);
                RecordToken reception = (RecordToken)_receptions.get(timeDouble);
                if (reception != null) {
                    // The time matches a pending reception.
                    _receptions.remove(reception);
                    // Use the superclass, not this class, or we just delay again.
                    output.send(0, reception);

                }
            }
            //if (_isRed){
                //Set color back to white.
                _circle2.fillColor.setToken("{1.0, 1.0, 1.0, 1.0}");
                //_isRed = false;
            //}            
        }
    }    

    /** Initialize the random number generator with the seed, if it
     *  has been given.  A seed of zero is interpreted to mean that no
     *  seed is specified.  In such cases, a seed based on the current
     *  time and this instance of a RandomSource is used to be fairly
     *  sure that two identical sequences will not be returned.
     *  Decide all the nodes that are connected to it.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        long seedValue = ((LongToken)(seed.getToken())).longValue();
        if (seedValue != (long)0) {
            _random.setSeed(seedValue);
        } else {
            _random.setSeed(System.currentTimeMillis() + hashCode());
        }
        _circle.fillColor.setToken("{0.0, 0.0, 1.0, 0.05}");
        _circle.lineColor.setToken("{0.0, 0.0, 1.0, 0.05}");
        _circle2.fillColor.setToken("{1.0, 1.0, 1.0, 1.0}");
        _circle2.lineColor.setToken("{0.0, 0.5, 0.5, 1.0}");
        _connectedNodes = (LinkedList) nodesInRange(output);
        //_isRed = false;
        //_values = new Token[4];
    }

    /** Explicitly declare which inputs and outputs are not dependent.
     *  
     */
    public void removeDependencies() {
        super.removeDependency(input, output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    /** Return the distance between two location.  This is a convenience
     *  method provided to make it easier to calculate the distance between
     *  two locations.
     *  @param location1 The first location.
     *  @param location2 The second location.
     *  @return The distance between the two locations.
     *  @exception IllegalActionException If the distance
     *   cannot be determined.
     */
    protected double _distanceBetween(
            Locatable location1, Locatable location2)
            throws IllegalActionException {
        double[] p1 = location1.getLocation();
        double[] p2 = location2.getLocation();
        return Math.sqrt((p1[0] - p2[0])*(p1[0] - p2[0])
                + (p1[1] - p2[1])*(p1[1] - p2[1]));
    }
    
    /** Return the list of nodes that can receive from the specified
     *  port. 
     *  @param sourcePort The sending port.
     *  @return A list of instances of Entity.
     *  @exception IllegalActionException If a location of a port cannot be
     *   evaluated.
     */
    protected List nodesInRange(
            WirelessIOPort sourcePort)
            throws IllegalActionException {
        List nodesInRangeList = new LinkedList();
        CompositeEntity container = (CompositeEntity) getContainer();
        Iterator ports = ModelTopology.listeningInputPorts(container, 
                outputChannelName.stringValue()).iterator();
        while (ports.hasNext()) {
            WirelessIOPort port = (WirelessIOPort)ports.next();

            // Skip ports contained by the same container as the source.
            if (port.getContainer() == sourcePort.getContainer()) continue;

            double distance = ModelTopology.distanceBetween(sourcePort, port);
            _distance.setToken(new DoubleToken(distance));

            double experiment = _random.nextDouble();
            double probability = ((DoubleToken)lossProbability.getToken())
                    .doubleValue();
            if (_debugging) {
                _debug(" **** loss probability is: " + probability);
            }
            // Make sure a probability of 1.0 is truly a sure loss.
            if (probability < 1.0 && experiment >= probability) {
                nodesInRangeList.add(port.getContainer());
            } 
        }
        return nodesInRangeList;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A variable that is set to the distance between the transmitter
     *  and the receiver before the
     *  <i>lossProbability</i> expression is evaluated.
     */
    protected Variable _distance;

    /** A random number generator.
     */
    protected Random _random = new Random();
    
    /** A list of entities that can receive message from this actor.
     */
    protected LinkedList _connectedNodes = new LinkedList();    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables               ////
    /** Icon indicating the communication region. */
    private EllipseAttribute _circle;
    
    /** Icon of this actor. */
    private EllipseAttribute _circle2;
       
    /** Messages received but haven't been relayed out.
     */
    private HashMap _receptions;
}