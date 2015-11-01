/* A GR Shape consisting of a circularly-swept surface.

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
import javax.media.j3d.Shape3D;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Stripifier;

///////////////////////////////////////////////////////////////////
//// CircularSweep3D

/** This actor contains the geometry and appearance specifications for a
 circularly swept object.  The output port is used to connect this
 actor to the Java3D scene graph. This actor will only have meaning
 in the GR domain.

 <p>The parameter <i>polyline</i> determines the silhouette of the
 object. One will need to enter an array of coordinates in the X-Y
 plane to specify the contour for this silhouette.  The parameter
 <i>angleSpan</i> determines the angle in which the silhouette is
 swept.  The parameter <i>slices</i> determines the number of
 polygonal slices used in the sweep.

 @author C. Fong (contributor: Edward A. Lee)
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (chf)
 @Pt.AcceptedRating Red (chf)
 */
public class CircularSweep3D extends GRShadedShape {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CircularSweep3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        polyline = new Parameter(this, "polyline");
        polyline.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        polyline.setExpression("{0.5, 0.25, 0.5, -0.25, 0.25, -0.25, 0.25, 0.25, 0.5, 0.25}");

        angleSpan = new Parameter(this, "angleSpan");
        angleSpan.setTypeEquals(BaseType.DOUBLE);
        angleSpan.setExpression("2.0 * PI");

        slices = new Parameter(this, "slices", new IntToken(32));
        slices.setTypeEquals(BaseType.INT);

        slices.moveToFirst();
        angleSpan.moveToFirst();
        polyline.moveToFirst();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The line segment array that is to be swept.
     *  This is an array of doubles that defaults to
     *  a square with sides of 0.25 shifted right from
     *  the origin so its left edge is at x = 0.25.
     *  This results in an annular sweep.
     */
    public Parameter polyline;

    /** The span of sweep angle. This is a double that
     *  defaults to "2.0 * PI".
     */
    public Parameter angleSpan;

    /** The number of slices. This is an integer
     *  that defaults to 32. Higher numbers result
     *  in a smoother sweep.
     */
    public Parameter slices;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the shape and appearance of the encapsulated swept surface.
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained.
     */
    @Override
    protected void _createModel() throws IllegalActionException {
        super._createModel();

        int numberOfSlices = ((IntToken) slices.getToken()).intValue();
        float[] data = _getPolyline();
        int numberOfSweepVertices = _getVertexCount();
        int numberOfQuads = (numberOfSweepVertices - 1) * numberOfSlices;
        int totalVertices = numberOfQuads * 4;
        float[] polydata = new float[totalVertices * 3];
        double span = ((DoubleToken) angleSpan.getToken()).doubleValue();

        int[] stripCount = new int[numberOfQuads];
        int i;

        for (i = 0; i < numberOfQuads; i++) {
            stripCount[i] = 4;
        }

        int j;
        int k = 0;
        int m = 0;

        for (i = 0; i < numberOfSweepVertices - 1; i++) {
            for (j = 0; j < numberOfSlices; j++) {
                float cosFactor1 = (float) Math.cos(span * j / numberOfSlices);
                float sinFactor1 = (float) Math.sin(span * j / numberOfSlices);
                float cosFactor2 = (float) Math.cos(span * (j + 1)
                        / numberOfSlices);
                float sinFactor2 = (float) Math.sin(span * (j + 1)
                        / numberOfSlices);

                polydata[k] = data[m] * cosFactor1;
                polydata[k + 1] = data[m + 1];
                polydata[k + 2] = data[m] * sinFactor1;
                k = k + 3;
                polydata[k] = data[m] * cosFactor2;
                polydata[k + 1] = data[m + 1];
                polydata[k + 2] = data[m] * sinFactor2;
                k = k + 3;
                polydata[k] = data[m + 2] * cosFactor2;
                polydata[k + 1] = data[m + 3];
                polydata[k + 2] = data[m + 2] * sinFactor2;
                k = k + 3;
                polydata[k] = data[m + 2] * cosFactor1;
                polydata[k + 1] = data[m + 3];
                polydata[k + 2] = data[m + 2] * sinFactor1;
                k = k + 3;
            }

            m = m + 2;
        }

        GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        gi.setCoordinates(polydata);
        gi.setStripCounts(stripCount);

        // The Triangulator constructor was deprecated.  The javadocs say:
        // "This class [Triangulator] is created automatically when needed
        // in GeometryInfo and never needs to be used directly. Putting data
        // into a GeometryInfo with primitive POLYGON_ARRAY automatically
        // causes the triangulator to be created and used."
        //Triangulator tr = new Triangulator();
        //tr.triangulate(gi);
        //gi.recomputeIndices();

        NormalGenerator ng = new NormalGenerator();
        ng.generateNormals(gi);
        gi.recomputeIndices();

        Stripifier st = new Stripifier();
        st.stripify(gi);
        gi.recomputeIndices();

        _containedNode = new Shape3D();
        _containedNode.setAppearance(_appearance);
        _containedNode.setGeometry(gi.getGeometryArray());
    }

    /** Return the encapsulated Java3D node of this 3D actor. The encapsulated
     *  node for this actor is a circular sweep.
     *  @return the Java3D circular sweep
     */
    @Override
    protected Node _getNodeObject() {
        return _containedNode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the polyline as an array of floats.
     *  @return The polyline.
     *  @exception IllegalActionException If polyline parameter
     *   cannot be evaluated.
     */
    private float[] _getPolyline() throws IllegalActionException {
        ArrayToken token = (ArrayToken) polyline.getToken();
        int numberOfElements = token.length();
        float[] data = new float[numberOfElements];

        for (int i = 0; i < numberOfElements; i++) {
            data[i] = (float) ((DoubleToken) token.getElement(i)).doubleValue();
        }

        return data;
    }

    /** Return the number of vertices of the polyline.
     *  @return The vertex count.
     *  @exception IllegalActionException If the polyline parameter
     *   cannot be evaluated.
     */
    private int _getVertexCount() throws IllegalActionException {
        ArrayToken token = (ArrayToken) polyline.getToken();
        return token.length() / 2;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The contained node. */
    private Shape3D _containedNode;
}
