/* A channel with a limited transmission range, delay, and packet loss.

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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.lib;

import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// LimitedRangeChannel

/**
This is a model of a wireless channel with a specified transmission range.
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
is called will receive the transmission.  The distance between
the transmitter and receiver is determined by the protected
method _distanceBetween().  In this base class, that method uses
the _location attribute of the transmit and receive actors,
which corresponds to the position of the icon in the Vergil
visual editor.  Subclasses may override this protected method
to provide some other notion of distance.

@author Edward A. Lee
@version $Id$
*/
public class LimitedRangeChannel extends DelayChannel {

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
    public LimitedRangeChannel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Force the type of the defaultProperties to at least include
        // the range field.
        String[] labels = {"range"};
        Type[] types = {BaseType.DOUBLE};
        RecordType type = new RecordType(labels, types);
        // Setting an upper bound allows the addition of fields.
        defaultProperties.setTypeAtMost(type);
        defaultProperties.setExpression("{range=Infinity}");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the fact that the specified Settable has changed.
     *  This class registers as a listener to attributes that
     *  specify transmit properties.  If those change, then
     *  it is necessary to invalidate the cache of receivers
     *  in range.  This method simply invalidates the cache if
     *  it is called, so subclasses should be careful to not
     *  register as value listeners unnecessarily to objects that
     *  do not affect the validity of this cache.
     *  @param settable The object that has changed value.
     */
    public void valueChanged(Settable settable) {
        _receiversInRangeCacheValid = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if the specified port is in range of the
     *  specified source port, assuming the source port transmits with
     *  the specified properties.  If the properties are an instance of
     *  DoubleToken, then that token is assumed to be the range of the
     *  transmission.  This method returns true if the distance between
     *  the source and the destination is less than or equal to the
     *  value of the properties.  If the properties argument is not
     *  a DoubleToken, then it simply returns true.
     *  @param source The source port.
     *  @param destination The destination port.
     *  @param properties The range of transmission.
     *  @return True if the destination is in range of the source.
     *  @exception IllegalActionException If it cannot be determined
     *   whether the destination is in range (not thrown in this base
     *   class).
     */
    protected boolean _isInRange(
            WirelessIOPort source,
            WirelessIOPort destination,
            RecordToken properties)
            throws IllegalActionException {
        double range = Double.POSITIVE_INFINITY;
        boolean rangeIsSet = false;
        if (properties != null) {
            Token field = properties.get("range");
            if (field instanceof ScalarToken) {
                // NOTE: This may throw a NotConvertibleException, if,
                // example, a Complex or a Long is given.
                range = ((ScalarToken)field).doubleValue();
                rangeIsSet = true;
            }
        }
        if (!rangeIsSet) {
            // Type constraints in the constructor make the casts safe.
            RecordToken defaultPropertiesValue
                    = (RecordToken)defaultProperties.getToken();
            // Type of the field must be convertible to double, but
            // need not actually be a double.
            ScalarToken field =
                (ScalarToken)defaultPropertiesValue.get("range");
            range = field.doubleValue();
        }
        boolean result = (_distanceBetween(source, destination) <= range);

        // Whether a port is in range depends on the
        // transmit properties of this sender, so we set up
        // a listener to be notified of any changes in those
        // properties.  Note that we need to do this even if the
        // properties argument to this method is null because while
        // a port may specify no properties now, it may later acquire
        // properties.
        if (source.getOutsideChannel() == this) {
            source.outsideTransmitProperties.addValueListener(this);
        } else {
            source.insideTransmitProperties.addValueListener(this);
        }

        return result;
    }
}
