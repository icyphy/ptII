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

public class Axis2DRenderer extends AxisRenderer {

    Texture2DVolume	texVol;
    public Shape3D frontShape;


    public Axis2DRenderer(View view, Context context, Volume vol) {
	super(view, context, vol);
	texVol = new Texture2DVolume(context, vol);
    }

    public void update() {
        int	texVolUpdate = texVol.update();
	switch (texVolUpdate) {
	  case Texture2DVolume.RELOAD_NONE:
	    fullReloadNeeded = false;
	    tctReloadNeeded = false;
	    break;
	  case Texture2DVolume.RELOAD_VOLUME:
	    fullReloadNeeded = true;
	    tctReloadNeeded = false;
	    break;
	  case Texture2DVolume.RELOAD_CMAP:
	    fullReloadNeeded = false;
	    tctReloadNeeded = true;
	    break;
	}

	updateDebugAttrs();

	if (fullReloadNeeded) {
	    fullReload();
	}
	if (tctReloadNeeded) {
	    tctReload();
	}
    }

    void fullReload() {
	clearData();

	if (volume.hasData()) {
	    loadQuads();
	    tctReload();
	}

	setWhichChild();

	fullReloadNeeded = false;
    }

    void tctReload() {
	if (texVol.useTextureColorTable) {
	     texAttr.setTextureColorTable(texVol.texColorMap);
	} else {
	     texAttr.setTextureColorTable(null);
	}
	tctReloadNeeded = false;
    }

    void loadQuads() {
	loadAxis(Z_AXIS);
	loadAxis(Y_AXIS);
	loadAxis(X_AXIS);
    }

    void loadAxis(int axis) {
	int	rSize = 0;        // number of tex maps to create
	OrderedGroup frontGroup = null;
	OrderedGroup backGroup = null;
	Texture2D[] textures = null;
	TexCoordGeneration tg = null;

	switch (axis) {
	  case Z_AXIS:
	    frontGroup = 
		(OrderedGroup)axisSwitch.getChild(axisIndex[Z_AXIS][FRONT]);
	    backGroup = 
		(OrderedGroup)axisSwitch.getChild(axisIndex[Z_AXIS][BACK]);
	    rSize = volume.zDim;
	    textures = texVol.zTextures;
	    tg = texVol.zTg;
	    setCoordsZ();
	    break;
	  case Y_AXIS:
	    frontGroup = 
		(OrderedGroup)axisSwitch.getChild(axisIndex[Y_AXIS][FRONT]);
	    backGroup = 
		(OrderedGroup)axisSwitch.getChild(axisIndex[Y_AXIS][BACK]);
	    rSize = volume.yDim;
	    textures = texVol.yTextures;
	    tg = texVol.yTg;
	    setCoordsY();
	    break;
	  case X_AXIS:
	    frontGroup = 
		(OrderedGroup)axisSwitch.getChild(axisIndex[X_AXIS][FRONT]);
	    backGroup = 
		(OrderedGroup)axisSwitch.getChild(axisIndex[X_AXIS][BACK]);
	    rSize = volume.xDim;
	    textures = texVol.xTextures;
	    tg = texVol.xTg;
	    setCoordsX();
	    break;
	}

	for (int i=0; i < rSize; i ++) { 

	    switch (axis) {
	      case Z_AXIS:
		setCurCoordZ(i);
		break;
	      case Y_AXIS:
		setCurCoordY(i);
		break;
	      case X_AXIS:
		setCurCoordX(i);
		break;
	    }

	    Texture2D tex = textures[i];

	    Appearance a = new Appearance();
	    a.setMaterial(m);
	    a.setTransparencyAttributes(t);
	    a.setTexture(tex);
	    a.setTextureAttributes(texAttr);
	    a.setTexCoordGeneration(tg);
	    if (dbWriteEnable == false) {
		RenderingAttributes r = new RenderingAttributes();
		r.setDepthBufferWriteEnable(dbWriteEnable);
		a.setRenderingAttributes(r);
	    }
	    a.setPolygonAttributes(p);

	    QuadArray quadArray = new QuadArray(4, GeometryArray.COORDINATES);
	    quadArray.setCoordinates(0, quadCoords);

	    frontShape = new Shape3D(quadArray, a);
	    

	    BranchGroup frontShapeGroup = new BranchGroup();
	    frontShapeGroup.setCapability(BranchGroup.ALLOW_DETACH);
	    frontShapeGroup.addChild(frontShape);
	    frontGroup.addChild(frontShapeGroup);

	    Shape3D backShape = new Shape3D(quadArray, a);

	    BranchGroup backShapeGroup = new BranchGroup();
	    backShapeGroup.setCapability(BranchGroup.ALLOW_DETACH);
	    backShapeGroup.addChild(backShape);
	    backGroup.insertChild(backShapeGroup, 0);
        
    

	}
    } 
    public BranchGroup getNode(){
    	return root;
    }
 }
 

