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


public class SegyColormap extends Colormap {
    IntAttr	minAlphaAttr;
    IntAttr	maxAlphaAttr;
    int   	minAlpha;
    int   	maxAlpha;

    SegyColormap(IntAttr minAlphaAttr, IntAttr maxAlphaAttr) {
	this.minAlphaAttr = minAlphaAttr;
	this.maxAlphaAttr = maxAlphaAttr;
	minAlpha = maxAlphaAttr.getValue();
	maxAlpha = maxAlphaAttr.getValue();
	updateMapping();
    }
    void updateMapping() {
	// set up mappings (from C code by Prem Domingo)
	for (int i = 0; i < 256; i++) {
	    double fraction = i / 256.0;
	    double value;
	    int redMapping, greenMapping, blueMapping, alphaMapping;
	    value = 255.0 * Math.abs(Math.sin(fraction * Math.PI / 2.0));
	    redMapping = (int) value;
	    value = 200.0 * Math.abs(Math.sin(fraction * Math.PI));
	    if (value > 175.0) {
		value = 175.0;
	    }
	    greenMapping = (int) value;
	    value = 255.0 * Math.abs(Math.cos(fraction * Math.PI / 2.0));
	    if (i == 0) {
		value = 0;
	    }
	    blueMapping = (int) value;
	    if (i == 0) {
		value = 0;
	    } else if (i <  minAlpha) {
		value = 255 * 0.9;
	    } else if (i < maxAlpha) {
		value = 1; // just a touch
	    } else {
		value = 255 * 0.9;
	    }
	    alphaMapping = (int) value;
	    colorMapping[i] = (alphaMapping & 0xFF) << 24 |
	    	  	(redMapping & 0xFF) << 16 |
	    	  	(greenMapping & 0xFF) << 8 |
	    	  	(blueMapping & 0xFF);
	}
	editId++;
    }
    int update() {
	int newMinAlpha, newMaxAlpha;
	newMinAlpha = minAlphaAttr.getValue();
	newMaxAlpha = maxAlphaAttr.getValue();
	if ((minAlpha != newMinAlpha) || (maxAlpha != newMaxAlpha)) {
	    minAlpha = newMinAlpha;
	    maxAlpha = newMaxAlpha;
	    updateMapping();
	}
	return editId;
    }
}
