/* A channel with a specified loss probability.

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

import java.util.Random;

import ptolemy.data.DoubleToken;
import ptolemy.data.LongToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.WirelessChannel;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.domains.wireless.kernel.WirelessReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ErasureChannel

/**
Model of a wireless channel with a specified loss probability. If the
loss probability is greater than zero then on each
call to the transmit() method, for each receiver in range,
with the specified probability, the tranmission to that
receiver will not occur.  Whether a transmission occurs to a particular
receiver is independent of whether it occurs to any other receiver.
<p>
For convenience, a variable named "distance" is available and
equal to the distance between the transmitter and the receiver
when the <i>lossProbability</i> is evaluated.  Thus, the 
loss probability can be given as an expression that depends
on this distance.
<p>
The distance between the transmitter and receiver is determined
by the protected method _distanceBetween(), which is also used
to set the value of the <i>distance</i> variable that can be
used in the expression for loss probability.
In this base class, that method uses
the _location attribute of the transmit and receive actors,
which corresponds to the position of the icon in the Vergil
visual editor.  Subclasses may override this protected method
to provide some other notion of distance.

@author Edward A. Lee
@version $Id$
*/
public class ErasureChannel extends WirelessChannel {

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
    public ErasureChannel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        lossProbability = new Parameter(this, "lossProbability");
        lossProbability.setTypeEquals(BaseType.DOUBLE);
        lossProbability.setExpression("0.0");
        
        seed = new Parameter(this, "seed", new LongToken(0));
        seed.setTypeEquals(BaseType.LONG);
        
        _distance = new Variable(this, "distance");
        _distance.setExpression("Infinity");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The probability that a call to transmit() will fail to deliver
     *  the token to a receiver that is in range.
     *  This is a double that defaults to 0.0, which means that
     *  no loss occurs.
     */
    public Parameter lossProbability;
    
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Initialize the random number generator with the seed, if it
     *  has been given.  A seed of zero is interpreted to mean that no
     *  seed is specified.  In such cases, a seed based on the current
     *  time and this instance of a RandomSource is used to be fairly
     *  sure that two identical sequences will not be returned.
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
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Transmit the specified token to the specified receiver.
     *  If the <i>lossProbability</i> is zero, (the default) then
     *  the specified receiver will receive the token if it has room.
     *  If <i>lossProbability</i> is greater than zero, the token will
     *  be lost with the specified probability, independently
     *  for each channel in range.
     *  Note that in this base class, a port is in range if it refers to
     *  this channel by name and is at the right place in the hierarchy.
     *  This base class makes no use of the properties argument.
     *  But derived classes may limit the range or otherwise change
     *  transmission properties using this argument.
     *  @param token The token to transmit, or null to clear
     *   the specified receiver.
     *  @param sender The sending port.
     *  @param receiver The receiver to which to transmit.
     *  @param properties The transmit properties (ignored in this base class).
     *  @throws IllegalActionException If the token cannot be converted
     *   or if the token argument is null and the destination receiver
     *   does not support clear.
     */
    protected void _transmitTo(
            Token token,
            WirelessIOPort sender,
            WirelessReceiver receiver, 
            RecordToken properties)
            throws IllegalActionException {
        // Get the distance and set the "distance" variable.
        WirelessIOPort destination = (WirelessIOPort)receiver.getContainer();
        double distance = _distanceBetween(sender, destination);
        _distance.setToken(new DoubleToken(distance));

        double experiment = _random.nextDouble();
        double probability = ((DoubleToken)lossProbability.getToken())
                .doubleValue();
        if (_debugging) {
            _debug(" **** loss probability is: " + probability);
        }
        // Make sure a probability of 1.0 is truly a sure loss.
        if (probability < 1.0 && experiment >= probability) {
            super._transmitTo(token, sender, receiver, properties);
        } else {
            if (_debugging) {
                _debug(" * discarding token to: "
                        + receiver.getContainer().getFullName());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A variable that is set to the distance between the transmitter
     *  and the receiver before the
     *  <i>powerLossFactor</i> expression is evaluated.
     */
    protected Variable _distance;

    /** A random number generator.
     */
    protected Random _random = new Random();
}