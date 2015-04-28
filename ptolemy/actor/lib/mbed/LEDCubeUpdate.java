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
//// LEDCubeUpdate

/**
 This actor receives values to update the position and color of 
 LEDs in the LED Cube mbed demo. 
 
 <p>The code generator supplies the method contents, thus this 
 class has no methods.</p>

 @author Robert Bui
 @version $Id: LEDCubeUpdate.java 71956 2015-04-27 03:52:01Z robert.bui@berkeley.edu $
 @since Ptolemy II 11.0
 @Pt.ProposedRating red (robert.bui)
 @Pt.AcceptedRating red (robert.bui)
 */
public class LEDCubeUpdate extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LEDCubeUpdate(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        deltaSize = new TypedIOPort(this, "deltaSize", true, false);
        deltaSize.setTypeEquals(BaseType.INT);
        deltaX = new TypedIOPort(this, "deltaX", true, false);
        deltaX.setTypeEquals(BaseType.INT); 
        deltaY = new TypedIOPort(this, "deltaY", true, false);
        deltaY.setTypeEquals(BaseType.INT); 
        deltaZ = new TypedIOPort(this, "deltaZ", true, false);
        deltaZ.setTypeEquals(BaseType.INT); 
        r = new TypedIOPort(this, "r", true, false);
        r.setTypeEquals(BaseType.DOUBLE); 
        g = new TypedIOPort(this, "g", true, false);
        g.setTypeEquals(BaseType.DOUBLE); 
        b = new TypedIOPort(this, "b", true, false);
        b.setTypeEquals(BaseType.DOUBLE); 

        colors = new TypedIOPort(this, "colors", false, true);
        colors.setTypeEquals(new ArrayType(BaseType.INT));
        indexes = new TypedIOPort(this, "indexes", false, true);
        indexes.setTypeEquals(new ArrayType(BaseType.INT));  
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /** The deltaSize input port. The type is int.
     */
    public TypedIOPort deltaSize;

    /** The deltaX input port. The type is int.
     */
    public TypedIOPort deltaX;

    /** The deltaY input port. The type is int.
     */
    public TypedIOPort deltaY;

    /** The deltaZ input port. The type is int.
     */
    public TypedIOPort deltaZ;

    /** The r input port. The type is double.
     */
    public TypedIOPort r;

    /** The g input port. The type is double.
     */
    public TypedIOPort g;

    /** The b input port. The type is double.
     */
    public TypedIOPort b;

    /** The indexes output port. The type is int array.
     */
    public TypedIOPort indexes;

    /** The colors output port. The type is int array.
     */
    public TypedIOPort colors;
}
