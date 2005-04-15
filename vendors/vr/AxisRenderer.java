/*
 *	%Z%%M% %I% %E% %U%
 *
 * Copyright (c) 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
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

import java.awt.*;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.io.*;
import com.sun.j3d.utils.behaviors.mouse.*;

abstract public class AxisRenderer extends Renderer 
				implements VolRendConstants{

    // Attrs we care about
    CoordAttr		volRefPtAttr;
    // debug attrs;
    ChoiceAttr		displayAxisAttr;
    ToggleAttr		dbWriteAttr;

    // current settings derived from attrs
    boolean		dbWriteEnable = true;
    int 		displayAxisMode = 0; // auto

    boolean		fullReloadNeeded = true;
    boolean		tctReloadNeeded = true;

    int 		curAxis = Z_AXIS;
    int			curDir = FRONT;
    boolean		autoAxisEnable = true;
    int			autoAxis, autoDir;

    public BranchGroup 	root;
    Switch 		axisSwitch;
    int[][] 		axisIndex = new int[3][2];
    OrderedGroup[][] 	groups = new OrderedGroup[3][2];

    double[] 		quadCoords;

    String[] 		axisStrings = new String[3];
    String[] 		dirStrings = new String[2];

    TextureAttributes 	texAttr = new TextureAttributes();
    TransparencyAttributes t = new TransparencyAttributes();
    PolygonAttributes 	p = new PolygonAttributes();
    Material 		m = new Material();


    public AxisRenderer(View view, Context context, Volume vol) {
	super(view, context, vol);
	volRefPtAttr = (CoordAttr) context.getAttr("Vol Ref Pt");
	if (debug) {
	    displayAxisAttr = (ChoiceAttr) context.getAttr("Display Axis");
	    dbWriteAttr = (ToggleAttr) context.getAttr("Depth Write");
	}

	dirStrings[FRONT] = "+";
	dirStrings[BACK] = "-";
	axisStrings[X_AXIS] = "X";
	axisStrings[Y_AXIS] = "Y";
	axisStrings[Z_AXIS] = "Z";
	axisIndex[X_AXIS][FRONT] = 0;
	axisIndex[X_AXIS][BACK] = 1;
	axisIndex[Y_AXIS][FRONT] = 2;
	axisIndex[Y_AXIS][BACK] = 3;
	axisIndex[Z_AXIS][FRONT] = 4;
	axisIndex[Z_AXIS][BACK] = 5;

        axisSwitch = new Switch();
        axisSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
        axisSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
        axisSwitch.setCapability(Group.ALLOW_CHILDREN_READ);
        axisSwitch.setCapability(Group.ALLOW_CHILDREN_WRITE);
        axisSwitch.addChild(getOrderedGroup());
        axisSwitch.addChild(getOrderedGroup());
        axisSwitch.addChild(getOrderedGroup());
        axisSwitch.addChild(getOrderedGroup());
        axisSwitch.addChild(getOrderedGroup());
        axisSwitch.addChild(getOrderedGroup());

	//texAttr.setTextureMode(TextureAttributes.MODULATE);
	texAttr.setTextureMode(TextureAttributes.REPLACE);
	texAttr.setCapability(TextureAttributes.ALLOW_COLOR_TABLE_WRITE);
	t.setTransparencyMode(TransparencyAttributes.BLENDED);
	m.setLightingEnable(false);
	p.setCullFace(PolygonAttributes.CULL_NONE);

	root = new BranchGroup();
	root.addChild(axisSwitch);
	root.setCapability(BranchGroup.ALLOW_DETACH);
	root.setCapability(BranchGroup.ALLOW_LOCAL_TO_VWORLD_READ);
    }

    public void attach(Group dynamicGroup, Group staticGroup) {
	dynamicGroup.addChild(root);
    }

    void updateDebugAttrs() {
	if (debug) {
	    int mode = displayAxisAttr.getValue();
	    if (mode != displayAxisMode) {
		displayAxisMode = mode;
                switch (mode) {
		  case 0:
		    setAutoAxisEnable(true);
		    break;
		  case 1:
		    setAutoAxisEnable(false);
		    setAxis(X_AXIS, BACK);
		    break;
		  case 2:
		    setAutoAxisEnable(false);
		    setAxis(X_AXIS, FRONT);
		    break;
		  case 3:
		    setAutoAxisEnable(false);
		    setAxis(Y_AXIS, BACK);
		    break;
		  case 4:
		    setAutoAxisEnable(false);
		    setAxis(Y_AXIS, FRONT);
		    break;
		  case 5:
		    setAutoAxisEnable(false);
		    setAxis(Z_AXIS, BACK);
		    break;
		  case 6:
		    setAutoAxisEnable(false);
		    setAxis(Z_AXIS, FRONT);
		    break;
		  default:
		    System.out.println("Unexpected display axis: " + mode);
		}
	    }
	    if (dbWriteAttr.getValue() != dbWriteEnable) {
		dbWriteEnable = dbWriteAttr.getValue();
		fullReloadNeeded = true;
	    }
	}
    }

    protected void clearData() {
	Group group;
	clearGroup(axisSwitch.getChild(axisIndex[Z_AXIS][FRONT]));
	clearGroup(axisSwitch.getChild(axisIndex[Z_AXIS][BACK]));
	clearGroup(axisSwitch.getChild(axisIndex[Y_AXIS][FRONT]));
	clearGroup(axisSwitch.getChild(axisIndex[Y_AXIS][BACK]));
	clearGroup(axisSwitch.getChild(axisIndex[X_AXIS][FRONT]));
	clearGroup(axisSwitch.getChild(axisIndex[X_AXIS][BACK]));
    }

    protected void clearGroup(Node node) {
	Group group = (Group) node;
	int numChildren = group.numChildren();
	for (int i = numChildren-1; i >= 0; i--) {
	    group.removeChild(i);
	}
	if ((numChildren = group.numChildren()) > 0) {
	    System.out.println("clearGroup(): still got a kid");
	}
    }

    protected void setCurCoordX(int i) {
	double curX = i * volume.xSpace;
	quadCoords[0] = curX;
	quadCoords[3] = curX;
	quadCoords[6] = curX;
	quadCoords[9] = curX;
    }

    protected void setCurCoordY(int i) {
	double curY = i * volume.ySpace;
	quadCoords[1] = curY;
	quadCoords[4] = curY;
	quadCoords[7] = curY;
	quadCoords[10] = curY;
    }

    protected void setCurCoordZ(int i) {
	double curZ = i * volume.zSpace;
	quadCoords[2] = curZ;
	quadCoords[5] = curZ;
	quadCoords[8] = curZ;
	quadCoords[11] = curZ;
    }

    private void checkCoords() {
	if ((quadCoords == null) || (quadCoords.length != 12)) {
	    quadCoords = new double[12];
	}
    }

    protected void setCoordsX() {
	checkCoords();

	// lower left
	quadCoords[1] = volume.minCoord.y;
	quadCoords[2] = volume.minCoord.z;
	// lower right
	quadCoords[4] = volume.maxCoord.y;
	quadCoords[5] = volume.minCoord.z;
	// upper right
	quadCoords[7] = volume.maxCoord.y;
	quadCoords[8] = volume.maxCoord.z;
	// upper left
	quadCoords[10] = volume.minCoord.y;
	quadCoords[11] = volume.maxCoord.z;
    }

    protected void setCoordsY() {
	checkCoords();

	// lower left
	quadCoords[0] = volume.minCoord.x;
	quadCoords[2] = volume.minCoord.z;
	// lower right
	quadCoords[3] = volume.minCoord.x;
	quadCoords[5] = volume.maxCoord.z;
	// upper right
	quadCoords[6] = volume.maxCoord.x;
	quadCoords[8] = volume.maxCoord.z;
	// upper left
	quadCoords[9] = volume.maxCoord.x;
	quadCoords[11] = volume.minCoord.z;
    }

    protected void setCoordsZ() {
	checkCoords();

	// lower left
	quadCoords[0] = volume.minCoord.x;
	quadCoords[1] = volume.minCoord.y;
	// lower right
	quadCoords[3] = volume.maxCoord.x;
	quadCoords[4] = volume.minCoord.y;
	// upper right
	quadCoords[6] = volume.maxCoord.x;
	quadCoords[7] = volume.maxCoord.y;
	// upper left
	quadCoords[9] = volume.minCoord.x;
	quadCoords[10] = volume.maxCoord.y;
    }

    protected OrderedGroup getOrderedGroup() {
	OrderedGroup og = new OrderedGroup();
	og.setCapability(Group.ALLOW_CHILDREN_READ);
	og.setCapability(Group.ALLOW_CHILDREN_WRITE);
	og.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	return og;
    }

    public void eyePtChanged() {

	Point3d eyePt = getViewPosInLocal(root);

	//System.out.println("eye pt = " + eyePt);

	if (eyePt != null) {
	    Point3d  volRefPt = volRefPtAttr.getValue();
	    Vector3d eyeVec = new Vector3d();
	    eyeVec.sub(eyePt, volRefPt);

	    //System.out.println("eye vec = " + eyeVec);

	    // compensate for different xyz resolution/scale
	    eyeVec.x /= volume.xSpace;
	    eyeVec.y /= volume.ySpace;
	    eyeVec.z /= volume.zSpace;

	    // select the axis with the greatest magnitude 
	    int axis = X_AXIS;
	    double value = eyeVec.x;
	    double max = Math.abs(eyeVec.x);
	    if (Math.abs(eyeVec.y) > max) {
		axis = Y_AXIS;
		value = eyeVec.y;
		max = Math.abs(eyeVec.y);
	    }
	    if (Math.abs(eyeVec.z) > max) {
		axis = Z_AXIS;
		value = eyeVec.z;
		max = Math.abs(eyeVec.z);
	    }

	    // select the direction based on the sign of the magnitude
	    int dir;
	    if (value > 0.0) {
		dir = FRONT;
	    } else {
		dir = BACK;
	    }


	    //System.out.println("Selected axis: " + dirStrings[dir] + 
	    //    axisStrings[axis]);

	    if ((axis != autoAxis) || (dir != autoDir)) {
		autoAxis = axis;
		autoDir = dir;
		if (autoAxisEnable) {
		    autoSetAxis();
		} else {
		    System.out.println("Auto axis is " + dirStrings[dir] + 
			axisStrings[axis] + " current is " + dirStrings[curDir]
				+ axisStrings[curAxis]);
		}
	    }
	}
    }

    private void setAutoAxisEnable(boolean flag) {
	if (autoAxisEnable != flag) {
	    autoAxisEnable = flag;
	    if (autoAxisEnable) {
		autoSetAxis();
	    }
	}
    }

    private void autoSetAxis() {
	setAxis(autoAxis, autoDir);
    }

    private void setAxis(int axis, int dir) {
	curAxis = axis;
	curDir = dir;
	setWhichChild();
    }

    /**
     * Returns the number of pixels drawn in the current display
     */
    public double calcRenderSize(ScreenSizeCalculator screenSize, 
		Canvas3D canvas) {
	int	rSize = 0;
	double 	area = 0.0;

	Node renderNode = root;

	screenSize.setScreenXform(canvas, renderNode);
	switch (curAxis) {
	  case Z_AXIS:
	    rSize = volume.zDim;
	    setCoordsZ();
	    break;
	  case Y_AXIS:
	    rSize = volume.yDim;
	    setCoordsY();
	    break;
	  case X_AXIS:
	    rSize = volume.xDim;
	    setCoordsX();
	    break;
	}
	for (int j=0; j < rSize; j ++) { 
	    switch (curAxis) {
	      case Z_AXIS:
		setCurCoordZ(j);
		break;
	      case Y_AXIS:
		setCurCoordY(j);
		break;
	      case X_AXIS:
		setCurCoordX(j);
		break;
	    }
	    area += screenSize.quadScreenSize(quadCoords);

	}
	return area;
    }

    protected void setWhichChild() {
	if (debug) {
	    System.out.println("Displaying axis: " + dirStrings[curDir] + 
		axisStrings[curAxis]);
	}
	axisSwitch.setWhichChild(axisIndex[curAxis][curDir]);
    }
}
