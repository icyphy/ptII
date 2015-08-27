/*
Copyright (c) 2013 by the Regents of the University of Michigan.
Developed by the APRIL robotics lab under the direction of Edwin Olson (ebolson@umich.edu).

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation
are those of the authors and should not be interpreted as representing
official policies, either expressed or implied, of the FreeBSD
Project.

This implementation of the AprilTags detector is provided for
convenience as a demonstration.  It is an older version implemented in
Java that has been supplanted by a much better performing C version.
If your application demands better performance, you will need to
replace this implementation with the newer C version and using JNI or
JNA to interface the C version to Java.

For details about the C version, see
https://april.eecs.umich.edu/wiki/index.php/AprilTags-C

*/

package edu.umich.eecs.april.jmat.geom;


import java.io.Serializable;
import java.util.List;

import edu.umich.eecs.april.jmat.LinAlg;

/** A 2D line with end points. **/
public class GLineSegment2D implements Serializable
{
    public GLine2D line;
    public double p1[];
    public double p2[];
    public int weight;

    static final long serialVersionUID=1001;

    public GLineSegment2D(double p1[], double p2[])
    {
        this.p1=p1;
        this.p2=p2;
        this.line=new GLine2D(p1,p2);
    }

    public GLine2D getLine()
    {
        return line;
    }

    /** Returns the point where this segment intersects this line, or null
     * if they do not intersect.
     **/
    public double[] intersectionWith(GLine2D l)
    {
        double p[]=line.intersectionWith(l);
        if (p==null)
            return null; //parallel!

        double a,b,c;
        a=line.getLineCoordinate(p1);
        b=line.getLineCoordinate(p2);
        c=line.getLineCoordinate(p);

        // b must be between a and c.
        if ((a<b && b<c) || (c<b && b<a))
            return p;

        return null;
    }

    public double[] intersectionWith(GLineSegment2D seg)
    {
        double p[]=line.intersectionWith(seg.line);
        if (p==null)
            return null; //parallel!

        double a,b,c;
        a=line.getLineCoordinate(p1);
        b=line.getLineCoordinate(p2);
        c=line.getLineCoordinate(p);

        // does intersection lie on first line?
        if ((c<a && c<b) || (c>a && c>b))
            return null;

        a=seg.line.getLineCoordinate(seg.p1);
        b=seg.line.getLineCoordinate(seg.p2);
        c=seg.line.getLineCoordinate(p);

        // does intersection lie on first line?
        if ((c<a && c<b) || (c>a && c>b))
            return null;

        return p;
    }

    public double[] closestPoint(double p[])
    {
        double pclosest[]=line.pointOnLineClosestTo(p);

        double a,b,c;
        a=line.getLineCoordinate(p1);
        b=line.getLineCoordinate(p2);
        c=line.getLineCoordinate(pclosest);

        if (c<a && c<b)
	    {
            if (a<b)
                return p1;
            else
                return p2;
	    }
        if (c>a && c>b)
	    {
            if (a>b)
                return p1;
            else
                return p2;
	    }

        return pclosest;
    }

    public double squaredDistanceTo(double p[])
    {
        double pclosest[] = closestPoint(p);

        return LinAlg.squaredDistance(p, pclosest);
    }

    public double length()
    {
        return LinAlg.distance(p1, p2);
    }

    public double distanceTo(double p[])
    {
        double pclosest[] = closestPoint(p);

        return LinAlg.distance(p, pclosest, 2);
    }

    public double lengthOfProjectionOnto(GLineSegment2D seg)
    {
        double pp1[]=line.pointOnLineClosestTo(seg.p1);
        double pp2[]=line.pointOnLineClosestTo(seg.p2);

        double a = line.getLineCoordinate(p1);
        double c = line.getLineCoordinate(p2);

        double l0, l1;

        double acmin = Math.min(a,c);
        double acmax = Math.max(a,c);

        l0 = line.getLineCoordinate(pp1);
        l1 = line.getLineCoordinate(pp2);

        if (l0 < acmin && l1 < acmin)
            return 0;

        if (l0 > acmax && l1 > acmax)
            return 0;

        l0 = Math.max(acmin, l0);
        l0 = Math.min(acmax, l0);
        l1 = Math.max(acmin, l1);
        l1 = Math.min(acmax, l1);

        //	System.out.print(Math.abs(l1-l0)+" ");
        return Math.abs(l1-l0);
    }

    public static GLineSegment2D lsqFit(List<double[]> points, double weights[])
    {
        GLine2D gline = GLine2D.lsqFit(points, weights);

        double maxcoord = -Double.MAX_VALUE;
        double mincoord = Double.MAX_VALUE;

        for (double p[] : points) {
            double coord = gline.getLineCoordinate(p);
            maxcoord = Math.max(maxcoord, coord);
            mincoord = Math.min(mincoord, coord);
        }

        return new GLineSegment2D(gline.getPointOfCoordinate(mincoord),
                                  gline.getPointOfCoordinate(maxcoord));
    }

    /** xyweight is a list of 3x1 arrays that will be interpreted as (x,y,weight) **/
    public static GLineSegment2D lsqFitXYW(List<double[]> xyweight)
    {
        GLine2D gline = GLine2D.lsqFitXYW(xyweight);

        double maxcoord = -Double.MAX_VALUE;
        double mincoord = Double.MAX_VALUE;

        for (double p[] : xyweight) {
            double coord = gline.getLineCoordinate(p);
            maxcoord = Math.max(maxcoord, coord);
            mincoord = Math.min(mincoord, coord);
        }

        return new GLineSegment2D(gline.getPointOfCoordinate(mincoord),
                                  gline.getPointOfCoordinate(maxcoord));
    }

}
