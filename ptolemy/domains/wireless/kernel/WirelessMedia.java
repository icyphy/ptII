/* Interface for actors.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (davisj@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.kernel;

import java.util.List;

import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// WirelessMedia
/**
Define an interface for (Atomic)WirelessChannel and CompositeWirelessChannel.

FIXME: It might be preferred to use "WirelessChannel" for this and change
WirelessChannel to AtomicWirelessChannel. I decided to use this name so that
I need to change fewer classes at this stage...
@author Yang
@version $Id$
@since Ptolemy II 3.1
*/
public interface WirelessMedia {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public String getName();
    public List listeningInputPorts() throws IllegalActionException;

    public List listeningOutputPorts() throws IllegalActionException;

    public List sendingInputPorts() throws IllegalActionException;

    public List sendingOutputPorts() throws IllegalActionException;

    public void transmit(Token token, WirelessIOPort port,
            RecordToken properties)
            throws IllegalActionException;
}
