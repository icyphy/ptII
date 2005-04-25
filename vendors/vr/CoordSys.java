/*
 *        @(#)CoordSys.java 1.4 00/09/20 15:47:48
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

import javax.media.j3d.*;
import javax.vecmath.*;


public class CoordSys extends TransformGroup {
    double scale = 1.0;
    Vector3f translation = new Vector3f();
    Point3d origin = new Point3d(0.0, 0.0, 0.0);
    Point3d plusX = new Point3d(1.0, 0.0, 0.0);
    Point3d plusY = new Point3d(0.0, 1.0, 0.0);
    Point3d plusZ = new Point3d(0.0, 0.0, 1.0);
    Point3d arrowRight = new Point3d();
    Point3d arrowLeft = new Point3d();
    Point3d arrowUp = new Point3d();
    Point3d arrowDown = new Point3d();
    Point3d[] line = new Point3d[10];

    public CoordSys(double scale) {
        this.scale = scale;
        setup();
    }

    public CoordSys(double scale, Vector3d translation) {
        this.scale = scale;
        this.translation.set(translation);
        setup();
    }

    public CoordSys() {
        setup();
    }

    private void setup() {
        // Can be used to make coordSys smaller TODO: specify in constructor
        Transform3D coordTrans = new Transform3D();
        coordTrans.setTranslation(translation);
        coordTrans.setScale(scale);
        setTransform(coordTrans);

        RenderingAttributes ra = new RenderingAttributes();
        ra.setDepthBufferEnable(true);

        LineArray xGeom = new LineArray(10, LineArray.COORDINATES);
        setupArrow(plusX, plusY, plusZ);
        xGeom.setCoordinates(0, line);

        ColoringAttributes xColoringAttributes = new ColoringAttributes(1.0f,
                0.0f, 0.0f, ColoringAttributes.SHADE_FLAT);
        Appearance xAppearance = new Appearance();
        xAppearance.setColoringAttributes(xColoringAttributes);
        xAppearance.setRenderingAttributes(ra);

        Shape3D xShape = new Shape3D(xGeom, xAppearance);
        addChild(xShape);

        LineArray yGeom = new LineArray(10, LineArray.COORDINATES);
        setupArrow(plusY, plusX, plusZ);
        yGeom.setCoordinates(0, line);

        ColoringAttributes yColoringAttributes = new ColoringAttributes(0.0f,
                1.0f, 0.0f, ColoringAttributes.SHADE_FLAT);
        Appearance yAppearance = new Appearance();
        yAppearance.setColoringAttributes(yColoringAttributes);
        yAppearance.setRenderingAttributes(ra);

        Shape3D yShape = new Shape3D(yGeom, yAppearance);
        addChild(yShape);

        LineArray zGeom = new LineArray(10, LineArray.COORDINATES);
        setupArrow(plusZ, plusX, plusY);
        zGeom.setCoordinates(0, line);

        ColoringAttributes zColoringAttributes = new ColoringAttributes(0.0f,
                0.0f, 1.0f, ColoringAttributes.SHADE_FLAT);
        Appearance zAppearance = new Appearance();
        zAppearance.setColoringAttributes(zColoringAttributes);
        zAppearance.setRenderingAttributes(ra);

        Shape3D zShape = new Shape3D(zGeom, zAppearance);
        addChild(zShape);
    }

    private void setupArrow(Point3d forward, Point3d right, Point3d up) {
        line[0] = origin;
        line[1] = forward;

        line[2] = forward;
        arrowRight.x = (forward.x * 0.9) + (right.x * 0.03);
        arrowRight.y = (forward.y * 0.9) + (right.y * 0.03);
        arrowRight.z = (forward.z * 0.9) + (right.z * 0.03);
        line[3] = arrowRight;

        line[4] = forward;
        arrowLeft.x = (forward.x * 0.9) - (right.x * 0.03);
        arrowLeft.y = (forward.y * 0.9) - (right.y * 0.03);
        arrowLeft.z = (forward.z * 0.9) - (right.z * 0.03);
        line[5] = arrowLeft;

        line[6] = forward;
        arrowUp.x = (forward.x * 0.9) + (up.x * 0.03);
        arrowUp.y = (forward.y * 0.9) + (up.y * 0.03);
        arrowUp.z = (forward.z * 0.9) + (up.z * 0.03);
        line[7] = arrowUp;

        line[8] = forward;
        arrowDown.x = (forward.x * 0.9) - (up.x * 0.03);
        arrowDown.y = (forward.y * 0.9) - (up.y * 0.03);
        arrowDown.z = (forward.z * 0.9) - (up.z * 0.03);
        line[9] = arrowDown;
    }
}
