/* A GR Shape consisting of a polyhedral box.

Copyright (c) 1998-2004 The Regents of the University of California.
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

import java.net.URL;

import javax.media.j3d.Node;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Primitive;

//////////////////////////////////////////////////////////////////////////
//// Box3D

/** This actor contains the geometry and appearance specifications for a
    box.  The output port is used to connect this actor to the Java3D scene
    graph. This actor will only have meaning in the GR domain.

    The parameters <i>xLength</i>, <i>yHeight</i>, and <i>zWidth</i>
    determine the dimensions of box.

    @author Chamberlain Fong, Edward A. Lee
    @version $Id$
    @since Ptolemy II 1.0
    @Pt.ProposedRating Green (eal)
    @Pt.AcceptedRating Green (liuxj)
*/
public class Box3D extends GRShadedShape {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Box3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        xLength = new Parameter(this, "xLength");
        xLength.setExpression("0.1");
        xLength.setTypeEquals(BaseType.DOUBLE);

        yHeight = new Parameter(this, "yHeight");
        yHeight.setExpression("0.1");
        yHeight.setTypeEquals(BaseType.DOUBLE);

        zWidth = new Parameter(this, "zWidth");
        zWidth.setExpression("0.1");
        zWidth.setTypeEquals(BaseType.DOUBLE);

        zWidth.moveToFirst();
        yHeight.moveToFirst();
        xLength.moveToFirst();

        // The flat parameter doesn't make much sense in this case.
        flat.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The length of the box in the x-axis. This has type double
     *  with default 0.1.
     */
    public Parameter xLength;

    /** The height of the box in the y-axis. This has type double
     *  with default 0.1.
     */
    public Parameter yHeight;

    /** The width of the box in the z-axis. This has type double
     *  with default 0.1.
     */
    public Parameter zWidth;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the shape and appearance of the box.
     *  @exception IllegalActionException If the value of some
     *   parameters can't be obtained.
     */
    protected void _createModel() throws IllegalActionException {
        super._createModel();

        int primitiveFlags = Primitive.GENERATE_NORMALS;
        URL textureURL = texture.asURL();
        if (textureURL != null) {
            primitiveFlags = primitiveFlags | Primitive.GENERATE_TEXTURE_COORDS;
        }
        
        // Although it is completely undocument in Java3D, the "dimension"
        // parameters of the box are more like radii than like width,
        // length, and height. So we have to divide by two.
        float height = (float)(((DoubleToken)
                yHeight.getToken()).doubleValue()/2.0);

        float length = (float)(((DoubleToken)
                xLength.getToken()).doubleValue()/2.0);

        float width = (float)(((DoubleToken)
                zWidth.getToken()).doubleValue()/2.0);

        _containedNode = new Box(length, height, width,
                primitiveFlags, _appearance);
    }

    /** Return the Java3D box.
     *  @return The Java3D box.
     */
    protected Node _getNodeObject() {
        return _containedNode;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables               ////

    /** The box. */
    private Box _containedNode;
}
