/* A GR Shape consisting of a circularly-swept surface

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

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.*;
import ptolemy.actor.lib.*;
import ptolemy.domains.dt.kernel.DTDebug;


import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;

//////////////////////////////////////////////////////////////////////////
//// CircularSweep3D
/** This actor contains the geometry and appearance specifications for a GR
circularly swept object.  The output port is used to connect this actor to the
Java3D scene graph. This actor will only have meaning in the GR domain.

    The parameter <i>polyline</i> determines the silhouette of the object. One
will need to enter an array of coordinates in the X-Y plane to specify the
contour for this silhouette.  The parameter <i>angleSpan</i> determines the
angle in which the silhouette is swept.  The parameter <i>slices</i> determines
the number of polygonal slices used in the sweep.


@author C. Fong
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
        DoubleToken[] tokenArray = new DoubleToken[10];
        tokenArray[0] = new DoubleToken(0.5);
        tokenArray[1] = new DoubleToken(0.25);
        tokenArray[2] = new DoubleToken(0.5);
        tokenArray[3] = new DoubleToken(-0.25);
        tokenArray[4] = new DoubleToken(0.25);
        tokenArray[5] = new DoubleToken(-0.25);
        tokenArray[6] = new DoubleToken(0.25);
        tokenArray[7] = new DoubleToken(0.25);
        tokenArray[8] = new DoubleToken(0.5);
        tokenArray[9] = new DoubleToken(0.25);
      	ArrayToken defaultPolygon = new ArrayToken(tokenArray);

        polyline = new Parameter(this, "polyline", defaultPolygon);
        polyline.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
        angleSpan = new Parameter(this, "angleSpan", new DoubleToken(2*Math.PI));
        slices = new Parameter(this, "slices", new IntToken(32));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The line segment array that is to be swept
     *  This parameter should contain a DoubleToken.
     */
    public Parameter polyline;

    /** The span of sweep angle
     *  This parameter should contain a DoubleToken.
     */
    public Parameter angleSpan;

    /** The number of slices
     *  This parameter should contain a IntToken.
     */
    public Parameter slices;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the encapsulated Java3D node of this 3D actor. The encapsulated
     *  node for this actor is a circular sweep.
     *  @return the Java3D circular sweep
     */
    public Node getNodeObject() {
        return (Node) containedNode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the shape and appearance of the encapsulated swept surface
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    protected void _createModel() throws IllegalActionException {

        super._createModel();
        int numberOfSlices = _getSlices();
        float data[] = _getPolyline();
        int numberOfSweepVertices = _getVertexCount();
        int numberOfQuads = (numberOfSweepVertices -1 ) * numberOfSlices;
        int totalVertices = numberOfQuads * 4;
        float[] polydata = new float[totalVertices * 3];
        double span = _getAngleSpan();

        int[] stripCount = new int[numberOfQuads];
        int i;
        for(i=0; i < numberOfQuads; i++) {
            stripCount[i] = 4;
        }

        int j;
        int k=0;
        int m=0;
        for(i=0; i<numberOfSweepVertices-1;i++) {
            for(j=0; j<numberOfSlices; j++) {
                float cosFactor1 = (float) Math.cos(span*j/numberOfSlices);
                float sinFactor1 = (float) Math.sin(span*j/numberOfSlices);
                float cosFactor2 = (float) Math.cos(span*(j+1)/numberOfSlices);
                float sinFactor2 = (float) Math.sin(span*(j+1)/numberOfSlices);


                polydata[k]   = data[m] * cosFactor1;
                polydata[k+1] = data[m+1];
                polydata[k+2] = data[m] * sinFactor1;
                k = k + 3;
                polydata[k]   = data[m] * cosFactor2;
                polydata[k+1] = data[m+1];
                polydata[k+2] = data[m] * sinFactor2;
                k = k + 3;
                polydata[k]   = data[m+2] * cosFactor2;
                polydata[k+1] = data[m+3];
                polydata[k+2] = data[m+2] * sinFactor2;
                k = k + 3;
                polydata[k]   = data[m+2] * cosFactor1;
                polydata[k+1] = data[m+3];
                polydata[k+2] = data[m+2] * sinFactor1;
                k = k + 3;
            }
            m = m + 2;
        }


        GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        gi.setCoordinates(polydata);
        gi.setStripCounts(stripCount);

        Triangulator tr = new Triangulator();
        tr.triangulate(gi);
        gi.recomputeIndices();

        NormalGenerator ng = new NormalGenerator();
        ng.generateNormals(gi);
        gi.recomputeIndices();

        Stripifier st = new Stripifier();
        st.stripify(gi);
        gi.recomputeIndices();

        containedNode = new Shape3D();
        containedNode.setAppearance(_appearance);
        containedNode.setGeometry(gi.getGeometryArray());
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the polyline
     *  @return the polyline
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private float[] _getPolyline() throws IllegalActionException {
        ArrayToken arrayToken = ((ArrayToken) polyline.getToken());
        int numberOfElements = arrayToken.length()/2;
        float[] data = new float[numberOfElements*2];

        for(int i=0 ;i < numberOfElements * 2; i++) {
            data[i] = (float) ((DoubleToken)arrayToken.getElement(i)).doubleValue();
        }

        return data;
    }

    /** Return the vertex count
     *  @return the vertex count
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private int _getVertexCount() throws IllegalActionException {
        ArrayToken arrayToken = ((ArrayToken) polyline.getToken());
        int numberOfElements = arrayToken.length()/2;

        return numberOfElements;
    }

    /** Return the number of slices
     *  @return the number of slices
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private int _getSlices() throws IllegalActionException {
        return ((IntToken) slices.getToken()).intValue();
    }

    /** Return the angle span of the sweep
     *  @return the angle span of the sweep
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private double _getAngleSpan() throws IllegalActionException  {
        return ((DoubleToken) angleSpan.getToken()).doubleValue();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Shape3D containedNode;
}
