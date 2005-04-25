/*
 *	@(#)AxisRenderer.java 1.5 00/09/21 15:48:21
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
import com.sun.j3d.utils.image.TextureLoader;
import java.util.BitSet;
import java.net.*;


// Annotations are geometry drawn around the outside of the volume.
// The volume is transparent and the annotations may also be transparent
// so we have to be explicit about the drawing order.  The annotations
// are defined as primitives attached to the faces of the VOI volume.  The
// faces are named using the VolRendConstants PLUS_X, MINUS_X, etc.
//
// The overall structure is set up so that the volume and annotations are
// in an OrderedGroup with the children:
//
//	BackAnnotations: the back facing annotation faces
//      Volume: the volume
// 	FrontAnnotations: the front facing annoation faces
//
// BackAnnotations and FrontAnnotations are Switch nodes which share common
// children.  Each has six children, corresponding to the faces.  As the eye
// point changes the Switches are updated to turn on and off the display
// of each face as it moves from front facing to back facing and back.  Each
// Switch has children
//    PLUS_X: Group
//	Link to +X SharedGroup
//	    +X SharedGroup
//	        Annotation Geometry
//    PLUS_Y...
//



public class Annotations extends Renderer implements VolRendConstants {

    // Attrs we care about
    CoordAttr		viewPtAttr;
    ToggleAttr          perspectiveAttr;
    ToggleAttr          boxAttr[] = new ToggleAttr[6];
    StringAttr          imageAttr[] = new StringAttr[6];

    Volume              volume;
    int                 volumeEditId = -1;

    BranchGroup		frontRoot;
    BranchGroup		backRoot;
    Switch		frontAnnotations = new Switch(Switch.CHILD_MASK);
    Switch		backAnnotations = new Switch(Switch.CHILD_MASK);
    BitSet              frontFaceBits = new BitSet();
    BitSet              backFaceBits = new BitSet();
    SharedGroup         faceGroup[] = new SharedGroup[6];
    Point3d             faceCenter[] = new Point3d[6];
    Vector3d            faceNormal[] = new Vector3d[6];
    Switch              boxSwitch[] = new Switch[6];
    LineStripArray      boxLine[] = new LineStripArray[6];
    QuadArray      	imageQuad[] = new QuadArray[6];
    String		imageFile[] = new String[6];
    Appearance 		imageAppearance[] = new Appearance[6];
    Switch              imageSwitch[] = new Switch[6];
    Texture 		imageTexture[] = new Texture[6];
    boolean		reloadNeeded = true;
    Point3d             volCenter = new Point3d();
    Vector3d            eyeVec = new Vector3d();

    public Annotations(View view, Context context, Volume vol) {
        super(view, context, vol);
	viewPtAttr = (CoordAttr) context.getAttr("Vol Ref Pt");
        perspectiveAttr = (ToggleAttr) context.getAttr("Perspective");
        boxAttr[PLUS_X] = (ToggleAttr) context.getAttr("Plus X Box");
        boxAttr[PLUS_Y] = (ToggleAttr) context.getAttr("Plus Y Box");
        boxAttr[PLUS_Z] = (ToggleAttr) context.getAttr("Plus Z Box");
        boxAttr[MINUS_X] = (ToggleAttr) context.getAttr("Minus X Box");
        boxAttr[MINUS_Y] = (ToggleAttr) context.getAttr("Minus Y Box");
        boxAttr[MINUS_Z] = (ToggleAttr) context.getAttr("Minus Z Box");
        imageAttr[PLUS_X] = (StringAttr) context.getAttr("Plus X Image");
        imageAttr[PLUS_Y] = (StringAttr) context.getAttr("Plus Y Image");
        imageAttr[PLUS_Z] = (StringAttr) context.getAttr("Plus Z Image");
        imageAttr[MINUS_X] = (StringAttr) context.getAttr("Minus X Image");
        imageAttr[MINUS_Y] = (StringAttr) context.getAttr("Minus Y Image");
        imageAttr[MINUS_Z] = (StringAttr) context.getAttr("Minus Z Image");

        volume = vol;
        frontRoot = new BranchGroup();
        frontRoot.setCapability(BranchGroup.ALLOW_DETACH);
        frontRoot.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);
        backRoot = new BranchGroup();
        backRoot.setCapability(BranchGroup.ALLOW_DETACH);
        backRoot.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);

        int stripLength[] = new int[1];
        stripLength[0] = 5;

        ColoringAttributes ca = new ColoringAttributes(1.0f, 0.0f, 0.0f,
            ColoringAttributes.SHADE_FLAT);
        Appearance boxAppearance = new Appearance();
        boxAppearance.setColoringAttributes(ca);

	TexCoord2f[] texCoords = new TexCoord2f[4];
	texCoords[0] = new TexCoord2f(0.0f, 0.0f);
	texCoords[1] = new TexCoord2f(1.0f, 0.0f);
	texCoords[2] = new TexCoord2f(1.0f, 1.0f);
	texCoords[3] = new TexCoord2f(0.0f, 1.0f);

	TransparencyAttributes ta = new TransparencyAttributes(
		TransparencyAttributes.BLENDED, 0.0f);
	TextureAttributes texAttr = new TextureAttributes();
	texAttr.setTextureMode(TextureAttributes.MODULATE);
	PolygonAttributes pa = new PolygonAttributes();
	pa.setCullFace(PolygonAttributes.CULL_NONE);
	RenderingAttributes ra = new RenderingAttributes();
	ra.setDepthBufferEnable(false);

        for (int i = 0; i < 6; i++) {
            faceGroup[i] = new SharedGroup();
	    frontAnnotations.addChild(new Link(faceGroup[i]));
      	    backAnnotations.addChild(new Link(faceGroup[i]));
            boxLine[i] = new LineStripArray(5, GeometryArray.COORDINATES,
                stripLength);
            boxLine[i].setCoordinates(0, volume.facePoints[i], 0, 4);
            boxLine[i].setCoordinate(4, volume.facePoints[i][0]);
            boxLine[i].setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
            Shape3D box = new Shape3D(boxLine[i], boxAppearance);
	    boxSwitch[i] = new Switch();
	    boxSwitch[i].setCapability(Switch.ALLOW_SWITCH_WRITE);
	    boxSwitch[i].addChild(box);
	    if (boxAttr[i].getValue() == true) {
		boxSwitch[i].setWhichChild(Switch.CHILD_ALL);
	    } else {
		boxSwitch[i].setWhichChild(Switch.CHILD_NONE);
	    }
            faceGroup[i].addChild(boxSwitch[i]);
	    imageQuad[i] = new QuadArray(4, QuadArray.COORDINATES |
		QuadArray.TEXTURE_COORDINATE_2);
            imageQuad[i].setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
            imageQuad[i].setCoordinates(0, volume.facePoints[i], 0, 4);
	    imageQuad[i].setTextureCoordinates(0, 0, texCoords, 0, 4);
	    imageAppearance[i] = new Appearance();
	    imageAppearance[i].setTransparencyAttributes(ta);
	    imageAppearance[i].setPolygonAttributes(pa);
	    imageAppearance[i].setRenderingAttributes(ra);
	    imageAppearance[i].setTextureAttributes(texAttr);
	    imageAppearance[i].setCapability(Appearance.ALLOW_TEXTURE_WRITE);
	    imageSwitch[i] = new Switch();
	    imageSwitch[i].setCapability(Switch.ALLOW_SWITCH_WRITE);
	    imageFile[i] = imageAttr[i].getValue();
	    if (imageFile[i].length() > 0) {
		try {
		    URL imageURL = new URL(
			    context.getCodeBase().toString() +
			    imageFile[i]);
		    imageTexture[i] =
			new TextureLoader(imageURL, null).getTexture();
		} catch (Exception e) {
		    System.err.println("Error " + e +
				" loading image:" + imageFile[i] + ".");
		}
	    }
	    imageAppearance[i].setTexture(imageTexture[i]);
	    if (imageTexture[i] != null) {
		imageSwitch[i].setWhichChild(Switch.CHILD_ALL);
	    } else {
		imageSwitch[i].setWhichChild(Switch.CHILD_NONE);
	    }
	    Shape3D imageShape = new Shape3D(imageQuad[i], imageAppearance[i]);
	    imageSwitch[i].addChild(imageShape);
            faceGroup[i].addChild(imageSwitch[i]);
        }
        frontAnnotations.setCapability(Switch.ALLOW_SWITCH_WRITE);
        backAnnotations.setCapability(Switch.ALLOW_SWITCH_WRITE);

        faceNormal[PLUS_X] = new Vector3d(1.0, 0.0, 0.0);
        faceCenter [PLUS_X]= new Point3d(volume.maxCoord.x, 0.0, 0.0);
        faceNormal[PLUS_Y] = new Vector3d(0.0, 1.0, 0.0);
        faceCenter[PLUS_Y] = new Point3d(0.0, volume.maxCoord.y, 0.0);
        faceNormal[PLUS_Z] = new Vector3d(0.0, 0.0, 1.0);
        faceCenter[PLUS_Z] = new Point3d(0.0, 0.0, volume.maxCoord.z);
        faceNormal[MINUS_X] = new Vector3d(-1.0,  0.0,  0.0);
        faceCenter[MINUS_X] = new Point3d(volume.minCoord.x,  0.0,  0.0);
        faceNormal[MINUS_Y] = new Vector3d( 0.0, -1.0,  0.0);
        faceCenter[MINUS_Y] = new Point3d( 0.0, volume.minCoord.y,  0.0);
        faceNormal[MINUS_Z] = new Vector3d( 0.0,  0.0, -1.0);
        faceCenter[MINUS_Z] = new Point3d( 0.0,  0.0, volume.minCoord.z);
        volCenter.x = (volume.maxCoord.x + volume.minCoord.x)/2;
        volCenter.y = (volume.maxCoord.y + volume.minCoord.y)/2;
        volCenter.z = (volume.maxCoord.z + volume.minCoord.z)/2;

	frontRoot.addChild(frontAnnotations);
	backRoot.addChild(backAnnotations);
    }

    public void attach(Group dynamicGroup, Group staticGroup) {
        dynamicGroup.addChild(frontRoot);
    }

    public void attachBack(Group dynamicGroup, Group staticGroup) {
        //dynamicGroup.addChild(backRoot);
    }

    public void eyePtChanged() {

        Point3d eyePt = getViewPosInLocal(frontRoot);

	//System.out.println("eye pt = " + eyePt)
	if (eyePt != null) {

            for (int i = 0; i < 6; i++) {
                if (perspectiveAttr.getValue() == true) {
                    eyeVec.sub(eyePt, faceCenter[i]);
                } else {
                    eyeVec.sub(eyePt, volCenter);
                }
		frontFaceBits.set(i);
		backFaceBits.clear(i);
                if (eyeVec.dot(faceNormal[i]) < 0) {
                    backFaceBits.set(i);
                    frontFaceBits.clear(i);
                } else {
                    frontFaceBits.set(i);
                    backFaceBits.clear(i);
                }
            }
            frontAnnotations.setChildMask(frontFaceBits);
            backAnnotations.setChildMask(backFaceBits);
	}
    }

    void update() {
        int newEditId;
        if ((newEditId = volume.update()) != volumeEditId) {
	    for (int i = 0; i < 6; i++) {
                boxLine[i].setCoordinates(0, volume.facePoints[i], 0, 4);
                boxLine[i].setCoordinate(4, volume.facePoints[i][0]);
		imageQuad[i].setCoordinates(0, volume.facePoints[i], 0, 4);
            }
            faceCenter[PLUS_X].set(volume.maxCoord.x, 0.0, 0.0);
            faceCenter[PLUS_Y].set(0.0, volume.maxCoord.y, 0.0);
            faceCenter[PLUS_Z].set(0.0, 0.0, volume.maxCoord.z);
            faceCenter[MINUS_X].set(volume.minCoord.x,  0.0,  0.0);
            faceCenter[MINUS_Y].set( 0.0, volume.minCoord.y,  0.0);
            faceCenter[MINUS_Z].set( 0.0,  0.0, volume.minCoord.z);
            volCenter.x = (volume.maxCoord.x + volume.minCoord.x)/2;
            volCenter.y = (volume.maxCoord.y + volume.minCoord.y)/2;
            volCenter.z = (volume.maxCoord.z + volume.minCoord.z)/2;
            volumeEditId = newEditId;
        }
        eyePtChanged();
	for (int i = 0; i < 6; i++) {
	    if (boxAttr[i].getValue() == true) {
		boxSwitch[i].setWhichChild(Switch.CHILD_ALL);
	    } else {
		boxSwitch[i].setWhichChild(Switch.CHILD_NONE);
	    }
	    String curImageFile = imageAttr[i].getValue();
	    if (curImageFile != imageFile[i]) {
		imageFile[i] = curImageFile;
		if (imageFile[i].length() > 0) {
		    try {
			URL imageURL = new URL(
				context.getCodeBase().toString() +
				imageFile[i]);
			imageTexture[i] =
			    new TextureLoader(imageURL, null).getTexture();
		    } catch (Exception e) {
			System.err.println("Error " + e +
				" loading image:" + imageFile[i] + ".");
		    }
		}
		imageAppearance[i].setTexture(imageTexture[i]);
		if (imageTexture[i] != null) {
		    imageSwitch[i].setWhichChild(Switch.CHILD_ALL);
		} else {
		    imageSwitch[i].setWhichChild(Switch.CHILD_NONE);
		}
	    }
	}
    }

    public double calcRenderSize(ScreenSizeCalculator calc, Canvas3D canvas) {
        return 0.0;
    }
}
