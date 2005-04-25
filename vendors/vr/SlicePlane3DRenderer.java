/*
 *	%Z%%M% %I% %E% %U%
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

public class SlicePlane3DRenderer extends SlicePlaneRenderer {


    Texture3DVolume	texVol;
    Appearance		appearance;
    TextureAttributes 	texAttr;
    Shape3D		shape;

    public SlicePlane3DRenderer(View view, Context context, Volume vol) {
	super(view, context, vol);
	texVol = new Texture3DVolume(context, vol);

	TransparencyAttributes transAttr = new TransparencyAttributes();
	transAttr.setTransparencyMode(TransparencyAttributes.BLENDED);
        texAttr = new TextureAttributes();
	texAttr.setTextureMode(TextureAttributes.MODULATE);
	texAttr.setCapability(TextureAttributes.ALLOW_COLOR_TABLE_WRITE);
	Material m = new Material();
        m.setLightingEnable(false);
	PolygonAttributes p = new PolygonAttributes();
        p.setCullFace(PolygonAttributes.CULL_NONE);
	p.setPolygonOffset(1.0f);
	p.setPolygonOffsetFactor(1.0f);
	appearance = new Appearance();
	appearance.setMaterial(m);
	appearance.setTextureAttributes(texAttr);
	appearance.setTransparencyAttributes(transAttr);
	appearance.setPolygonAttributes(p);
	appearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
	appearance.setCapability(Appearance.ALLOW_TEXGEN_WRITE);

	shape = new Shape3D(null, appearance);
	shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
	shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

	root.addChild(shape);
    }

    void setSliceGeo() {
	GeometryArray pgonGeo = null;

	if (numSlicePts > 0) {
	    count[0] = numSlicePts;

	    pgonGeo = new TriangleFanArray(numSlicePts,
		TriangleFanArray.COORDINATES, count);
	    pgonGeo.setCoordinates(0, slicePts, 0, numSlicePts);
	}

	shape.setGeometry(pgonGeo);

    }

    public void update() {
	boolean reloadPgon = false;
	boolean reloadTct = false;
	int texVolUpdate = texVol.update();

	switch (texVolUpdate) {
	  case TextureVolume.RELOAD_NONE:
	    return;
	  case TextureVolume.RELOAD_VOLUME:
	    appearance.setTexture(texVol.getTexture());
	    appearance.setTexCoordGeneration(texVol.getTexGen());
	    reloadPgon = true;
	    reloadTct = true;
	    break;
	  case TextureVolume.RELOAD_CMAP:
	    reloadTct = true;
	    break;
	}

	if (reloadPgon) {
	    //System.out.println("update: reloadPgon true");
	    setPlaneGeos();
	} else {
	    //System.out.println("update: reloadPgon false");
	}

	if (reloadTct) {
	    tctReload();
	}
    }

    void tctReload() {
	if (texVol.useTextureColorTable) {
	     texAttr.setTextureColorTable(texVol.texColorMap);
	} else {
	     texAttr.setTextureColorTable(null);
	}
    }

}
