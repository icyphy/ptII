/*
 *      @(#)TimingCanvas3D.java 1.6 00/09/20 15:47:48
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


import java.awt.AWTEvent;
import java.awt.event.*;
import java.awt.*;
import sun.awt.*;
import java.text.NumberFormat;
import javax.media.j3d.Canvas3D;

public class TimingCanvas3D extends Canvas3D {
    static final boolean debug = false;
    VolRendListener volRend;
    long startTime;
    long frameStartTime;
    NumberFormat numFormat;
    int numFrames = 0;

    public TimingCanvas3D(GraphicsConfiguration gc, VolRendListener volRend) {
	super(gc);
	this.volRend = volRend;
    	numFormat = NumberFormat.getInstance();
	numFormat.setMaximumFractionDigits(2);
	frameStartTime = System.currentTimeMillis();
    }

    public void preRender() {
	startTime = System.currentTimeMillis();
    }

    public void postRender() {
	//System.out.println("frame");
	if (numFrames++ > 4) {
	    long duration = System.currentTimeMillis() - startTime;

	    double renderSize = volRend.calcRenderSize() / (1024.0 * 1024.0);
	    double renderTime = duration / 1000.0;
	    double renderRate = renderSize / renderTime;

	    if (renderSize != 0) {
		System.out.println("Last frame: " +
			numFormat.format(renderTime) +
			" sec " +
			numFormat.format(renderSize) +
			"M pixels " +
			numFormat.format(renderRate) +
			"M pixels/sec");
	    }
	}
	if ((numFrames % 10) == 0) {
	    double framesDuration  = (System.currentTimeMillis() -
						frameStartTime) / 1000.0;

	    System.out.println("Last 10 frames rendered at " +
		numFormat.format(10.0 / framesDuration) + " frames/sec");

	    frameStartTime = System.currentTimeMillis();

	}
    }
}
