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
@AcceptedRating Red (davisj@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.kernel;

import ptolemy.data.RecordToken;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// PropertyTransformer
/**
Interface for property transformers.  Property transformers are components
that register with the channel a callback that they can use to modify
the transmit properties of a transmission. They can register to modify
the transmit properties of transmissions from a specific port, or they
can register to modify all transmissions through the channel.
Note that if multiple property transformers are registered that can operate
on a given transmission, then the order in which they are applied
is arbitrary.  Thus, property transformers should implement
commutative operations on the properties (such as multiplying
a field by a value).

@author Yang Zhao and Edward Lee
@version $Id$
@since Ptolemy II 3.1
*/
public interface PropertyTransformer {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Modify the specified properties and return a new token with the
     *  modifications.  Implementers may also return the specified token
     *  unchanged.
     *  @param properties The properties to modify.
     *  @param source The sending port.
     *  @param destination The receiving port.
     *  @return The (possibly) modified properties record.
     *  @exception IllegalActionException If the properties cannot be
     *   transformed for some reason.
     */
    public RecordToken transformProperties(
            RecordToken properties,
            WirelessIOPort source,
            WirelessIOPort destination)
            throws IllegalActionException;
}
