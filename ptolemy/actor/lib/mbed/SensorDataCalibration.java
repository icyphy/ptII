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

///////////////////////////////////////////////////////////////////
//// SensorDataCalibration

/**
 This actor receives values from the bend sensors and 
 accelerometer/gyroscope of a data glove and outputs 
 corrected and filtered data. 
 
 <p>The code generator supplies the method contents, thus this 
 class has no methods.</p>

 @author Robert Bui
 @version $Id: SensorDataCalibration.java 72162 2015-04-30 03:52:01Z robert.bui@berkeley.edu $
 @since Ptolemy II 11.0
 @Pt.ProposedRating red (robert.bui)
 @Pt.AcceptedRating red (robert.bui)
 */
public class SensorDataCalibration extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SensorDataCalibration(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        finger1Input = new TypedIOPort(this, "finger1Input", true, false);
        finger1Input.setTypeEquals(BaseType.INT);
        finger2Input = new TypedIOPort(this, "finger2Input", true, false);
        finger2Input.setTypeEquals(BaseType.INT); 
        finger3Input = new TypedIOPort(this, "finger3Input", true, false);
        finger3Input.setTypeEquals(BaseType.INT); 
        finger4Input = new TypedIOPort(this, "finger4Input", true, false);
        finger4Input.setTypeEquals(BaseType.INT); 
        finger5Input = new TypedIOPort(this, "finger5Input", true, false);
        finger5Input.setTypeEquals(BaseType.INT); 
        rollInput = new TypedIOPort(this, "rollInput", true, false);
        rollInput.setTypeEquals(BaseType.INT); 
        pitchInput = new TypedIOPort(this, "pitchInput", true, false);
        pitchInput.setTypeEquals(BaseType.INT); 
        yawInput = new TypedIOPort(this, "yawInput", true, false);
        yawInput.setTypeEquals(BaseType.INT); 
        finger1Output = new TypedIOPort(this, "finger1Output", false, true);
        finger1Output.setTypeEquals(BaseType.INT);
        finger2Output = new TypedIOPort(this, "finger2Output", false, true);
        finger2Output.setTypeEquals(BaseType.INT); 
        finger3Output = new TypedIOPort(this, "finger3Output", false, true);
        finger3Output.setTypeEquals(BaseType.INT); 
        finger4Output = new TypedIOPort(this, "finger4Output", false, true);
        finger4Output.setTypeEquals(BaseType.INT); 
        finger5Output = new TypedIOPort(this, "finger5Output", false, true);
        finger5Output.setTypeEquals(BaseType.INT); 
        rollOutput = new TypedIOPort(this, "rollOutput", false, true);
        rollOutput.setTypeEquals(BaseType.INT); 
        pitchOutput = new TypedIOPort(this, "pitchOutput", false, true);
        pitchOutput.setTypeEquals(BaseType.INT); 
        yawOutput = new TypedIOPort(this, "yawOutput", false, true);
        yawOutput.setTypeEquals(BaseType.INT); 
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /** The finger1Input input port. The type is int.
     */
    public TypedIOPort finger1Input;

    /** The finger2Input input port. The type is int.
     */
    public TypedIOPort finger2Input;

    /** The finger3Input input port. The type is int.
     */
    public TypedIOPort finger3Input;

    /** The finger4Input input port. The type is int.
     */
    public TypedIOPort finger4Input;

    /** The finger5Input input port. The type is int.
     */
    public TypedIOPort finger5Input;

    /** The rollInput input port. The type is int.
     */
    public TypedIOPort rollInput;

    /** The pitchInput input port. The type is int.
     */
    public TypedIOPort pitchInput;

    /** The yawInput input port. The type is int.
     */
    public TypedIOPort yawInput;

    /** The finger1Output output port. The type is int.
     */
    public TypedIOPort finger1Output;

    /** The finger2Input output port. The type is int.
     */
    public TypedIOPort finger2Output;

    /** The finger3Output output port. The type is int.
     */
    public TypedIOPort finger3Output;

    /** The finger4Output output port. The type is int.
     */
    public TypedIOPort finger4Output;

    /** The finger5Outpu output port. The type is int.
     */
    public TypedIOPort finger5Output;

    /** The rollOutput output port. The type is int.
     */
    public TypedIOPort rollOutput;

    /** The pitchOutput output port. The type is int.
     */
    public TypedIOPort pitchOutput;

    /** The yawOutput output port. The type is int.
     */
    public TypedIOPort yawOutput;
}
