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
import edu.umich.eecs.april.jmat.Matrix;

/** A 2D line **/
public class GLine2D implements Cloneable, Serializable
{
    protected static GLine2D YAXIS=new GLine2D(0,1,new double[] {0,0});
    protected static GLine2D XAXIS=new GLine2D(1,0,new double[] {0,0});

    final static long serialVersionUID=1001;

    /** A vector representing the slope of the line **/
    protected double dx, dy;

    /** A point that the line passes through. When normalized, it is
        the point on the line closest to the origin. **/
    double p[];

    // is the slope a vector of length 1?
    boolean normalizedSlope=false;

    // is the point p the point that is on a perpendicular line from the origin?
    boolean normalizedp=false;

    public GLine2D()
    {
        dx=0;
        dy=1;
        p = new double[] {0, 0};
    }

    /** Hint that the object should be optimized for future computation by putting
        the internal representation into a more convenient format. This never affects
        correctness, only performance.
    **/
    public void optimize()
    {
        normalizeSlope();
        normalizeP();
    }

    protected void normalizeSlope()
    {
        if (normalizedSlope)
            return;

        double mag=Math.sqrt(dx*dx+dy*dy);
        dx/=mag;
        dy/=mag;
        normalizedSlope=true;
    }

    /** Create a new line
     * @param dx A change in X corresponding to dy
     * @param dy A change in Y corresponding to dx
     * @param p A point that the line passes through
     **/
    public GLine2D(double dx, double dy, double p[])
    {
        this.dx=dx;
        this.dy=dy;
        this.p=p;
    }

    /** Create a new line
     * @param M the slope
     * @param b the y intercept
     **/
    public GLine2D(double M, double b)
    {
        this.dx=1;
        this.dy=M;
        this.p=new double[] {0, b};
    }

    /** Create a new line through two points. **/
    public GLine2D(double p1[], double p2[])
    {
        dx=p2[0]-p1[0];
        dy=p2[1]-p1[1];

        this.p=p1;
    }

    /** Get the slope of the line **/
    public double getM()
    {
        return dy/dx;
    }

    /** Get the y intercept of the line **/
    public double getB()
    {
        double p[]=intersectionWith(YAXIS);
        return p[1];
    }

    /** Get the coordinate of a point (on this line), with 0
     * corresponding to the point on the line that is perpendicular to
     * a line passing through the origin and the line.  This allows
     * easy computation if one point is between two other points on
     * the line: compute the line coordinate of all three points and
     * test if a<=b<=c. This is implemented by computing the dot product
     * of the vector p with the line's direction unit vector.
     **/
    public double getLineCoordinate(double p[])
    {
        normalizeSlope();
        return (p[0]*dx+p[1]*dy);
    }

    /** The inverse of getLineCoordinate. **/
    public double[] getPointOfCoordinate(double coord)
    {
        Matrix R=getR();

        normalizeSlope();

        return new double[] { R.get(0,0) + coord*dx,
                              R.get(1,0) + coord*dy };
    }

    /** Create a line from the vector from the origin to the line that
     * is perpendicular to the line.
     * @param R a 2x1 matrix [x y]'
     **/
    public static GLine2D fromRmatrix(Matrix R)
    {
        double p[] = new double[] {R.get(0,0), R.get(1,0) };

        double dx=-R.get(1,0);
        double dy=R.get(0,0);

        return new GLine2D(dx, dy, p);
    }

    /** Create a new line given a distance and angle from the origin
     * that is perpendicular to the line.
     **/
    public static GLine2D fromRTheta(double r, double theta)
    {
        double x=r*Math.cos(theta);
        double y=r*Math.sin(theta);

        double M=-1/(y/x);
        double b=y-M*x;

        return new GLine2D(M,b);
    }

    /** Create a line that is at angle theta from the x axis and passes
     * through point p
     **/
    public static GLine2D fromThetaPoint(double theta, double p[])
    {
        return new GLine2D(Math.cos(theta), Math.sin(theta), p);
    }

    /** A line perpendicular to this line.
     **/
    public GLine2D perpendicularLine()
    {
        return new GLine2D(-dy, dx, p);
    }

    protected void normalizeP()
    {
        if (normalizedp)
            return;

        normalizeSlope();

        // we already have a point (P) on the line, and we know the line vector U and it's perpendicular vector V:
        // so, P'=P.*V *V
        double dotprod=-dy*p[0] + dx*p[1];

        p = new double[] {-dy*dotprod, dx*dotprod};

        normalizedp=true;

    }

    /** The 2x1 vector from the origin to the line that is
     * perpendicular to the line.
     **/
    public Matrix getR()
    {
        normalizeP();

        Matrix m=new Matrix(2,1);
        m.set(0,0,p[0]);
        m.set(1,0,p[1]);

        return m;
    }

    /** The 2x1 unit vector corresponding to the slope of the line.
     **/
    public Matrix getU()
    {
        Matrix m=new Matrix(2,1);
        m.set(0,0,dx);
        m.set(1,0,dy);

        return m.times(1/m.normF());
    }

    /** Get the angle of the line (the angle between the line and the
        X axis.) Note that a line has two possible values, T and
        T+180.
    **/
    public double getTheta()
    {
        return Math.atan2(dy,dx);
    }

    /** The line perpendicular to this line that passes through point
     * p
     **/
    public GLine2D perpendicularLineThrough(double pin[])
    {
        return new GLine2D(-dy, dx, pin);
    }

    /** Return a line parallel to this line that passes through the
     * specified point.
     **/
    public GLine2D parallelLineThrough(double pin[])
    {
        return new GLine2D(dx, dy, pin);
    }

    /** Compute the point where two lines intersect, or null if the lines are parallel. **/
    public double[] intersectionWith(GLine2D l)
    {
        // this implementation is many times faster than the original,
        // mostly due to avoiding a general-purpose LU decomposition in
        // Matrix.inverse().
        double m00, m01, m10, m11;
        double i00, i01, i10, i11;
        double b00, b10;

        m00=this.dx;
        m01=-l.dx;
        m10=this.dy;
        m11=-l.dy;

        // determinant of m
        double det=m00*m11-m01*m10;

        // parallel lines?
        if (Math.abs(det)<0.0000000001)
	    {
            //		System.out.println("GLine2D.intersectionWith error: Parallel lines");
            return null;
	    }

        // inverse of m
        i00=m11/det;
        i11=m00/det;
        i01=-m01/det;
        i10=-m10/det;

        b00=l.p[0] - p[0];
        b10=l.p[1] - p[1];

        double x00, x10;
        x00=i00*b00+i01*b10;
        //	x10=i10*b00+i11*b10;

        return new double[] {dx*x00+p[0], dy*x00+p[1]};
    }

    public double[] pointOnLineClosestTo(double pin[])
    {
        normalizeSlope();
        normalizeP();

        double dotprod=pin[0]*dx + pin[1]*dy;

        return new double[] {p[0]+dx*dotprod, p[1]+dy*dotprod};
    }

    /** Compute the perpendicular distance between a point and the
     * line
     **/
    public double perpendicularDistanceTo(double pin[])
    {
        double pClosest[]=pointOnLineClosestTo(pin);
        return LinAlg.distance(pin, pClosest);
    }

    public double distanceTo(double p[])
    {
        return perpendicularDistanceTo(p);
    }

    public String toString()
    {
        return "{Line through "+p+", ["+dx+","+dy+"]}";
    }

    /** What is the vertical distance between p and the line? **/
    public double getDistY(double p0[])
    {
        double x = p0[0] - p[0];
        double y = p0[1] - p[1];

        double s = x/dx;
        return y - s*dy;
    }

    /** Is point p to the left of the line? **/
    public double getDistX(double p0[])
    {
        double x = p0[0] - p[0];
        double y = p0[1] - p[1];

        double s = y/dx;
        return x - s*dx;
    }

    /** Get component of unit vector **/
    public double getDx()
    {
        normalizeSlope();
        return dx;
    }

    /** Get component of unit vector **/
    public double getDy()
    {
        normalizeSlope();
        return dy;
    }

    /** Get an arbitrary point on the line. **/
    public double[] getPoint()
    {
        return p;
    }

    /** Self tests **/
    public static void main(String[] args)
    {
        GLine2D l=new GLine2D(4, 10); // y=4x+10
        GLine2D lperp=l.perpendicularLineThrough(new double[] {0,0});

        double EPSILON=0.00001;

        assert(Math.abs(lperp.getM()+0.25)<EPSILON);
        assert(Math.abs(lperp.getB())<EPSILON);

        System.out.println(""+l.p+" "+lperp+" "+l.perpendicularDistanceTo(new double[] {4, 9}));

        assert(Math.abs(l.perpendicularDistanceTo(new double[] {4,9})-4.1231056256)<EPSILON);

        double c=l.getLineCoordinate(new double[] {0,0});
        assert(Math.abs(c)<EPSILON);

        c=l.getLineCoordinate(new double[] {2, 18});
        double p[]=l.getPointOfCoordinate(c);
        assert(Math.abs(p[0]-2)<EPSILON);
        assert(Math.abs(p[1]-18)<EPSILON);

        System.out.println("\nAll tests passed. The next assertion should fail.\n");
        assert(false);
    }

    static final double square(double x)
    {
        return x*x;
    }

    public static GLine2D lsqFit(List<double[]> points)
    {
        return lsqFit(points, null);
    }

    public static GLine2D lsqFit(List<double[]> points, double weights[])
    {
        double Cxx=0, Cyy=0, Cxy=0, Ex=0, Ey=0, mXX=0, mYY=0, mXY=0, mX=0, mY=0;
        double n=0;

        int idx = 0;
        for (double tp[] : points) {

            double x = tp[0];
            double y = tp[1];

            double alpha = 1;

            if (weights!=null)
                alpha = weights[idx];

            mY  += y*alpha;
            mX  += x*alpha;
            mYY += y*y*alpha;
            mXX += x*x*alpha;
            mXY += x*y*alpha;
            n   += alpha;

            idx++;
        }

        Ex  = mX/n;
        Ey  = mY/n;
        Cxx = mXX/n - square(mX/n);
        Cyy = mYY/n - square(mY/n);
        Cxy = mXY/n - (mX/n)*(mY/n);

        // find dominant direction via SVD
        double phi = 0.5*Math.atan2(-2*Cxy,(Cyy-Cxx));
        double rho = Ex*Math.cos(phi) + Ey*Math.sin(phi);

        // compute line parameters
        return new GLine2D(-Math.sin(phi), Math.cos(phi), new double[] {Ex, Ey});
    }

    public static GLine2D lsqFitXYW(List<double[]> xyweight)
    {
        double Cxx=0, Cyy=0, Cxy=0, Ex=0, Ey=0, mXX=0, mYY=0, mXY=0, mX=0, mY=0;
        double n=0;

        int idx = 0;
        for (double tp[] : xyweight) {

            double x = tp[0];
            double y = tp[1];
            double alpha = tp[2];

            mY  += y*alpha;
            mX  += x*alpha;
            mYY += y*y*alpha;
            mXX += x*x*alpha;
            mXY += x*y*alpha;
            n   += alpha;

            idx++;
        }

        Ex  = mX/n;
        Ey  = mY/n;
        Cxx = mXX/n - square(mX/n);
        Cyy = mYY/n - square(mY/n);
        Cxy = mXY/n - (mX/n)*(mY/n);

        // find dominant direction via SVD
        double phi = 0.5*Math.atan2(-2*Cxy,(Cyy-Cxx));
        double rho = Ex*Math.cos(phi) + Ey*Math.sin(phi);

        // compute line parameters
        return new GLine2D(-Math.sin(phi), Math.cos(phi), new double[] {Ex, Ey});
    }

    public static class Fitter
    {
        double mXX, mYY, mXY, mX, mY;
        double n;

        double ux, uy; // unit vector of line's direction.

        boolean dirty = true;

        public Fitter()
        {
        }

        public void addPoint(double p[])
        {
            addPoint(p, 1);
        }

        public void addPoint(double p[], double weight)
        {
            double x = p[0], y = p[1];

            mY  += y*weight;
            mX  += x*weight;
            mYY += y*y*weight;
            mXX += x*x*weight;
            mXY += x*y*weight;
            n   += weight;

            dirty = true;
        }

        public void merge(Fitter f)
        {
            mY += f.mY;
            mX += f.mX;
            mYY += f.mYY;
            mXX += f.mXX;
            mXY += f.mXY;
            n += f.n;

            dirty = true;
        }

        public Fitter copy()
        {
            Fitter f = new Fitter();
            f.mY = mY;
            f.mX = mX;
            f.mYY = mYY;
            f.mXX = mXX;
            f.mXY = mXY;
            f.n = n;

            return f;
        }

        static final double sq(double v)
        {
            return v*v;
        }

        protected void update()
        {
            if (!dirty)
                return;

            double Ex  = mX/n;
            double Ey  = mY/n;
            double Cxx = mXX/n - sq(mX/n);
            double Cyy = mYY/n - sq(mY/n);
            double Cxy = mXY/n - (mX/n)*(mY/n);

            // find dominant direction via SVD
            double phi = 0.5*Math.atan2(-2*Cxy,(Cyy-Cxx));
            double rho = Ex*Math.cos(phi) + Ey*Math.sin(phi);

            // compute line parameters
            ux = -Math.sin(phi);
            uy = Math.cos(phi);

            dirty = false;
        }

        public GLine2D getLine()
        {
            update();

            return new GLine2D(ux, uy, new double[] {mX/n, mY/n});
        }

        public double getError()
        {
            update();
            double p1 = mX/n, p2 = mY/n; // point on line.

            double nx = -uy, ny = ux; // line normal

            double err = mXX*nx*nx +
                mX*(-2*p1*nx*nx - 2*p2*ny*nx) +
                mY*(-2*p1*nx*ny - 2*p2*ny*ny) +
                mXY*(2*ny*nx) +
                mYY*ny*ny +
                n*(p1*p1*nx*nx + p2*p2*ny*ny + 2*p1*nx*p2*ny);

            return err/n;
        }
    }
}
