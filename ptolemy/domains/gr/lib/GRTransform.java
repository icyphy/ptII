/* An abstract base class for transforming input 3D shape

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.gr.lib;

import ptolemy.actor.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.dt.kernel.DTDebug;
import ptolemy.domains.gr.kernel.*;

import javax.media.j3d.*;
import javax.vecmath.*;

//////////////////////////////////////////////////////////////////////////
//// GRTransform

/** An abstract base class for a transform operator of GR shapes. This actor
will only have meaning in the GR domain.

The parameter <i>accumulate</i> determines whether transformations are 
accumulated or reset during firing.

@author C. Fong
*/
public class GRTransform extends GRActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GRTransform(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setInput(true);
	    input.setMultiport(true);
	    
	    output = new TypedIOPort(this, "output");
	    output.setOutput(true);
	    output.setTypeEquals(BaseType.GENERAL);
	    
	    accumulate = new Parameter(this, "accumulate", new BooleanToken(false));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port for connecting to other GR Actors in
     *  the scene graph
     */
    public TypedIOPort input;
    
    /** The output port for connecting to other GR Actors in
     *  the scene graph
     */
    public TypedIOPort output;
    
    /** Boolean value determining whether transformations are 
     *  accumulated or reset for each firing
     */
    public Parameter accumulate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a scene graph child node to this actor
     */
    public void addChild(Node node) {
        transformNode.addChild(node);
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        GRTransform newObject = (GRTransform)super.clone(workspace);
        newObject.input  = (TypedIOPort) newobj.getPort("input");
        newObject.output = (TypedIOPort) newobj.getPort("output");
        return newObject;
    }
    
    /** Return the encapsulated Java3D node of this 3D actor. The encapsulated
     *  node for this actor is a Java3D TransformGroup.
     *  @return the Java3D TransformGroup
     */    
    public Node getNodeObject() {
        return (Node) transformNode;
    }

    /** Setup the transform object
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
  	    transformNode = new TransformGroup();
	    transformNode.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /**  Return the value of the <i>accumulate</i> parameter
     *  @return the accumlation mode
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    protected boolean _isAccumulating() throws IllegalActionException {
        return ((BooleanToken) accumulate.getToken()).booleanValue();
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    
    protected TransformGroup transformNode;
    
}
