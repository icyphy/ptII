/* A GR Shape consisting of a cone.

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
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.Primitive;

//////////////////////////////////////////////////////////////////////////
//// Cone3D

/** This actor contains the geometry and appearance specifications for a
    cone.  The output port is used to connect this actor to the Java3D scene
    graph. This actor will only have meaning in the GR domain.

    @author Chamberlain Fong, Edward A. Lee
    @version $Id$
    @since Ptolemy II 1.0
    @Pt.ProposedRating Green (eal)
    @Pt.AcceptedRating Green (liuxj)
*/
public class Cone3D extends GRShadedShape {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Cone3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        radius = new Parameter(this, "radius");
        radius.setExpression("0.5");
        radius.setTypeEquals(BaseType.DOUBLE);

        height = new Parameter(this, "height");
        height.setExpression("0.7");
        height.setTypeEquals(BaseType.DOUBLE);

        height.moveToFirst();
        radius.moveToFirst();

        circleDivisions = new Parameter(this, "circleDivisions");
        circleDivisions.setExpression("max(6, roundToInt(radius * 100))");
        circleDivisions.setTypeEquals(BaseType.INT);

        sideDivisions = new Parameter(this, "sideDivisions");
        sideDivisions.setExpression("1");
        sideDivisions.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of divisions in the circle forming the base of the
     *  cone. This is an integer with
     *  default value "max(6, roundToInt(radius * 100))". This parameter
     *  determines the resolution of the cone, which is approximated
     *  as a surface composed of triangular facets. Increasing this
     *  value makes the surface smoother, but also increases the cost
     *  of rendering.
     */
    public Parameter circleDivisions;

    /** The height of the cone. This is a double that defaults to 0.5.
     */
    public Parameter height;

    /** The number of divisions on the side of the cone.
     *  This is an integer with default value "1". This parameter
     *  probably only needs to change when the <i>wireFrame</i>
     *  parameter is set to true.
     */
    public Parameter sideDivisions;

    /** The radius of the base of the cone. This is a double that
     *  defaults to 0.7.
     */
    public Parameter radius;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the shape and appearance of the cone.
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

        int circleDivisionsValue
                = ((IntToken)circleDivisions.getToken()).intValue();
        int sideDivisionsValue
                = ((IntToken)sideDivisions.getToken()).intValue();
        
        float heightValue 
                = (float)((DoubleToken) height.getToken()).doubleValue();
        float radiusValue
                = (float)((DoubleToken) radius.getToken()).doubleValue();
        
        _containedNode = new Cone(radiusValue, heightValue,
                primitiveFlags, circleDivisionsValue,
                sideDivisionsValue, _appearance);
    }

    /** Return the cone.
     *  @return The Java3D cone.
     */
    protected Node _getNodeObject() {
        return (Node) _containedNode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The cone. */
    private Cone _containedNode;
}
