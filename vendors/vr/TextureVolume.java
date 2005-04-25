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
import javax.media.j3d.*;
import javax.vecmath.*;
import java.io.*;
import java.text.NumberFormat;

public abstract class TextureVolume implements VolRendConstants {

    // Attrs we care about
    ColormapChoiceAttr  colorModeAttr;
    ToggleAttr		texColorMapAttr;

    Volume		volume;
    Colormap		cmap = null;
    boolean		useCmap = false;;
    boolean 		useTextureColorTable = false;
    boolean 		texColorMapEnable = false;
    boolean 		texColorMapAvailable = true;
    int[][]		texColorMap;

    static final int	RELOAD_NONE = 0;
    static final int	RELOAD_VOLUME = 1;
    static final int	RELOAD_CMAP = 2;

    static final int	RED = 0;
    static final int	GREEN = 1;
    static final int	BLUE = 2;
    static final int	ALPHA = 3;

    // cache for update()
    int			volEditId = -1;
    int			cmapEditId = -1;

    boolean		timing = false;
    //NumberFormat	numFormatter = null;

    boolean		volumeReloadNeeded = true;
    boolean		tctReloadNeeded = true;

    public TextureVolume(Context context, Volume volume) {
//        timing = Boolean.getBoolean("timing");
	this.volume = volume;
	colorModeAttr = (ColormapChoiceAttr) context.getAttr("Color Mode");
	texColorMapAttr = (ToggleAttr) context.getAttr("Tex Color Map");
	texColorMapEnable = texColorMapAttr.getValue();
	texColorMap = new int[4][];
	texColorMap[0] = new int[256];
	texColorMap[1] = new int[256];
	texColorMap[2] = new int[256];
	texColorMap[3] = new int[256];
    }

    int update() {
	int newVolEditId = -1;
	if ((newVolEditId = volume.update()) != volEditId) {
	    volEditId = newVolEditId;
	    volumeReloadNeeded = true;
	}
	boolean newTexColorMapEnable = texColorMapAttr.getValue();
	int newCmapEditId = -1;
	if (colorModeAttr.getColormap() != cmap) {
	    cmap = colorModeAttr.getColormap();
	    if (cmap != null) {
		cmapEditId = cmap.update();
	    }
        else {
		cmapEditId = -1;
	    }
	    if (texColorMapAvailable && texColorMapEnable) {
		tctReloadNeeded = true;
		useCmap = false;
    		useTextureColorTable = true;
	    }
        else {
        	if (cmap != null) {
        		useCmap = true;
        	}
            else {
            	useCmap = false;
            }
        	useTextureColorTable = false;
        	volumeReloadNeeded = true;
	    }
	}
    else if ((cmap != null) &&
		   (((newCmapEditId = cmap.update()) != cmapEditId) ||
	            (texColorMapEnable != newTexColorMapEnable))) {
	    if (texColorMapAvailable && newTexColorMapEnable) {
	    	tctReloadNeeded = true;
	    	useCmap = false;
    		useTextureColorTable = true;
	        if (!texColorMapEnable) {
		    /* previously loaded with color table, need to reload
		     * w/o color table
		     */
	        	volumeReloadNeeded = true;
	        }
	    }
        else {
        	useCmap = true;
    		useTextureColorTable = false;
    		volumeReloadNeeded = true;
	    }
	    cmapEditId = newCmapEditId;
	    texColorMapEnable = newTexColorMapEnable;
	}

	if (volumeReloadNeeded) {
	    volumeReload();
	    tctReload();
	    return RELOAD_VOLUME;
	}
    else if (tctReloadNeeded) {
	    tctReload();
	    return RELOAD_CMAP;
	}
    else {
	    return RELOAD_NONE;
	}
}

    void volumeReload() {
	if (volume.hasData()) {
	    long start = 0;
	    if (timing) {
		start = System.currentTimeMillis();
	    }
	    loadTexture();
	    if (timing) {
		long end = System.currentTimeMillis();
		double elapsed = (end - start) / 1000.0;
		System.out.println("Texture load took " +
			/*numFormat*/ (elapsed) + " seconds");
	    }
	}
	volumeReloadNeeded = false;
    }

    int byteToInt(int byteValue)
    {
	return (int)((byteValue / 255.0f) * Integer.MAX_VALUE);
    }

    void tctReload() {
	if ((cmap != null) && texColorMapEnable) {
	    for (int i = 0; i < 256; i++) {
		texColorMap[RED][i] =
			byteToInt((cmap.colorMapping[i] & 0x00ff0000) >> 16);
		texColorMap[GREEN][i] =
			byteToInt((cmap.colorMapping[i] & 0x0000ff00) >> 8);
		texColorMap[BLUE][i] =
			byteToInt((cmap.colorMapping[i] & 0x000000ff));
		texColorMap[ALPHA][i] =
			byteToInt((cmap.colorMapping[i] & 0xff000000) >> 24);
	    }
	    useTextureColorTable = true;
	} else {
	    useTextureColorTable = false;
	}
	tctReloadNeeded = false;
    }

    abstract void loadTexture();
}
