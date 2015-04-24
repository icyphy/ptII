/* An actor that gets the IP Address.

 Copyright (c) 2015 The Regents of the University of California.
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

 */
package ptolemy.actor.lib.net;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import ptolemy.actor.lib.StringConst;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


///////////////////////////////////////////////////////////////////
//// IPAddress

/**
 Send the IP address of the host to the output port.

 @author Christopher Brooks
 @version $Id: DatagramReader.java 70402 2014-10-23 00:52:20Z cxh $
 @since Ptolemy II 11.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class IPAddress extends StringConst {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public IPAddress(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _updateValueWithIPAddress();
    }

    /** Set the value produced by this actor that of the host address.
     *  @exception IllegalActionException If thrown by the super class
     *  or while setting the value.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _updateValueWithIPAddress();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the value produced by this actor that of the host address.
     *  @exception IllegalActionException If thrown while setting the value.
     */
    private void _updateValueWithIPAddress() throws IllegalActionException {
        try {
            value.setExpression(Inet4Address.getLocalHost().getHostAddress());
        } catch (UnknownHostException ex) {
            throw new IllegalActionException(this, ex, "Could not get the local host?");
        }
    }
    
}
