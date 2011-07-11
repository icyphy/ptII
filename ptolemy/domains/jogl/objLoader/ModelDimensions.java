/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2010-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
*/
// ModelDimensions.java
// Andrew Davison, Novemeber 2006, ad@fivedots.coe.psu.ac.th

/* This class calculates the 'edge' coordinates for the model
   along its three dimensions.

   The edge coords are used to calculate the model's:
      * width, height, depth
      * its largest dimension (width, height, or depth)
      * (x, y, z) center point
*/

package ptolemy.domains.jogl.objLoader;

import java.text.DecimalFormat;

public class ModelDimensions {
    // edge coordinates
    private float leftPt, rightPt; // on x-axis
    private float topPt, bottomPt; // on y-axis
    private float farPt, nearPt; // on z-axis

    // for reporting
    private DecimalFormat df = new DecimalFormat("0.##"); // 2 dp

    public ModelDimensions() {
        leftPt = 0.0f;
        rightPt = 0.0f;
        topPt = 0.0f;
        bottomPt = 0.0f;
        farPt = 0.0f;
        nearPt = 0.0f;
    } // end of ModelDimensions()

    public void set(Tuple3 vert)
    // initialize the model's edge coordinates
    {
        rightPt = vert.getX();
        leftPt = vert.getX();

        topPt = vert.getY();
        bottomPt = vert.getY();

        nearPt = vert.getZ();
        farPt = vert.getZ();
    } // end of set()

    public void update(Tuple3 vert)
    // update the edge coordinates using vert
    {
        if (vert.getX() > rightPt) {
            rightPt = vert.getX();
        }
        if (vert.getX() < leftPt) {
            leftPt = vert.getX();
        }

        if (vert.getY() > topPt) {
            topPt = vert.getY();
        }
        if (vert.getY() < bottomPt) {
            bottomPt = vert.getY();
        }

        if (vert.getZ() > nearPt) {
            nearPt = vert.getZ();
        }
        if (vert.getZ() < farPt) {
            farPt = vert.getZ();
        }
    } // end of update()

    // ------------- use the edge coordinates ----------------------------

    public float getWidth() {
        return (rightPt - leftPt);
    }

    public float getHeight() {
        return (topPt - bottomPt);
    }

    public float getDepth() {
        return (nearPt - farPt);
    }

    public float getLargest() {
        float height = getHeight();
        float depth = getDepth();

        float largest = getWidth();
        if (height > largest) {
            largest = height;
        }
        if (depth > largest) {
            largest = depth;
        }

        return largest;
    } // end of getLargest()

    public Tuple3 getCenter() {
        float xc = (rightPt + leftPt) / 2.0f;
        float yc = (topPt + bottomPt) / 2.0f;
        float zc = (nearPt + farPt) / 2.0f;
        return new Tuple3(xc, yc, zc);
    } // end of getCenter()

    public void reportDimensions() {
        Tuple3 center = getCenter();

        System.out.println("x Coords: " + df.format(leftPt) + " to "
                + df.format(rightPt));
        System.out.println("  Mid: " + df.format(center.getX()) + "; Width: "
                + df.format(getWidth()));

        System.out.println("y Coords: " + df.format(bottomPt) + " to "
                + df.format(topPt));
        System.out.println("  Mid: " + df.format(center.getY()) + "; Height: "
                + df.format(getHeight()));

        System.out.println("z Coords: " + df.format(nearPt) + " to "
                + df.format(farPt));
        System.out.println("  Mid: " + df.format(center.getZ()) + "; Depth: "
                + df.format(getDepth()));
    } // end of reportDimensions()

} // end of ModelDimensions class
