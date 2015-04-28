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
//// NeoPixelLEDStrip 

/**
 This actor uses three input ports to light up a NeoPixel LED Strip.

 <p>The code generator supplies the method contents, thus this 
 class has no methods.</p>

 @author Robert Bui
 @version $Id: NeoPixelLEDStrip.java 71956 2015-04-15 03:03:01Z robert.bui@berkeley.edu $
 @since Ptolemy II 11.0
 @Pt.ProposedRating red (robert.bui)
 @Pt.AcceptedRating red (robert.bui)
 */
public class NeoPixelLEDStrip extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public NeoPixelLEDStrip(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        ledIndex = new TypedIOPort(this, "ledIndex", true, false);
        ledIndex.setTypeEquals(new ArrayType(BaseType.INT));
        color = new TypedIOPort(this, "color", true, false);
        color.setTypeEquals(new ArrayType(BaseType.INT));
        clear = new TypedIOPort(this, "clear", true, false);
        clear.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /** The ledIndex input port.  If this port is connected, then its
     *  input will determine whether an output is produced in any
     *  given firing. The type is integer array. 
     */
    public TypedIOPort ledIndex;

    /** The color input port.  If this port is connected, its input will 
     *  will determine the color of the LEDs. The type is integer array.
     */
    public TypedIOPort color;

    /** The clear input port.  If this port is connected, then its
     *  input will clear 
     *  given firing. The type is integer. 
     */
    public TypedIOPort clear;

}
