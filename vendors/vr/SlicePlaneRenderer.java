/*
 *	@(#)SlicePlaneRenderer.java 1.2 99/08/06 13:21:43
 *
 * Copyright (c) 1996-1998 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

package vendors.vr;

import javax.media.j3d.*;
import javax.vecmath.*;

public abstract class SlicePlaneRenderer extends Renderer {

    // Attrs we care about
    CoordAttr		volRefPtAttr;

    // current values derived from ctx
    boolean		border = true;

    Switch		borderSwitch;
    Shape3D		borderShape;

    int			numSlicePts;
    Point3d[]		slicePts = new Point3d[7];
    int[] 		count = new int[1];

    BranchGroup 	root;

    public SlicePlaneRenderer(View view, Context context, Volume vol) {
	super(view, context, vol);
	volRefPtAttr = (CoordAttr) context.getAttr("Vol Ref Pt");

	root = new BranchGroup();

	// subclasses add the slice geometry to root

	borderSwitch = new Switch(Switch.CHILD_ALL);
	borderSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);

	RenderingAttributes ra = new RenderingAttributes();
	ra.setDepthBufferEnable(true);
	ColoringAttributes bclr = new ColoringAttributes(0.4f, 0.4f, 0.4f,
		ColoringAttributes.SHADE_FLAT);
	Appearance ba = new Appearance();
	ba.setColoringAttributes(bclr);
	ba.setRenderingAttributes(ra);

	borderShape = new Shape3D(null, ba);
	borderShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

	borderSwitch.addChild(borderShape);

	root.addChild(borderSwitch);

	root.setCapability(BranchGroup.ALLOW_DETACH);
	root.setCapability(BranchGroup.ALLOW_LOCAL_TO_VWORLD_READ);
    }

    private double planeDist(Point3d pt, Vector4d plane) {
	return pt.x * plane.x + pt.y * plane.y + pt.z * plane.z + plane.w;
    }

    private void exchangePts(Point3d[] edge) {
	Point3d	temp = edge[0];
	edge[0] = edge[1];
	edge[1] = temp;
    }

    private void exchangeEdges(Point3d[] edge0, Point3d[] edge1) {
	Point3d temp0 = edge0[0];
	Point3d temp1 = edge0[1];
	edge0[0] = edge1[0];
	edge0[1] = edge1[1];
	edge1[0] = temp0;
	edge1[1] = temp1;
    }

    /**
     * Intersect the VOI "cube" and a slice plane.  Put the resulting points
     * into slicePts[]
     */
    void calcSlicePts(Vector4d plane) {
	int 		numOutEdges = 0;
	Point3d[][]	outEdges = new Point3d[6][2];
	Point3d		wantVtx;
	Point3d[]	points = volume.voiPts;
	double[]	ptDist = new double[8]; // dist of pt from plane
	int[][] 	edges = { // edges of the cube
			    /* 0  */{ 0, 1},
			    /* 1  */{ 1, 2},
			    /* 2  */{ 2, 3},
			    /* 3  */{ 3, 0},
			    /* 4  */{ 0, 4},
			    /* 5  */{ 1, 5},
			    /* 6  */{ 2, 6},
			    /* 7  */{ 3, 7},
			    /* 8  */{ 4, 5},
			    /* 9  */{ 5, 6},
			    /* 10 */{ 6, 7},
			    /* 11 */{ 7, 4}};
	boolean[] 	edgeFlags = new boolean[12];  // edge cut by plane?
	Point3d[] 	edgeInts = new Point3d[12];   // intersection in edge
	int[][] 	faces = { // edges of faces of cube
			    { 0, 1, 2, 3},
			    { 1, 6, 9, 5},
			    { 6, 2, 7, 10},
			    { 7, 3, 4, 11},
			    { 0, 5, 8, 4},
			    { 9, 8, 10, 11}};

	// determine the distance of each point from the plane
	for (int i = 0; i < 8;  i++) {
	    ptDist[i] = planeDist(points[i], plane);
	}

	// scan each edge, mark the ones that are cut and calc the intersection
	for (int i = 0; i < 12; i++) {
	    double dst0 = ptDist[edges[i][0]];
	    double dst1 = ptDist[edges[i][1]];
	    if ((dst0 > 0) ^ (dst1 > 0)) {
		edgeFlags[i] = true;
		double t = dst1 / (dst1 - dst0);
		edgeInts[i] = new Point3d();
		edgeInts[i].x =  t * points[edges[i][0]].x  +
				(1 - t) * points[edges[i][1]].x;
		edgeInts[i].y = t * points[edges[i][0]].y  +
				(1 - t) * points[edges[i][1]].y;
		edgeInts[i].z = t * points[edges[i][0]].z  +
				(1 - t) * points[edges[i][1]].z;
	    } else {
		edgeFlags[i] = false;
	    }
	}

	// scan each face, if it is cut by the plane, make an edge across
	// the face
	for (int i = 0; i < 6; i++) {
	    boolean anyCut = (edgeFlags[faces[i][0]] | edgeFlags[faces[i][1]] |
		edgeFlags[faces[i][2]] | edgeFlags[faces[i][3]]);
	    if (anyCut) {
		int edgePt = 0;
		if (edgeFlags[faces[i][0]]) {
		    outEdges[numOutEdges][edgePt++] = edgeInts[faces[i][0]];
		}
		if (edgeFlags[faces[i][1]]) {
		    outEdges[numOutEdges][edgePt++] = edgeInts[faces[i][1]];
		}
		if (edgeFlags[faces[i][2]]) {
		    outEdges[numOutEdges][edgePt++] = edgeInts[faces[i][2]];
		}
		if (edgeFlags[faces[i][3]]) {
		    outEdges[numOutEdges][edgePt++] = edgeInts[faces[i][3]];
		}
		numOutEdges++;
	    }
	}

	// sort the edges, matching the endpoints to make a loop
	for (int i = 0; i < numOutEdges; i++ ) {
	    wantVtx = outEdges[i][1];
	    for (int j = i+1; j < numOutEdges; j++) {
	       if ((outEdges[j][0] == wantVtx) ||
		   (outEdges[j][1] == wantVtx)) {
		   if (outEdges[j][1] == wantVtx) {
		      exchangePts(outEdges[j]);
		   }
		   if (j != (i+1)) {
		       exchangeEdges(outEdges[i+1], outEdges[j]);
		   }
	       }
	    }
	}

	numSlicePts = numOutEdges;
	for (int i = 0; i < numOutEdges; i++ ) {
	    slicePts[i] = outEdges[i][0];
	}
	// close the loop for rendering as line
	slicePts[numOutEdges] = slicePts[0];

	/*
	System.out.println("Slice points = ");
	for (int i = 0; i < numOutEdges; i++ ) {
	     System.out.println("\tslicePts[" + i + "] = " + slicePts[i]);
	}
	*/
    }

    // subclasses define the actual slice geometry
    abstract void setSliceGeo();

    void setPlaneGeos() {

	Point3d	eyePt = getViewPosInLocal(root);

	//System.out.println("eypt = " + eyePt);

	// geos is null unless points are valid
	GeometryArray borderGeo = null;

	if (eyePt != null) {
	    Point3d volRefPt = volRefPtAttr.getValue();
	    //System.out.println("volRefPt = " + volRefPt);
	    Vector3d eyeVec = new Vector3d();
	    eyeVec.sub(eyePt, volRefPt);
	    //System.out.println("eyeVec = " + eyeVec);
	    if (eyeVec.length() < 0.001) {
		eyeVec.x = 0.0;
		eyeVec.x = 0.0;
		eyeVec.z = 1.0;
	    }

	    Vector4d slicePlane = new Vector4d();
	    slicePlane.x = eyeVec.x;
	    slicePlane.y = eyeVec.y;
	    slicePlane.z = eyeVec.z;
	    slicePlane.w = -(slicePlane.x * volRefPt.x +
				slicePlane.y * volRefPt.y +
				slicePlane.z * volRefPt.z);

	    //System.out.println("slicePlane = " + slicePlane);

	    calcSlicePts(slicePlane);
	    if (numSlicePts > 0) {
		//System.out.println("slicePts[0] = " + slicePts[0]);
		count[0] = numSlicePts;



		count[0] = numSlicePts + 1;
		borderGeo = new LineStripArray(numSlicePts + 1,
			LineStripArray.COORDINATES, count);
		borderGeo.setCoordinates(0, slicePts, 0, numSlicePts + 1);
	    }
	} else {
	    //System.out.println("eye pt is null");
	}

	// set up the actual slice
	setSliceGeo();

	borderShape.setGeometry(borderGeo);

    }

    public void attach(Group dynamicGroup, Group staticGroup) {
	dynamicGroup.addChild(root);
    }

    abstract public void update();

    public void eyePtChanged() {
	setPlaneGeos();
    }


    /**
     * Returns the number of pixels drawn in the current display
     */
    public double calcRenderSize(ScreenSizeCalculator screenSize,
		Canvas3D canvas) {
	int	rSize = 0;
	double  area = 0.0;

	Node renderNode = root;

	screenSize.setScreenXform(canvas, renderNode);

	// TODO: modify ScreenSizeCalculator to take convex poly instead of
	// quad

	return area;
    }

}
