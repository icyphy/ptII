/* An abstract base class for transforming input 3D shape

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.domains.gr.lib;

import javax.media.j3d.Node;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.gr.kernel.GRActor3D;
import ptolemy.domains.gr.kernel.SceneGraphToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// GRTransform

/** An abstract base class for a transform operator of GR shapes. This actor
 will only have meaning in the GR domain.

 The parameter <i>accumulate</i> determines whether transformations are
 accumulated or reset during firing.

 @author C. Fong, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (chf)
 @Pt.AcceptedRating Red (chf)
 */
abstract public class GRTransform extends GRActor3D {
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
        sceneGraphIn = new TypedIOPort(this, "sceneGraphIn");
        sceneGraphIn.setInput(true);
        sceneGraphIn.setMultiport(true);
        sceneGraphIn.setTypeEquals(SceneGraphToken.TYPE);

        sceneGraphOut = new TypedIOPort(this, "sceneGraphOut");
        sceneGraphOut.setOutput(true);
        sceneGraphOut.setTypeEquals(SceneGraphToken.TYPE);

        accumulate = new Parameter(this, "accumulate", new BooleanToken(false));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Boolean value determining whether transformations are
     *  accumulated or reset for each firing.
     */
    public Parameter accumulate;

    /** The input port for connecting to other GR Actors in
     *  the scene graph.
     */
    public TypedIOPort sceneGraphIn;

    /** The output port for connecting to other GR Actors in
     *  the scene graph.
     */
    public TypedIOPort sceneGraphOut;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Setup the transform object.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the value of the <i>accumulate</i> parameter.
     *  @return the accumulation mode
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    protected boolean _isAccumulating() throws IllegalActionException {
        return ((BooleanToken) accumulate.getToken()).booleanValue();
    }

    @Override
    protected void _makeSceneGraphConnection() throws IllegalActionException {
        int width = sceneGraphIn.getWidth();

        for (int i = 0; i < width; i++) {
            if (sceneGraphIn.hasToken(i)) {
                SceneGraphToken nodeToken = (SceneGraphToken) sceneGraphIn
                        .get(i);
                Node node = nodeToken.getSceneGraphNode();
                _addChild(node);
            }
        }

        sceneGraphOut.send(0, new SceneGraphToken(_getNodeObject()));
    }
}
