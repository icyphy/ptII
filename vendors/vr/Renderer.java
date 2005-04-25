/*
 *	@(#)Renderer.java 1.3 00/09/20 15:47:52
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
import java.text.NumberFormat;
import com.sun.j3d.utils.behaviors.mouse.*;

abstract public class Renderer {

    View	view;
    Context	context;
    Volume 	volume;

    boolean	debug = false;
    boolean	timing = false;
    NumberFormat numFormatter = null;

    public Renderer(View vw, Context ctx, Volume vol) {
	view = vw;
	context = ctx;
	volume = vol;
//	debug = Boolean.getBoolean("debug");
//	timing = Boolean.getBoolean("timing");
    }

    /**
     * Attach the branchgroups for this renderer to the display
     */
    abstract public void attach(Group dynamicGroup, Group staticGroup);

    /**
     * Called to make changes to the renderer state
     */
    abstract void update();

    /**
     * Returns the number of pixels drawn in the current display
     */
    abstract public double calcRenderSize(ScreenSizeCalculator screenSize,
		Canvas3D canvas);

    /**
     * Called when the tranform for the volume has changed
     */
    public void transformChanged(int type, Transform3D trans) {};

    /**
     * Called when the view position relative to the renderer changes
     */
    public void eyePtChanged() {};


    /**
     * return the eye's position in <node>'s coordinate space
     */
    Point3d getViewPosInLocal(Node node) {

	Point3d viewPosition = new Point3d();
	Vector3d translate = new Vector3d();
	double angle = 0.0;
	double mag,sign;
	double tx,ty,tz;


	if (node == null ){
	    System.out.println("called getViewPosInLocal() with null node");
	    return null;
	}
	if (!node.isLive()) {
	    System.out.println("called getViewPosInLocal() with non-live node");
	    return null;
	}

	//  get viewplatforms's location in virutal world
	Canvas3D canvas = (Canvas3D)view.getCanvas3D(0);
	canvas.getCenterEyeInImagePlate(viewPosition);
	Transform3D t = new Transform3D();
	canvas.getImagePlateToVworld(t);
	t.transform(viewPosition);
	//System.out.println("vworld view position is " + viewPosition);

	// get parent transform
	Transform3D parentInv = new Transform3D();
	node.getLocalToVworld(parentInv);
	//System.out.println("node xform is \n" + parentInv);
	parentInv.invert();

	// transform the eye position into the parent's coordinate system
	parentInv.transform(viewPosition);

	//System.out.println("node space view position is " + viewPosition);

	return viewPosition;
    }

    // format a number to two digits past the decimal
    String numFormat(double value) {
	return numFormat(value, 2);
    }

    // format a number to numDigits past the decimal
    String numFormat(double value, int numDigits) {
	if (numFormatter == null) {
	    numFormatter = NumberFormat.getInstance();
	}
	numFormatter.setMaximumFractionDigits(numDigits);
	return numFormatter.format(value);
    }
}
