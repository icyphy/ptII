/*
 *      @(#)CoordAttr.java 1.1 00/09/21 14:21:24
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


import javax.vecmath.*;
import javax.media.j3d.Transform3D;

public class CoordAttr extends Attr {
    Point3d value = new Point3d();
    Point3d initValue = new Point3d();

    CoordAttr(String label, Point3d initValue) {
	super(label);
	this.initValue.set(initValue);
	this.value.set(initValue);
    }
    public void set(String stringValue) {
	int index;
	String doubleString;
	index = stringValue.indexOf('(');
	stringValue = stringValue.substring(index+1);

	index = stringValue.indexOf(',');
	doubleString = stringValue.substring(0, index);
	double x = Double.valueOf(doubleString).doubleValue();

	stringValue = stringValue.substring(index+1);
	index = stringValue.indexOf(',');
	doubleString = stringValue.substring(0, index);
	double y = Double.valueOf(doubleString).doubleValue();

	stringValue = stringValue.substring(index+1);
	index = stringValue.indexOf(')');
	doubleString = stringValue.substring(0, index);
	double z = Double.valueOf(doubleString).doubleValue();

	set(new Point3d(x, y, z));
    }
    public void set(Point3d newValue) {
	value.set(newValue);
    }
    public void set(Point3f newValue) {
	value.set(newValue);
    }
    public void reset() {
	value.set(initValue);
    }
    public String toString() {
	return name + " (" + numFormat.format(value.x) +
		", " + numFormat.format(value.y) +
		", " + numFormat.format(value.z) +
		")";
    }
    public Point3d getValue() {
	return new Point3d(value);
    }
}
