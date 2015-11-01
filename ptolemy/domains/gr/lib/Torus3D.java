/* A GR Shape consisting of a torus

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

import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Stripifier;

///////////////////////////////////////////////////////////////////
//// Torus3D

/** This actor contains the geometry and appearance specifications for
 a GR torus.  The output port is used to connect this actor to the
 Java3D scene graph. This actor will only have meaning in the GR
 domain.  The parameter <i>hullRadius</i> determines the radius of
 torus ring. The parameter <i>crossSectionRadius</i> determines the
 radius of the torus cross-section.  The parameter <i>slices</i>
 determines the number of polygonal slices used in the creating the
 torus.

 @author C. Fong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (chf)
 @Pt.AcceptedRating Red (chf)
 */
public class Torus3D extends GRShadedShape {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Torus3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        angleSpan = new Parameter(this, "angleSpan", new DoubleToken(
                2 * Math.PI));
        slices = new Parameter(this, "slices", new IntToken(28));
        crossSectionRadius = new Parameter(this, "crossSectionRadius",
                new DoubleToken(0.15));
        hullRadius = new Parameter(this, "hullRadius", new DoubleToken(0.75));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The radius of the torus cross-section
     *  This parameter should contain a DoubleToken.
     */
    public Parameter crossSectionRadius;

    /** The radius of the torus outer hull
     *  This parameter should contain a DoubleToken.
     */
    public Parameter hullRadius;

    /** The span of torus sweep angle
     *  This parameter should contain a DoubleToken.
     */
    public Parameter angleSpan;

    /** The number of slices
     *  This parameter should contain a IntToken.
     */
    public Parameter slices;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the shape and appearance of the encapsulated torus.
     *  @exception IllegalActionException If the value of some parameters can't
     *  be obtained.
     */
    @Override
    protected void _createModel() throws IllegalActionException {
        super._createModel();

        int numberOfSlices = _getSlices();
        float[] data = new float[numberOfSlices * 2];
        int numberOfSweepVertices = numberOfSlices;
        int numberOfQuads = (numberOfSweepVertices - 1) * numberOfSlices;
        int totalVertices = numberOfQuads * 4;
        float[] polydata = new float[totalVertices * 3];
        double span = _getAngleSpan();
        float innerRadius = _getCrossSectionRadius();
        float outerRadius = _getHullRadius();

        int[] stripCount = new int[numberOfQuads];

        int i;
        int j;
        int k;
        int m;

        for (i = 0; i < numberOfQuads; i++) {
            stripCount[i] = 4;
        }

        j = 0;

        for (i = 0; i < numberOfSlices; i++) {
            double theta = Math.PI - 2 * Math.PI * i / (numberOfSlices - 1);
            data[j++] = outerRadius + innerRadius * (float) Math.cos(theta);
            data[j++] = innerRadius * (float) Math.sin(theta);
        }

        k = m = 0;

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
     *  node for this actor is a customized torus.
     *
     *  @return the torus node
     */
    @Override
    protected Node _getNodeObject() {
        return _containedNode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the angle span of the sweep
     *  @return the angle span of the sweep
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private double _getAngleSpan() throws IllegalActionException {
        return ((DoubleToken) angleSpan.getToken()).doubleValue();
    }

    /** Return the radius of the cross section
     *  @return the radius of the cross section
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private float _getCrossSectionRadius() throws IllegalActionException {
        return (float) ((DoubleToken) crossSectionRadius.getToken())
                .doubleValue();
    }

    /** Return the radius of the outer hull
     *  @return the radius of the outer hull
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private float _getHullRadius() throws IllegalActionException {
        return (float) ((DoubleToken) hullRadius.getToken()).doubleValue();
    }

    /** Return the number of slices
     *  @return the number of slices
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private int _getSlices() throws IllegalActionException {
        return ((IntToken) slices.getToken()).intValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Shape3D _containedNode;
}
