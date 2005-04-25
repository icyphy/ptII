/*
 *	%Z%%M% %I% %E% %U%
 *
 * Copyright (c) 1996-2001 Sun Microsystems, Inc. All Rights Reserved.
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
import java.text.NumberFormat;

public class Texture3DVolume extends TextureVolume {

    TexCoordGeneration 	tg = new TexCoordGeneration();
    Texture3D		texture;

    ColorModel          colorModel;
    WritableRaster      raster;

    boolean		timing = false;
    NumberFormat	numFormatter = null;

    public Texture3DVolume(Context context, Volume volume) {
	super(context, volume);
    }

    void loadTexture() {

        System.out.print("Loading 3D texture map");

	// set up the texture coordinate generation
	tg = new TexCoordGeneration();
	tg.setFormat(TexCoordGeneration.TEXTURE_COORDINATE_3);
	tg.setPlaneS(new Vector4f(volume.xTexGenScale, 0.0f, 0.0f, 0.0f));
	tg.setPlaneT(new Vector4f(0.0f, volume.yTexGenScale, 0.0f, 0.0f));
	tg.setPlaneR(new Vector4f(0.0f, 0.0f, volume.zTexGenScale, 0.0f));

	if (useCmap) {
	    colorModel = ColorModel.getRGBdefault();
	} else {
	    ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
	    int[] nBits = {8};
	    colorModel = new ComponentColorModel(cs, nBits, false, false,
		Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
	}
	int sSize = volume.xTexSize;
	int tSize = volume.yTexSize;
	int rSize = volume.zTexSize;

	raster = colorModel.createCompatibleWritableRaster(sSize, tSize);
	BufferedImage bImage =
		new BufferedImage(colorModel, raster, false, null);

	int[] intData = null;
	byte[] byteData = null;
	ImageComponent3D pArray;
	if (useCmap) {
	    intData = ((DataBufferInt)raster.getDataBuffer()).getData();
	    texture = new Texture3D(Texture.BASE_LEVEL, Texture.RGBA,
		sSize, tSize, rSize);
	    pArray = new ImageComponent3D(
		    ImageComponent.FORMAT_RGBA, sSize, tSize, rSize);
	} else {
	    byteData = ((DataBufferByte)raster.getDataBuffer()).getData();
	    texture = new Texture3D(Texture.BASE_LEVEL, Texture.INTENSITY,
		sSize, tSize, rSize);
	    pArray = new ImageComponent3D(
		    ImageComponent.FORMAT_CHANNEL8, sSize, tSize, rSize);
	}
	for (int i=0; i < volume.zDim; i ++) {
	    if (useCmap) {
		volume.loadZRGBA(i, intData, cmap);
	    } else {
		volume.loadZIntensity(i, byteData);
	    }

	    pArray.set(i, bImage);

	    System.out.print(".");
	}
	texture.setImage(0, pArray);
	texture.setEnable(true);
	texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
	texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
	texture.setBoundaryModeS(Texture.CLAMP);
	texture.setBoundaryModeT(Texture.CLAMP);
	texture.setBoundaryModeR(Texture.CLAMP);

	// TODO: reset after load...

        System.out.println("done");
    }

    Texture3D getTexture() {
	return texture;
    }
    TexCoordGeneration getTexGen() {
	return tg;
    }
}

