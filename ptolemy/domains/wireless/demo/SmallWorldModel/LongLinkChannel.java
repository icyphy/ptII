/* A channel with a distance-dependent power loss.

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

package ptolemy.domains.wireless.demo.SmallWorldModel;

import ptolemy.actor.IOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.domains.wireless.lib.LimitedRangeChannel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// PowerLossChannel

/**
This is a model of a wireless channel with a specified connection
probability formula. This probability is given as an expression that
is evaluated and then used to decide whether a receiver can receive
the message. For convenience, a variable named
"distance" is available and equal to the distance between the
transmitter and the receiver when the probability formula is
evaluated.  Thus, the expression can depend on this distance.

<p>
The default value of <i>linkProbability</i> is
<pre>
   1 / (distance * distance).
</pre>

<p>
The distance between the transmitter and receiver is determined by the 
protected method _distanceBetween(), which is also used to set the value
of the <i>distance</i> variable used to calculate the probability.
This method overwrite the base class method to return a "grid distance".
A grid distance is the number of grids between two nodes on the grid. 

FIXME: This Channel assumes that the port's container has a "gridID" 
attribute and use this to calculate the grid distance.   


@author Yang Zhao
@version $ $
*/
public class LongLinkChannel extends LimitedRangeChannel {

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
    public LongLinkChannel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _distance = new Variable(this, "distance");
        _distance.setExpression("Infinity");

        linkProbability = new Parameter(this, "linkProbability");
        linkProbability.setTypeEquals(BaseType.DOUBLE);
        // FIXME: Check this formula.
        linkProbability.setExpression(
                "1 / (distance * distance)");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////


    /** The default formula for the conncetion probability factor.
     * It can be an expression that depends on a variable "distance,"
     *  which has the value of the grid distance between a transmitter
     *  and receiver when this parameter is evaluated.  This is
     *  a double that defaults to "1 / (distance * distance))".
     */
    public Parameter linkProbability;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                    ////

    /** Return the distance between two ports.  Overide the method of the
     *  base class to return the grid distance of two ports:
     *  d(port1, port2) = d({i,k}, {j,l})=|i-j| + |k-l|;
     *  @param port1 The first port.
     *  @param port2 The second port.
     *  @return The grid distance between the two ports.
     *  @exception IllegalActionException If the distance
     *   cannot be determined.
     */
    protected double _distanceBetween(
            WirelessIOPort port1, WirelessIOPort port2)
            throws IllegalActionException {
        int[] p1 = _getGrid(port1);
        int[] p2 = _getGrid(port2);
        return Math.abs(p1[0] - p2[0]) + Math.abs(p1[1] - p2[1]);
    }

    /** Return the grid id of the given port. It assumes that the container 
     *  of the port has a gridID parameter and return the value of that.
     *  @param port A port with a location.
     *  @return The gridID of the port container.
     *  @throws IllegalActionException If a valid gridID attribute cannot
     *   be found.
     */
    protected int[] _getGrid(IOPort port) throws IllegalActionException {
        Entity container = (Entity)port.getContainer();
        Parameter gridId = (Parameter) container.getAttribute("gridID");
        if (gridId == null) {
            throw new IllegalActionException(
                    "Cannot determine location for port "
                    + port.getName()
                    + ".");
        }
        //Assume the parameter contains an int arrayToken.
        ArrayToken id = (ArrayToken) gridId.getToken();
        int[] results = new int[id.length()];
        for (int i=0; i<id.length(); i++) {
            results[i] =((IntToken)id.getElement(i)).intValue();
        }
        return results;
    }
    
    /** Return true if the specified port is in range of the
     *  specified source port. Overwrite the base class to return 
     *  whether a receiver is in range according to the probability
     *  distribution specified by the <i>linkProbability<i> parameter.
     *  @param source The source port.
     *  @param destination The destination port.
     *  @param properties The range of transmission.
     *  @return True if the destination is in range of the source.
     *  @throws IllegalActionException If it cannot be determined
     *   whether the destination is in range (not thrown in this base
     *   class).
     */
    protected boolean _isInRange(
            WirelessIOPort source,
            WirelessIOPort destination,
            RecordToken properties)
            throws IllegalActionException {
        
        double experiment = _random.nextDouble();
        double distance = _distanceBetween(source, destination);
        _distance.setToken(new DoubleToken(distance));
        
        double probability
                        = ((DoubleToken)linkProbability.getToken()).doubleValue();        
        // Make sure a probability of 1.0 is truly a sure thing.
        if (probability == 1.0 || experiment >= probability) {
            return true;
        } else {
           return false;
        }
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A variable that is set to the grid distance between the transmitter
     *  and the receiver before the
     *  <i>linkProbability</i> expression is evaluated.
     */
    protected Variable _distance;
}
