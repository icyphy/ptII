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
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;

///////////////////////////////////////////////////////////////////
//// GestureRecognition

/**
 This actor uses eight input ports that receives values from the five finger 
 bend sensors and quaternion data of a dataglove and outputs information to 
 update the color, size and position of LEDs in the mbed LED Cube Demo. 
 
 <p>The code generator supplies the method contents, thus this 
 class has no methods.</p>

 @author Robert Bui
 @version $Id: GestureRecognition.java 71956 2015-04-27 03:52:01Z robert.bui@berkeley.edu $
 @since Ptolemy II 11.0
 @Pt.ProposedRating red (robert.bui)
 @Pt.AcceptedRating red (robert.bui)
 */
public class GestureRecognition extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GestureRecognition(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        finger1 = new TypedIOPort(this, "finger1", true, false);
        finger1.setTypeEquals(BaseType.INT);
        finger2 = new TypedIOPort(this, "finger2", true, false);
        finger2.setTypeEquals(BaseType.INT); 
        finger3 = new TypedIOPort(this, "finger3", true, false);
        finger3.setTypeEquals(BaseType.INT); 
        finger4 = new TypedIOPort(this, "finger4", true, false);
        finger4.setTypeEquals(BaseType.INT); 
        finger5 = new TypedIOPort(this, "finger5", true, false);
        finger5.setTypeEquals(BaseType.INT); 
        roll = new TypedIOPort(this, "roll", true, false);
        roll.setTypeEquals(BaseType.INT); 
        pitch = new TypedIOPort(this, "pitch", true, false);
        pitch.setTypeEquals(BaseType.INT); 
        yaw = new TypedIOPort(this, "yaw", true, false);
        yaw.setTypeEquals(BaseType.INT); 
        deltaSize = new TypedIOPort(this, "deltaSize", false, true);
        deltaSize.setTypeEquals(BaseType.INT);
        deltaX = new TypedIOPort(this, "deltaX", false, true);
        deltaX.setTypeEquals(BaseType.INT);
        deltaY = new TypedIOPort(this, "deltaY", false, true);
        deltaY.setTypeEquals(BaseType.INT);
        deltaZ = new TypedIOPort(this, "deltaZ", false, true);
        deltaZ.setTypeEquals(BaseType.INT);
        hue = new TypedIOPort(this, "hue", false, true);
        hue.setTypeEquals(BaseType.DOUBLE);  
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /** The finger1 input port. The type is int.
     */
    public TypedIOPort finger1;

    /** The finger2 input port. The type is int.
     */
    public TypedIOPort finger2;

    /** The finger3 input port. The type is int.
     */
    public TypedIOPort finger3;

    /** The finger4 input port. The type is int.
     */
    public TypedIOPort finger4;

    /** The finger5 input port. The type is int.
     */
    public TypedIOPort finger5;

    /** The roll input port. The type is int.
     */
    public TypedIOPort roll;

    /** The pitch input port. The type is int.
     */
    public TypedIOPort pitch;

    /** The yaw input port. The type is int.
     */
    public TypedIOPort yaw;

    /** The deltaSize output port. The type is int.
     */
    public TypedIOPort deltaSize;

    /** The deltaX output port. The type is int.
     */
    public TypedIOPort deltaX;

    /** The deltaY output port. The type is int.
     */
    public TypedIOPort deltaY;

    /** The deltaZ output port. The type is int.
     */
    public TypedIOPort deltaZ;

    /** The hue output port. The type is double.
     */
    public TypedIOPort hue;
}
