/* An actor that writes the value of string tokens to a file, one per line.

 @Copyright (c) 2015 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.mbed;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// CC3000Control

/**
 This actor outputs tokens with information to control the CC3000 
 WiFi Module.

 <p>The code generator supplies the method contents, thus this 
 class has no methods.</p>

 @author Robert Bui
 @version $Id: WiFiControl.java 71956 2015-04-30 03:52:01Z robert.bui@berkeley.edu $
 @since Ptolemy II 11.0
 @Pt.ProposedRating red (robert.bui)
 @Pt.AcceptedRating red (robert.bui)
 */
public class CC3000Control extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CC3000Control(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        packetsOut = new TypedIOPort(this, "packetsOut", false, true);
        packetsOut.setTypeEquals(new ArrayType(BaseType.INT)); 
        //packetsOut.setTypeAtLeast(BaseType.BOOLEAN); 
        sendStatus = new TypedIOPort(this, "sendStatus", false, true);
        sendStatus.setTypeEquals(BaseType.BOOLEAN); 
        totalPackets = new TypedIOPort(this, "totalPackets", false, true);
        totalPackets.setTypeEquals(BaseType.INT); 
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /** The packetsOut output port. The type is int array.
     */
    public TypedIOPort packetsOut;

    /** The sendStatus output port. The type is boolean.
     */
    public TypedIOPort sendStatus;

    /** The totalPackets output port. The type is int. 
     */
    public TypedIOPort totalPackets;
}
