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

package ptolemy.domains.wireless.lib;

import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// PowerLossChannel

/**
This is a model of a wireless channel with a specified power loss formula.
This power loss is given as an expression that is evaluated and then
multiplied by the power field of the transmit properties before
delivery to the receiver. For convenience, a variable named
"distance" is available and equal to the distance between the
transmitter and the receiver when the power loss formula is
evaluated.  Thus, the expression can depend on this distance.
In addition, a parameter <i>efficiency</i> can be used in this
formula to reflect constant factor losses (independent of distance).
The value of the power field at the receiver should be interpreted
as a power density (per unit area), so the receiver should again
multiply it by an area (typically the sensor area).
A receiver can then use the resulting power
to compare against a detectable threshold, or to determine
signal-to-interference ratio, for example.
<p>
The default value of <i>powerLossFactor</i> is
<pre>
   efficiency / (4 * PI * distance * distance).
</pre>
This assumes that the transmit power is uniformly distributed
on a sphere of radius <i>distance</i>. The result of multiplying
this by a transmit power is a power density (power per unit area).
The receiver should multiply this power density by the area of the
sensor it uses to capture the energy (such as antenna area).
<p>
The power field of the transmit properties can be supplied by
the transmitter as a record with a <i>power</i> field of type double.
The default value provided by this channel is Infinity, which when
multiplied by any positive constant will yield Infinity, which
presumably will be above any threshold. Thus, the default behavior
is to encounter no power loss and no limits to communication due
to power.
<p>
In addition, this channel can have a specified limited transmission
range. If a transmission range less than Infinity (the default)
is specified, then receivers outside the specified range will
not be notified of transmission, irrespective of power loss.
The transmission range can be specified in one of two ways.
Either it is the value of the <i>range</i> field in the
<i>defaultProperties</i> parameter (a record) of this
channel, or it is provided by the transmitter on each call to
transmit() as a property argument.  To use the latter mechanism,
it is necessary that the property token be an instance of RecordToken
with a field named "range" that can be converted to a double
(i.e., it can be a double, an int, or a byte).
The default value for <i>range</i> is Infinity, which
indicates that by default, there is no range limit.
<p>
Any receiver that is within the specified range when transmit()
is called will receive the transmission, unless the <i>lossProbability</i>
parameter is set to greater than zero.

<p>
@author Edward A. Lee
@version $Id$
*/
public class PowerLossChannel extends LimitedRangeChannel {

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
    public PowerLossChannel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Set the default properties.  Note that this type is a
        // subtype of the base class type (it includes more fields).
        defaultProperties.setExpression(
                "{range = Infinity, power = Infinity}");

        // Force the type of the defaultProperties to at least include
        // the range field. This must be done after setting the value
        // above, because the value in the base class is not a subtype
        // of this specified type.
        String[] labels = {"range", "power"};
        Type[] types = {BaseType.DOUBLE, BaseType.DOUBLE};
        RecordType type = new RecordType(labels, types);
        // Setting an upper bound allows the addition of fields.
        defaultProperties.setTypeAtMost(type);

        efficiency = new Parameter(this, "efficiency");
        efficiency.setTypeEquals(BaseType.DOUBLE);
        efficiency.setExpression("1.0");

        powerLossFactor = new Parameter(this, "powerLossFactor");
        powerLossFactor.setTypeEquals(BaseType.DOUBLE);
        powerLossFactor.setExpression(
                "efficiency / (4 * PI * distance * distance)");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The efficiency of the receiver.
     *  This is used in the default expression for <i>powerLossFactor</i>,
     *  and nowhere else.  If that expression is changed to ignore this
     *  value, then this parameter will have no effect on the channel.
     *  This is a double that defaults to 1.0.
     */
    public Parameter efficiency;

    /** The default formula for the power loss factor.
     *  This value, when multiplied by the transmit power, yields
     *  the power density (per unit area) at a receiver. It can
     *  be an expression that depends on a variable "distance,"
     *  which has the value of the distance between a transmitter
     *  and receiver when this parameter is evaluated.  This is
     *  a double that defaults to
     *  "min(1.0, efficiency / (distance * distance))".
     */
    public Parameter powerLossFactor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Transform the properties to take into account channel losses,
     *  noise, etc., for transmission between the specified sender
     *  and the specified receiver.  In this base class, the
     *  specified properties are merged with the defaultProperties
     *  so that the resulting properties contain at least all the
     *  fields of the defaultProperties.
     *  @param properties The transmit properties.
     *  @param source The sending port.
     *  @param destination The receiving port.
     *  @return The transformed properties.
     *  @exception IllegalActionException If the properties cannot
     *   be transformed. Not thrown in this base class.
     */
    public RecordToken transformProperties(
            RecordToken properties,
            WirelessIOPort source,
            WirelessIOPort destination)
            throws IllegalActionException {
        // Use the superclass to merge the record argument with the
        // default properties and to apply registered transformers.
        RecordToken merged = super.transformProperties(
                properties, source, destination);

        // Get the transmit power.
        ScalarToken transmitPower = (ScalarToken)merged.get("power");

        // Evaluate the power loss factor, which will have been updated
        // with the new value of "distance."
        double powerLossFactorValue
            = ((DoubleToken)powerLossFactor.getToken()).doubleValue();

        // Calculate the receive power.
        double receivePower
            = transmitPower.doubleValue() * powerLossFactorValue;

        // Create a record token with the receive power.
        String[] names = {"power"};
        Token[] values = {new DoubleToken(receivePower)};
        RecordToken newPower = new RecordToken(names, values);

        // Merge the receive power into the merged token.
        RecordToken result = RecordToken.merge(newPower, merged);

        // Report the new received power.
        if (_debugging) {
            _debug(" * receive properties: \""
                    + result.toString()
                    + "\".");
        }
        return result;
    }
}
