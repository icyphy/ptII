/* A GR Shape consisting of an extrusion of a polygon base.

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

import javax.media.j3d.GeometryArray;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Stripifier;

///////////////////////////////////////////////////////////////////
//// PolyCylinder3D

/**

 This actor produces a generalized cylindrical shape in the GR domain.
 The output port is used to connect this actor to the Java3D scene
 graph. This actor will only have meaning in the GR domain.

 The parameter <i>polygon</i> determines the polygonal shape of the base
 of this generalized cylinder. One will need to enter an array of coordinates
 in the X-Y plane to specify the polygonal shape for the base. The parameter
 <i>thickness</i> determines the thickness of the generalized cylinder.

 @author C. Fong and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0

 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Yellow (liuxj)
 */
public class PolyCylinder3D extends GRShadedShape {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PolyCylinder3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        polygon = new Parameter(this, "polygon");
        polygon.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        polygon.setExpression("{0.0, 0.5, -0.433, -0.25, 0.433, -0.25}");

        thickness = new Parameter(this, "thickness");
        thickness.setExpression("0.3");
        thickness.setTypeEquals(BaseType.DOUBLE);

        thickness.moveToFirst();
        polygon.moveToFirst();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The polygonal shape of the base
     *  This parameter should contain a ArrayToken with an even number
     *   of DoubleToken values.
     *  The default shape for this polygon is a triangle
     */
    public Parameter polygon;

    /** The thickness of the generalized cylinder
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is the DoubleToken 0.3
     */
    public Parameter thickness;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>polygon</i> or <i>thickness</i>
     *  and runtime changes are allowed, then update the geometry.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the parent class.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (allowRuntimeChanges != null) {
            if ((attribute == polygon || attribute == thickness)
                    && _changesAllowedNow && _containedNode != null) {
                _containedNode.setGeometry(_getGeometry());
            }
        }

        super.attributeChanged(attribute);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the shape and appearance of the encapsulated
     *  generalized cylinder.
     *  @exception IllegalActionException If the value of some
     *   parameters can't be obtained.
     */
    @Override
    protected void _createModel() throws IllegalActionException {
        super._createModel();
        _containedNode = new Shape3D();
        _containedNode.setAppearance(_appearance);
        _containedNode.setGeometry(_getGeometry());

        if (_changesAllowedNow) {
            _containedNode.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        }
    }

    /** Return the encapsulated Java3D node of this 3D actor. The encapsulated
     *  node for this actor is a generalized polygonal cylinder.
     *
     *  @return the generalized polygonal cylinder
     */
    @Override
    protected Node _getNodeObject() {
        return _containedNode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the shape and appearance of the encapsulated
     *  generalized cylinder and return as a geometry array.
     *  @exception IllegalActionException If the value of some
     *   parameters can't be obtained.
     */
    private GeometryArray _getGeometry() throws IllegalActionException {
        float[] data = _getPolygon();
        int numberOfVertices = _getVertexCount();
        int[] stripCount = new int[2 + numberOfVertices];
        stripCount[0] = numberOfVertices;
        stripCount[1] = numberOfVertices;

        for (int i = 2; i < 2 + numberOfVertices; i++) {
            stripCount[i] = 4;
        }

        float thicknessValue = (float) ((DoubleToken) thickness.getToken())
                .doubleValue();

        data[numberOfVertices * 3] = data[0];
        data[numberOfVertices * 3 + 1] = data[1];
        data[numberOfVertices * 3 + 2] = data[2] - thicknessValue;

        int j = numberOfVertices * 3 - 3;
        int k = numberOfVertices * 3 + 3;

        for (int i = 1; i < numberOfVertices; i++) {
            data[k] = data[j];
            data[k + 1] = data[j + 1];
            data[k + 2] = data[j + 2] - thicknessValue;
            j = j - 3;
            k = k + 3;
        }

        j = 0;
        k = 2 * 3 * numberOfVertices;

        for (int i = 0; i < numberOfVertices; i++) {
            data[k] = data[j];
            data[k + 1] = data[j + 1];
            data[k + 2] = data[j + 2];

            data[k + 3] = data[j];
            data[k + 4] = data[j + 1];
            data[k + 5] = data[j + 2] - thicknessValue;

            j = j + 3;

            if (j == numberOfVertices * 3) {
                j = 0;
            }

            data[k + 6] = data[j];
            data[k + 7] = data[j + 1];
            data[k + 8] = data[j + 2] - thicknessValue;

            data[k + 9] = data[j];
            data[k + 10] = data[j + 1];
            data[k + 11] = data[j + 2];
            k = k + 12;
        }

        GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        geometryInfo.setCoordinates(data);
        geometryInfo.setStripCounts(stripCount);

        // The Triangulator constructor was deprecated.  The javadocs say:
        // "This class [Triangulator] is created automatically when needed
        // in GeometryInfo and never needs to be used directly. Putting data
        // into a GeometryInfo with primitive POLYGON_ARRAY automatically
        // causes the triangulator to be created and used."
        //Triangulator triangulator = new Triangulator();
        //triangulator.triangulate(geometryInfo);
        //geometryInfo.recomputeIndices();

        NormalGenerator normalGenerator = new NormalGenerator();
        normalGenerator.generateNormals(geometryInfo);
        geometryInfo.recomputeIndices();

        Stripifier st = new Stripifier();
        st.stripify(geometryInfo);
        geometryInfo.recomputeIndices();
        return geometryInfo.getGeometryArray();
    }

    /** Get the array that contains the 2D polygonal representation
     *  of the base of this cylinder.
     *  @return An array that contains the 2D polygonal vertex coordinates.
     *  @exception IllegalActionException If the value of the <i>polygon</i>
     *   parameter can't be obtained.
     */
    private float[] _getPolygon() throws IllegalActionException {
        ArrayToken polygonToken = (ArrayToken) polygon.getToken();

        int numberOfElements = polygonToken.length() / 2;

        // FIXME: What is this naked constant 18?
        // It seems the array gets filled with other things
        // after being returned.
        float[] data = new float[numberOfElements * 18];

        int j = 0;

        for (int i = 0; i < numberOfElements * 2; i = i + 2) {
            data[j++] = (float) ((DoubleToken) polygonToken.getElement(i))
                    .doubleValue();
            data[j++] = (float) ((DoubleToken) polygonToken.getElement(i + 1))
                    .doubleValue();
            data[j++] = 0.0f;
        }

        return data;
    }

    /** Get the number of vertices in the 2D polygonal base of this
     *  generalized cylinder.
     *  @return the number of vertices in the base polygon
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private int _getVertexCount() throws IllegalActionException {
        ArrayToken polygonToken = (ArrayToken) polygon.getToken();
        return polygonToken.length() / 2;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The contained shape. */
    private Shape3D _containedNode;
}
