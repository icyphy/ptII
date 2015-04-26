/* An actor that writes the value of string tokens to a file, one per line.

 @Copyright (c) 2015-2015 The Regents of the University of California.
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
//// K64FAccelerometer 

/**
 <p>
 This actor uses three output ports to output
 the x, y, and z measurements from an accelerometer</p>


 @author Robert Bui
 @version $Id: Accelerometer.java 71956 2015-04-15 03:03:01Z robert.bui@berkeley.edu $
 @since Ptolemy II 10.0
 @Pt.ProposedRating
 @Pt.AcceptedRating
 */
public class Accelerometer extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Accelerometer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        accX = new TypedIOPort(this, "accX", false, true);
        accX.setTypeEquals(BaseType.DOUBLE);
        accY = new TypedIOPort(this, "accY", false, true);
        accY.setTypeEquals(BaseType.DOUBLE);
        accZ = new TypedIOPort(this, "accZ", false, true);
        accZ.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /** The accX output port.  If this port is connected, then its
     *  input will determine whether an output is produced in any
     *  given firing. The type is double.
     */
    public TypedIOPort accX;

    /** The accY output port.  If this port is connected, then its
     *  input will determine whether an output is produced in any
     *  given firing. The type is double.
     */
    public TypedIOPort accY;

    /** The accZ output port.  If this port is connected, then its
     *  input will determine whether an output is produced in any
     *  given firing. The type is double.
     */
    public TypedIOPort accZ;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Accelerometer newObject = (Accelerometer) super.clone(workspace);

        return newObject;
    }

    /** Initialize the actor by resetting to the first output value.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
    }

    /*
     *  @exception IllegalActionException If the file cannot be opened
     *   or created.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
    }
   

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////


    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    
    

}
