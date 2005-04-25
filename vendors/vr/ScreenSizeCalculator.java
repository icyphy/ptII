/*
 *	@(#)ScreenSizeCalculator.java 1.4 00/09/20 15:47:49
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

import java.awt.Dimension;
import java.awt.Point;
import javax.media.j3d.*;
import javax.vecmath.*;


public class ScreenSizeCalculator {

    LocalToWindow	locToWindow;
    Point3d		localPt;
    Point2d		windowPts[];

    public ScreenSizeCalculator() {
	locToWindow = new LocalToWindow();
	localPt = new Point3d();
	windowPts = new Point2d[4];
	for (int i = 0; i < 4; i++) {
	    windowPts[i] = new Point2d();
	}
    }

    public void setScreenXform(Canvas3D canvas, Node renderNode) {
	locToWindow.update(renderNode, canvas);
    }

    private double triArea(Point2d pt1, Point2d pt2, Point2d pt3) {
	Point2d  top, mid, bot;
	double area;

	/* sort the points to find top, mid and bot in y */
	top = pt1;
	if (pt2.y > top.y) {
	    top = pt2;
	}
	if (pt3.y > top.y) {
	    top = pt3;
	}
	bot = pt1;
	if (pt2.y < bot.y) {
	    bot = pt2;
	}
	if (pt3.y < bot.y) {
	    bot = pt3;
	}
	mid = pt1;
	if ((mid == top) || (mid == bot)) {
	    mid = pt2;
	    if ((mid == top) || (mid == bot)) {
		mid = pt3;
	    }
	}
	/* now have a tri:

		top
		| \
		|  mid
		|  /
		| /
		bot

	(or a mirror image)

	intersect top-bot edge at y value of mid

	*/
	double deltaX = top.x - bot.x;
	double deltaY = top.y - bot.y;

	if (deltaY == 0.0) {
	 return 0.0;
	}
	double intersectX = bot.x + deltaX * (mid.y - bot.y) / deltaY;

	/* area is the sum of the area of the two tris */
	area =  Math.abs(0.5 * (intersectX - mid.x) * (mid.y - bot.y));
	area += Math.abs(0.5 * (intersectX - mid.x) * (top.y - mid.y));

	return area;

    }


    double quadScreenSize(double[] coords) {
	double area = 0.0f;
	//System.out.println("wc pts = \n\t(" +
	//	coords[0] + ", " + coords[1] + ", " + coords[2] + ") (" +
	//	coords[3] + ", " + coords[4] + ", " + coords[5] + ")\n\t(" +
	//	coords[6] + ", " + coords[7] + ", " + coords[8] + ") (" +
	//	coords[9] + ", " + coords[10] + ", " + coords[11] + ")");
	for (int i = 0; i < 4; i++) {
	    localPt.x = coords[i * 3 + 0];
	    localPt.y = coords[i * 3 + 1];
	    localPt.z = coords[i * 3 + 2];
	    locToWindow.transformPt(localPt, windowPts[i]);
 	}
	//System.out.println("window pts = " + windowPts[0] + " " +
	//	windowPts[1] + " " + windowPts[2] + " " +
	//	windowPts[3]);
	area += triArea(windowPts[0], windowPts[1], windowPts[2]);
	area += triArea(windowPts[1], windowPts[2], windowPts[3]);

	return area;
    }

}
