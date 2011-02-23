/* Load a Jogl OpenGL Model

 @Copyright (c) 2010-2011 The Regents of the University of California.
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

package ptolemy.domains.jogl.lib;

import javax.media.opengl.GL;

import ptolemy.actor.lib.Sink;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.jogl.objLoader.OBJModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** Open a Jogl OpenGL Model.
 * @author  Yasemin Demir
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ModelLoader extends Sink {
    // FIXME: Why is this a sink if it supposed to load a model?
    // shouldn't it be a source?

    /**
     *  Load 3D object in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this Line3D.
     *  @exception IllegalActionException If this actor
     *  is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *  CompositeActor and the name collides with an entity in the container.
     */
    public ModelLoader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        // FIXME: change to "modelName"
        modelName = new StringParameter(this, "3D Model");
        modelName.setExpression("pawn");
        modelName.addChoice("penguin");
        modelName.addChoice("barbell");
        modelName.addChoice("heli");
    }
    
    /** The name of the model to be loaded.  The initial
     * default value is "pawn".
     */
    public StringParameter modelName;
    
    /** Read in a model.
     *  @exception IllegalActionException If there is a problem
     *  reading the input token.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }

        if (input.hasToken(0)) {
            GL gl= null;
            // FIXME: this ignores the modelName parameter.
            String name = "bo";
            float maxSize = 1;
            new OBJModel(name, maxSize, gl, true);
            ObjectToken inputToken = (ObjectToken)input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof GL)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of GL. Got "
                        + inputObject.getClass());
            }
        }
    }
}

