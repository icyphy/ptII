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

import java.awt.*;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Texture;
import javax.vecmath.*;
import java.io.*;
import java.text.NumberFormat;

public class Texture2DVolume extends TextureVolume {

    // sets of textures, one for each axis, sizes of the arrays are set
    // by the dimensions of the Volume
    Texture2D[]		xTextures;
    Texture2D[]		yTextures;
    Texture2D[]		zTextures;

    TexCoordGeneration 	xTg = new TexCoordGeneration();
    TexCoordGeneration 	yTg = new TexCoordGeneration();
    TexCoordGeneration 	zTg = new TexCoordGeneration();

    ColorModel          colorModel;
    WritableRaster      raster;

    public Texture2DVolume(Context context, Volume volume) {
	super(context, volume);
    }

    public void loadTexture() {
	if (useCmap) {
	    colorModel = ColorModel.getRGBdefault();
	} else {
	    ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
	    int[] nBits = {8};
	    colorModel = new ComponentColorModel(cs, nBits, false, false,
		Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
	}

	long start = 0;
	if (timing) {
	    start = System.currentTimeMillis();
	}
	System.out.print("Loading Z axis texture maps");
	loadAxis(Z_AXIS);
	System.out.println("done");
	System.out.print("Loading Y axis texture maps");
	loadAxis(Y_AXIS);
	System.out.println("done");
	System.out.print("Loading X axis texture maps");
	loadAxis(X_AXIS);
	System.out.println("done");
	//if (timing) {
	//    long end = System.currentTimeMillis();
	//    double elapsed = (end - start) / 1000.0;
	//    System.out.println("Load took " + numFormat(elapsed) +
	//		" seconds");
	//}
    }

    private void loadAxis(int axis) {
	int	rSize = 0;        // number of tex maps to create
	int	sSize = 0; 	  // s,t = size of texture map to create
	int 	tSize = 0;
	Texture2D[] textures = null;

	switch (axis) {
	  case Z_AXIS:
	    rSize = volume.zDim;
	    sSize = volume.xTexSize;
	    tSize = volume.yTexSize;
	    textures = zTextures = new Texture2D[rSize];
    	    zTg = new TexCoordGeneration();
	    zTg.setPlaneS(new Vector4f(volume.xTexGenScale, 0.0f, 0.0f, 0.0f));
	    zTg.setPlaneT(new Vector4f(0.0f, volume.yTexGenScale, 0.0f, 0.0f));
	    break;
	  case Y_AXIS:
	    rSize = volume.yDim;
	    sSize = volume.xTexSize;
	    tSize = volume.zTexSize;
	    textures = yTextures = new Texture2D[rSize];
    	    yTg = new TexCoordGeneration();
	    yTg.setPlaneS(new Vector4f(volume.xTexGenScale, 0.0f, 0.0f, 0.0f));
	    yTg.setPlaneT(new Vector4f(0.0f, 0.0f, volume.zTexGenScale, 0.0f));
	    break;
	  case X_AXIS:
	    rSize = volume.xDim;
	    sSize = volume.yTexSize;
	    tSize = volume.zTexSize;
	    textures = xTextures = new Texture2D[rSize];
    	    xTg = new TexCoordGeneration();
 	    xTg.setPlaneS(new Vector4f(0.0f, volume.yTexGenScale, 0.0f, 0.0f));
	    xTg.setPlaneT(new Vector4f(0.0f, 0.0f, volume.zTexGenScale, 0.0f));
	    break;
	}

	raster = colorModel.createCompatibleWritableRaster(sSize, tSize);

	BufferedImage bImage =
		new BufferedImage(colorModel, raster, false, null);

	int[] intData = null;
	byte[] byteData = null;
	if (useCmap) {
	    intData = ((DataBufferInt)raster.getDataBuffer()).getData();
	} else {
	    byteData = ((DataBufferByte)raster.getDataBuffer()).getData();
	}


	for (int i=0; i < rSize; i ++) {
	    switch (axis) {
	      case Z_AXIS:
		if (useCmap) {
		    volume.loadZRGBA(i, intData, cmap);
		} else {
		    volume.loadZIntensity(i, byteData);
		}
		break;
	      case Y_AXIS:
		if (useCmap) {
		    volume.loadYRGBA(i, intData, cmap);
		} else {
		    volume.loadYIntensity(i, byteData);
		}
		break;
	      case X_AXIS:
		if (useCmap) {
		    volume.loadXRGBA(i, intData, cmap);
		} else {
		    volume.loadXIntensity(i, byteData);
		}
		break;
	    }

	    Texture2D tex;
	    ImageComponent2D pArray;
	    if (useCmap) {
		tex = new Texture2D(Texture.BASE_LEVEL,
		    Texture.RGBA, sSize, tSize);
		pArray = new ImageComponent2D(
			ImageComponent.FORMAT_RGBA, sSize, tSize);
	    } else {
		tex = new Texture2D(Texture.BASE_LEVEL,
		    Texture.INTENSITY, sSize, tSize);
		pArray = new ImageComponent2D(
			ImageComponent.FORMAT_CHANNEL8, sSize, tSize);
	    }
	    pArray.set(bImage);


	    tex.setImage(0, pArray);
	    tex.setEnable(true);
	    tex.setMinFilter(Texture.BASE_LEVEL_LINEAR);
	    tex.setMagFilter(Texture.BASE_LEVEL_LINEAR);
	    //tex.setMinFilter(Texture.BASE_LEVEL_POINT);
	    //tex.setMagFilter(Texture.BASE_LEVEL_POINT);
	    tex.setBoundaryModeS(Texture.CLAMP);
	    tex.setBoundaryModeT(Texture.CLAMP);

	    textures[i] = tex;
	    System.out.print(".");

	}
    }
}
