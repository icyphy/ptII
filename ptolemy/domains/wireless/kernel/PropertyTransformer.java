/* Interface for property transformers in the wireless domain.

 Copyright (c) 2003 The Regents of the University of California.
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

package ptolemy.domains.wireless.kernel;

import ptolemy.data.RecordToken;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// PropertyTransformer
/**
Interface for transmit property transformers.  Property transformers 
are components that register with the channel a callback that they can 
use to modify the transmission properties of a transmission. They can 
register to modify the transmission properties of transmissions from a 
specific port, or they can register to modify all transmissions 
through the channel. Note that if multiple property transformers are 
registered that can operate on a given transmission, then the order 
in which they are applied is arbitrary. Thus, property transformers 
should implement commutative operations on the properties (such as 
multiplying a field by a value). 
<p>
It is possible to return the modified transmission properties with 
different record types from the previours transmit property, i.e. some  
record fields may be removed or added. The channel should merge the 
returned transmission properties with its <i>defaultProperties<i> to 
make sure that the transmission properties contains at least all the  
fields of the defaultProperties.
<p>
Anything that needs to know the locations of the sender and receiver
to alter the transimt properties should implement this interface. 
For example, for a sender with a specific antenna pattern to send data
to receivers, it needs to know each receiver's position and its own 
position to calculator the antenna gain. To do so, the sender can 
implement this interface and register itself with the wireless channel 
it uses. The channel will call the <i>transformProperties</I> methods 
later and provides the required information for the sender to calculate 
the antenna gain to a specific receiver(@see TransformProperties).

The modified property transformer   

@see WirelessChannel
@author Yang Zhao and Edward Lee
@version $Id$
@since Ptolemy II 3.1
*/
public interface PropertyTransformer {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Modify the transmission properties and return a new token with the
     *  modifications. Implementers may also return the specified token
     *  unchanged.
     *  @param properties The transmission properties to modify.
     *  @param source The sending port.
     *  @param destination The receiving port.
     *  @return The (possibly) modified transmission properties.
     *  @exception IllegalActionException If the properties cannot be
     *   transformed for some reason.
     */
    public RecordToken transformProperties(
            RecordToken properties,
            WirelessIOPort source,
            WirelessIOPort destination)
            throws IllegalActionException;
}
