/* A Jogl 3D Cube.

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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.jogl.kernel.GLActor3D;
import ptolemy.domains.jogl.kernel.GLToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A Jogl 3D Cube.
 *
 * @author  Yasemin Demir
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Cube3D extends TypedAtomicActor implements GLActor3D {

    /**
     *  Construct a Line3D object in the given container with the given name.
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
    public Cube3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        glOut = new TypedIOPort(this, "glOut");
        glOut.setOutput(true);
        glOut.setTypeEquals(GLToken.GL_TYPE);
        glOut.setMultiport(true);

        width = new Parameter(this, "width");
        width.setExpression("2.0");

        rgbColor = new ColorAttribute(this, "rgbColor");
        rgbColor.setExpression("{1.0, 1.0, 1.0}");

        lineStart = new Parameter(this, "lineStart");
        lineStart.setExpression("{0.0, 0.0, 0.0}");

        lineEnd = new Parameter(this, "lineEnd");
        lineEnd.setExpression("{1.0, 0.0, 0.0}");
    }

    /**
     * The width of the line.
     */
    public Parameter width;

    /** The output port.  The type is a GLToken. */
    public TypedIOPort glOut;

    /** The red, green, blue, and alpha components of the line.  This
     *  parameter must contain an array of double values.  The default
     *  value is {0.0, 0.0, 1.0}, corresponding to opaque black.
     */
    public ColorAttribute rgbColor;

    /** The x,y,z coordinate of the start position of the Cube in the view screen. */
    public Parameter lineStart;

    /** The x,y,z coordinate of the end position of the Cube in the view screen. */
    public Parameter lineEnd;

    /** Render a Jogl OpenGL 3D object.
     *  @param gl The GL object to be rendered.
     *  @exception IllegalActionException If the object cannot be rendered.
     */
    public void render(GL gl) throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }

        ArrayToken lineStartToken = ((ArrayToken) lineStart.getToken());
        ArrayToken lineEndToken = ((ArrayToken) lineEnd.getToken());
        ArrayToken rgbColorValue = ((ArrayToken) rgbColor.getToken());
        DoubleToken widthValue = (DoubleToken) width.getToken();

        gl.glLineWidth((float) widthValue.doubleValue());
        gl.glBegin(GL.GL_LINES);

        gl.glColor3d(((DoubleToken) rgbColorValue.getElement(0)).doubleValue(),
                ((DoubleToken) rgbColorValue.getElement(1)).doubleValue(),
                ((DoubleToken) rgbColorValue.getElement(2)).doubleValue());

        // Origin of the cube.
        gl.glVertex3d(
                ((DoubleToken) lineStartToken.getElement(0)).doubleValue(),
                ((DoubleToken) lineStartToken.getElement(1)).doubleValue(),
                ((DoubleToken) lineStartToken.getElement(2)).doubleValue());

        // End point of the cube.
        gl.glVertex3d(((DoubleToken) lineEndToken.getElement(0)).doubleValue(),
                ((DoubleToken) lineEndToken.getElement(1)).doubleValue(),
                ((DoubleToken) lineEndToken.getElement(2)).doubleValue());

        gl.glEnd();
    }
}
