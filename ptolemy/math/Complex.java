// package  ORG.netlib.math.complex;
package ptolemy.math;


import  java.io.Serializable;



/**
* <p>
* @version              1.0 FINAL <br> ALM Fri 29-Aug-97
* <p>
* A Java class for performing complex number arithmetic to <tt>double</tt>
* precision.
*
* <p>
* <center>
*     <Applet
*         name="SeeComplex"
*         code="SeeComplex.class"
*         codebase="classes"
*         width=85%
*         height=85%
*         align="Middle"
*         alt="SeeComplex Applet"
*     >
*         Make yours a Java enabled browser and OS!
*     </applet>
* <p>
* This applet has been adapted<br>from a <a
* href="http://www.pa.uky.edu/~phy211/VecArith/index.html">Vector
* Visualization applet</a> by <a
* href="mailto:Vladimir Sorokin <vsoro00@pop.uky.edu>">Vladimir Sorokin</a>.
* </center>
* <hr>
*
* <p>
* @author               <a HREF="mailto:Alexander Anderson <sandy@almide.demon.co.uk>">Sandy Anderson</a>
* @author               <a HREF="mailto:Priyantha Jayanetti <pidge@eece.maine.edu>">Priyantha Jayanetti</a>
* <p>
* </dl>
* <font color="000080">
* <pre>
* <b>Copyright (c) 1997,  ALMA Services.  All Rights Reserved.</b>
* <br>
*     Permission to  use, copy,  modify, and distribute  this software  and its
* documentation is  hereby granted provided that  this copyright notice appears
* in all copies and that all modifications are clearly marked.
* <br>
*     THE AUTHORS MAKE NO  REPRESENTATIONS OR WARRANTIES  ABOUT THE SUITABILITY
* OF THE SOFTWARE,  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
* IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE,  OR
* NON-INFRINGEMENT.   THE AUTHORS SHALL NOT BE LIABLE FOR  ANY DAMAGES SUFFERED
* BY LICENSEE AS A RESULT OF USING,  MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
* ITS DERIVATIVES.
* <br>
* Last change:  ALM  29 Aug 97    2:13 am
* </pre>
* </font>
* <p>
* <dd>The latest version of this <tt>Complex</tt> class is available from
*     the <a href="http://www.netlib.org/">Netlib Repository</a>.
* </dd>
* <p>
* <dd>Here's an example of the style the class permits:<br>
*
* <pre>
*         <b>import</b>  ORG.netlib.math.complex.Complex;<br>
*         <b>public</b> <b>class</b> Test {<br>
*             <b>public boolean</b> isInMandelbrot (Complex c, <b>int</b> maxIter) {
*                 Complex z= <b>new</b> Complex(0, 0);<br>
*                 <b>for</b> (<b>int</b> i= 0; i < maxIter; i++) {
*                     z= z.mul(z).add(c);
*                     <b>if</b> (z.abs() > 2) <b>return false</b>;
*                 }<br>
*                 <b>return true</b>;
*             }<br>
*         }
* </pre>
* </dd>
* <p>
* <dd>This class was developed by
*     <a HREF="http://www.almide.demon.co.uk">Sandy Anderson</a> at the
*     <a HREF="http://www.mdx.ac.uk/www/see/opsee.htm">School of Electronic Engineering</a>,
*     <a HREF="http://www.mdx.ac.uk/">Middlesex University</a>, UK, and
*     <a HREF="http://www2.eece.maine.edu/~pidge/index.html">Priyantha Jayanetti</a> at
*     <a HREF="http://www.eece.maine.edu/Power/">The Power Systems Program</a>, the
*     <a HREF="http://www.ume.maine.edu/">University of Maine</a>, USA.
* </dd>
* <p>
* <dd>And many, many thanks to <a href="mailto:R.D.Hirsch@qmw.ac.uk">Mr. Daniel
*     Hirsch</a>, for his constant advice on the mathematics, his exasperating
*     ability to uncover bugs blindfold, and for his persistent badgering over
*     the exact wording of this documentation.
* </dd>
* <p>
* <dd>For instance, he starts to growl like a badger if you say "infinite set".</dd><br>
* <dd>"Grrr...What's <i>that</i> mean?  <i>Countably</i> infinite?"</dd><br>
* <dd>You think for a while.</dd><br>
* <dd>"Grrr..."</dd><br>
* <dd>"Yes."</dd><br>
* <dd>"Ah! Then you mean <i>infinitely many</i>."</dd><br>
* <p>
**/

public class
Complex implements Cloneable, Serializable {

    public    static final String          VERSION             =  "1.0 FINAL alm";
    public    static final String          DATE                =  "Fri 29-Jul-97";
    public    static final String          AUTHOR              =  "sandy@almide.demon.co.uk";
    public    static final String          REMARK              =  "Class available from http://www.netlib.org/";

    /**
    * Switches on debugging information.
    * <p>
    **/
    // protected static       boolean         debug               =  false;

    /**
    * Whilst debugging:  the nesting level when tracing method calls.
    * <p>
    **/
    // private   static       int             trace_nesting       =  0;

    /**
    * Twice <a
    * href="http://cad.ucla.edu/repository/useful/PI.txt"><tt><b>PI</b></tt></a>
    * radians is the same thing as 360 degrees.
    * <p>
    **/
    protected static final double          TWO_PI              =  2.0 * Math.PI;

    /**
    * A constant representing <i><b>i</b></i>, the famous square root of
    * <i>-1</i>.
    * <p>
    * The other square root of <i>-1</i> is - <i><b>i</b></i>.
    * <p>
    **/
    public    static final Complex         i                   =  new Complex(0.0, 1.0);

    // private   static final Complex         one                 =  new Complex(1.0, 0.0);

    // private   static       double          epsilon             =  Math.abs(1.0e-5);

    // private   static       long            objectCount;                        // !!!


    private                double          re;
    private                double          im;



    //---------------------------------//
    //           CONSTRUCTORS          //
    //---------------------------------//



    /**
    * Constructs a <tt>Complex</tt> representing the number zero.
    *
    * <p>
    **/

    public Complex () {
        this(0.0, 0.0);
    }//end Complex()



    /**
    * Constructs a <tt>Complex</tt> representing a real number.
    *
    * <p>
    * @param  re               The real number
    * <p>
    * @see                     Complex#real(double)
    **/

    public Complex (double re) {
        this(re, 0.0);
    }//end Complex(double)



    /**
    * Constructs a separate new <tt>Complex</tt> from an existing
    * <tt>Complex</tt>.
    *
    * <p>
    * @param  z                A <tt>Complex</tt> number
    * <p>
    **/

    public Complex (Complex z) {
        this(z.re, z.im);
    }//end Complex(Complex)



    /**
    * Constructs a <tt>Complex</tt> from real and imaginary parts.
    *
    * <p>
    * <i><b>Note:</b><ul> <font color="000080">All methods in class
    * <tt>Complex</tt> which deliver a <tt>Complex</tt> are written such that
    * no intermediate <tt>Complex</tt> objects get generated.  This means that
    * you can easily anticipate the likely effects on garbage collection caused
    * by your own coding.</font>
    * </ul></i>
    * <p>
    * @param  re               Real part
    * @param  im               Imaginary part
    * <p>
    * @see                     Complex#cart(double, double)
    * @see                     Complex#polar(double, double)
    **/

    public Complex (double re, double im) {
        this.re =  re;
        this.im =  im;

        // if (debug) System.out.println(indent(trace_nesting) + "new Complex, #" + (++objectCount));// !!!
    }//end Complex(double,double)



    //---------------------------------//
    //              DEBUG              //
    //---------------------------------//



    /*

    // BETA Debugging methods...

    private static void
    entering (String what) {
        System.out.print(indent(trace_nesting) + what);
        trace_nesting++;
    }//end entering(String)

    private static void
    enter (String what, double param1, double param2) {
        entering(what);
        System.out.println("(" + param1 + ", " + param2 + ") ");
    }//end enter(String,double,double)

    private static void
    enter (String what, double param) {
        entering(what);
        System.out.println("(" + param + ") ");
    }//end enter(String,double)

    private static void
    enter (String what, Complex z) {
        entering(what);
        System.out.println("(" + z + ") ");
    }//end enter(String,ORG.netlib.math.complex.Complex)

    private static void
    enter (String what, Complex z1, Complex z2) {
        entering(what);
        System.out.println("(" + z1 + ", " + z2 + ") ");
    }//end enter(String,Complex,ORG.netlib.math.complex.Complex)

    private static void
    enter (String what, Complex z, double x) {
        entering(what);
        System.out.println("(" + z + ", " + x + ") ");
    }//end enter(String,Complex,double)

    private static void
    enter (String what, Complex z, double x, double y) {
        entering(what);
        System.out.println("(" + z + ", " + cart(x, y) + ") ");
    }//end enter(String,Complex,double)

    private static void
    enter (String what, Complex z1, Complex z2, double x) {
        entering(what);
        System.out.println("(" + z1 + ", " + z2 + ", " + x + ") ");
    }//end enter(String,Complex,Complex,double)

    private static void
    leaving (String what) {
        trace_nesting--;
        System.out.print(indent(trace_nesting) + "is ");
    }//end leaving(String)

    private static void
    leave (String what, boolean result) {
        leaving(what);
        System.out.println(result);
    }//end leave(String,boolean)

    private static void
    leave (String what, double result) {
        leaving(what);
        System.out.println(result);
    }//end leave(String,double)

    private static void
    leave (String what, Complex result) {
        leaving(what);
        System.out.println(result);
    }//end leave(String,ORG.netlib.math.complex.Complex)

    private static String
    indent (int nesting) {
        StringBuffer indention =  new StringBuffer("");

        for (int i =  0; i < nesting; i++) {
            indention.append("    ");
        }//endfor

        return  indention.toString();
    }//end indent(int)

    */



    /**
    * Useful for checking up on the exact version.
    *
    * <p>
    **/

    public static void
    main (String[] args) {
        System.out.println();
        System.out.println("Module : " + Complex.class.getName());
        System.out.println("Version: " + Complex.VERSION);
        System.out.println("Date   : " + Complex.DATE);
        System.out.println("Author : " + Complex.AUTHOR);
        System.out.println("Remark : " + Complex.REMARK);
        System.out.println();
        System.out.println("Hint:  use TestComplex to test the class.");
        System.out.println();
    }//end main(String[])



    //---------------------------------//
    //             STATIC              //
    //---------------------------------//



    /**
    * Returns a <tt>Complex</tt> representing a real number.
    *
    * <p>
    * @param  real             The real number
    * <p>
    * @return                  <tt>Complex</tt> representation of the real
    * <p>
    * @see                     Complex#re()
    * @see                     Complex#cart(double, double)
    **/

    public static Complex
    real (double real) {
        return  new Complex(real, 0.0);
    }//end real(double)



    /**
    * Returns a <tt>Complex</tt> from real and imaginary parts.
    *
    * <p>
    * @param  re               Real part
    * @param  im               Imaginary part
    * <p>
    * @return                  <tt>Complex</tt> from Cartesian coordinates
    * <p>
    * @see                     Complex#re()
    * @see                     Complex#im()
    * @see                     Complex#polar(double, double)
    * @see                     Complex#toString()
    **/

    public static Complex
    cart (double re, double im) {
        return  new Complex(re, im);
    }//end cart(double,double)



    /**
    * Returns a <tt>Complex</tt> from a size and direction.
    *
    * <p>
    * @param  r                Size
    * @param  theta            Direction (in <i>radians</i>)
    * <p>
    * @return                  <tt>Complex</tt> from Polar coordinates
    * <p>
    * @see                     Complex#abs()
    * @see                     Complex#arg()
    * @see                     Complex#cart(double, double)
    **/

    public static Complex
    polar (double r, double theta) {
        if (r < 0.0) {
            theta +=  Math.PI;
            r      =  -r;
        }//endif

        theta =  theta % TWO_PI;

        return  cart(r * Math.cos(theta), r * Math.sin(theta));
    }//end polar(double,double)



    /**
    * Returns the <tt>Complex</tt> base raised to the power of the exponent.
    *
    * <p>
    * @param  base             The base "to raise"
    * @param  exponent         The exponent "by which to raise"
    * <p>
    * @return                  base "raised to the power of" exponent
    * <p>
    * @see                     Complex#pow(double, ORG.netlib.math.complex.Complex)
    **/

    public static Complex
    pow (Complex base, double exponent) {
        // return  base.log().scale(exponent).exp();

        double re =  exponent * Math.log(base.abs());
        double im =  exponent * base.arg();

        double scalar =  Math.exp(re);

        return  cart( scalar * Math.cos(im), scalar * Math.sin(im) );
    }//end pow(Complex,double)



    /**
    * Returns the base raised to the power of the <tt>Complex</tt> exponent.
    *
    * <p>
    * @param  base             The base "to raise"
    * @param  exponent         The exponent "by which to raise"
    * <p>
    * @return                  base "raised to the power of" exponent
    * <p>
    * @see                     Complex#pow(ORG.netlib.math.complex.Complex, ORG.netlib.math.complex.Complex)
    * @see                     Complex#exp()
    **/

    public static Complex
    pow (double base, Complex exponent) {
        // return  real(base).log().mul(exponent).exp();

        double re =  Math.log(Math.abs(base));
        double im =  Math.atan2(0.0, base);

        double re2 =  (re*exponent.re) - (im*exponent.im);
        double im2 =  (re*exponent.im) + (im*exponent.re);

        double scalar =  Math.exp(re2);

        return  cart( scalar * Math.cos(im2), scalar * Math.sin(im2) );
    }//end pow(double,Complex)



    /**
    * Returns the <tt>Complex</tt> base raised to the power of the <tt>Complex</tt> exponent.
    *
    * <p>
    * @param  base             The base "to raise"
    * @param  exponent         The exponent "by which to raise"
    * <p>
    * @return                  base "raised to the power of" exponent
    * <p>
    * @see                     Complex#pow(ORG.netlib.math.complex.Complex, double)
    * @see                     Complex#pow(ORG.netlib.math.complex.Complex)
    **/

    public static Complex
    pow (Complex base, Complex exponent) {
        // return  base.log().mul(exponent).exp();

        double re =  Math.log(base.abs());
        double im =  base.arg();

        double re2 =  (re*exponent.re) - (im*exponent.im);
        double im2 =  (re*exponent.im) + (im*exponent.re);

        double scalar =  Math.exp(re2);

        return  cart( scalar * Math.cos(im2), scalar * Math.sin(im2) );
    }//end pow(Complex,Complex)



    //---------------------------------//
    //             PUBLIC              //
    //---------------------------------//



    /**
    * Returns <tt>true</tt> if either the real or imaginary component of this
    * <tt>Complex</tt> is an infinite value.
    *
    * <p>
    * @return                  <tt>true</tt> if either component of the <tt>Complex</tt> object is infinite; <tt>false</tt>, otherwise.
    * <p>
    **/

    public boolean
    isInfinite () {
        return  ( Double.isInfinite(re) || Double.isInfinite(im) );
    }//end isInfinite()



    /**
    * Returns <tt>true</tt> if either the real or imaginary component of this
    * <tt>Complex</tt> is a Not-a-Number (<tt>NaN</tt>) value.
    *
    * <p>
    * @return                  <tt>true</tt> if either component of the <tt>Complex</tt> object is <tt>NaN</tt>; <tt>false</tt>, otherwise.
    * <p>
    **/

    public boolean
    isNaN () {
        return  ( Double.isNaN(re) || Double.isNaN(im) );
    }//end isNaN()



    /**
    * Decides if two <tt>Complex</tt> numbers are "sufficiently" alike to be
    * considered equal.
    *
    * <p>
    * <tt>tolerance</tt> is the maximum magnitude of the difference between
    * them before they are considered <i>not</i> equal.
    * <p>
    * Checking for equality between two real numbers on computer hardware is a
    * tricky business.  Try
    * <p>
    * <pre>    System.out.println((1.0/3.0 * 3.0));</pre>
    * <p>
    * and you'll see the nature of the problem!  It's just as tricky with
    * <tt>Complex</tt> numbers.
    * <p>
    * Realize that because of these complications, it's possible to find that
    * the magnitude of one <tt>Complex</tt> number <tt>a</tt> is less than
    * another, <tt>b</tt>, and yet <tt>a.equals(b, myTolerance)</tt> returns
    * <tt>true</tt>.  Be aware!
    * <p>
    * @param  z                The <tt>Complex</tt> to compare with
    * @param  tolerance        The tolerance for equality
    * <p>
    * @return                  <tt>true</tt>, or <tt>false</tt>
    * <p>
    **/

    public boolean
    equals (Complex z, double tolerance) {
        // still true when _equal_ to tolerance? ...
        return  abs(re - z.re, im -z.im) <= Math.abs(tolerance);
        // ...and tolerance is always non-negative
    }//end equals(Complex,double)



    /**
    * Extracts the real part of a <tt>Complex</tt> as a <tt>double</tt>.
    *
    * <p>
    * <pre>
    *     re(x + <i><b>i</b></i>*y)  =  x
    * </pre>
    * <p>
    * @return                  The real part
    * <p>
    * @see                     Complex#im()
    * @see                     Complex#cart(double, double)
    * @see                     Complex#real(double)
    **/

    public double
    re () {
        return  re;
    }//end re()



    /**
    * Extracts the imaginary part of a <tt>Complex</tt> as a <tt>double</tt>.
    *
    * <p>
    * <pre>
    *     im(x + <i><b>i</b></i>*y)  =  y
    * </pre>
    * <p>
    * @return                  The imaginary part
    * <p>
    * @see                     Complex#re()
    * @see                     Complex#cart(double, double)
    **/

    public double
    im () {
        return  im;
    }//end im()



    /**
    * Returns the square of the "length" of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     norm(x + <i><b>i</b></i>*y)  =  x*x + y*y
    * </pre>
    * <p>
    * Always non-negative.
    * <p>
    * @return                  The norm
    * <p>
    * @see                     Complex#abs()
    **/

    public double
    norm () {
        return  (re*re) + (im*im);
    }//end norm()



    /**
    * Returns the magnitude of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     abs(z)  =  sqrt(norm(z))
    * </pre>
    * <p>
    * In other words, it's Pythagorean distance from the origin
    * (<i>0 + 0<b>i</b></i>, or zero).
    * <p>
    * The magnitude is also referred to as the "modulus" or "length".
    * <p>
    * Always non-negative.
    * <p>
    * @return                  The magnitude (or "length")
    * <p>
    * @see                     Complex#arg()
    * @see                     Complex#polar(double, double)
    * @see                     Complex#norm()
    **/

    public double
    abs () {
        return  abs(re, im);
    }//end abs()



    static private double
    abs (double x, double y) {
        //  abs(z)  =  sqrt(norm(z))

        // Adapted from "Numerical Recipes in Fortran 77: The Art of Scientific Computing" (ISBN 0-521-43064-X)

        double absX =  Math.abs(x);
        double absY =  Math.abs(y);

        if (absX == 0.0 && absY == 0.0) {                                      // !!! Numerical Recipes, mmm?
            return  0.0;
        } else if (absX >= absY) {
            double d =  y / x;
            return  absX*Math.sqrt(1.0 + d*d);
        } else {
            double d =  x / y;
            return  absY*Math.sqrt(1.0 + d*d);
        }//endif
    }//end abs()



    /**
    * Returns the <i>principal</i> angle of a <tt>Complex</tt> number, in
    * radians, measured counter-clockwise from the real axis.  (Think of the
    * reals as the x-axis, and the imaginaries as the y-axis.)
    *
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>A</b> is the principal solution of <i>arg(z)</i>, the others are of
    * the form:
    * <p>
    * <pre>
    *     <b>A</b> + 2*k*<b>PI</b>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * <tt>arg()</tt> always returns a <tt>double</tt> between
    * -<tt><b>PI</b></tt> and +<tt><b>PI</b></tt>.
    * <p>
    * <i><b>Note:</b><ul> 2*<tt><b>PI</b></tt> radians is the same as 360 degrees.
    * </ul></i>
    * <p>
    * <i><b>Domain Restrictions:</b><ul> There are no restrictions: the
    * class defines arg(0) to be 0
    * </ul></i>
    * <p>
    * @return                  Principal angle (in radians)
    * <p>
    * @see                     Complex#abs()
    * @see                     Complex#polar(double, double)
    **/

    public double
    arg () {
        return  Math.atan2(im, re);
    }//end arg()



    /**
    * Returns the "negative" of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     neg(a + <i><b>i</b></i>*b)  =  -a - <i><b>i</b></i>*b
    * </pre>
    * <p>
    * The magnitude of the negative is the same, but the angle is flipped
    * through <tt><b>PI</b></tt> (or 180 degrees).
    * <p>
    * @return                  Negative of the <tt>Complex</tt>
    * <p>
    * @see                     Complex#scale(double)
    **/

    public Complex
    neg () {
        return  this.scale(-1.0);
    }//end neg()



    /**
    * Returns the <tt>Complex</tt> "conjugate".
    *
    * <p>
    * <pre>
    *     conj(x + <i><b>i</b></i>*y)  =  x - <i><b>i</b></i>*y
    * </pre>
    * <p>
    * The conjugate appears "flipped" across the real axis.
    * <p>
    * @return                  The <tt>Complex</tt> conjugate
    *<p>
    **/

    public Complex
    conj () {
        return  cart(re, -im);
    }//end conj()



    /**
    * Returns the <tt>Complex</tt> multiplicative inverse.
    *
    * <p>
    * <pre>
    *     inv(z)  =  1 / z
    * </pre>
    * <p>
    * @return                  The <tt>Complex</tt> reciprocal
    *<p>
    **/

/* DEPRECATED !!!
    public Complex
    inv () {
        double scalar =  1.0 / ((re*re)+(im*im));
        return  cart(re*scalar, - im*scalar);
    }//end inv()
/* */


    static private void
    inv (Complex z) {
        double zRe, zIm;
        double scalar;

        if (Math.abs(z.re) >= Math.abs(z.im)) {
            scalar =  1.0 / ( z.re + z.im*(z.im/z.re) );

            zRe =    scalar;
            zIm =    scalar * (- z.im/z.re);
        } else {
            scalar =  1.0 / ( z.re*(z.re/z.im) + z.im );

            zRe =    scalar * (  z.re/z.im);
            zIm =  - scalar;
        }//endif

        z.re = zRe;
        z.im = zIm;
    }//end inv(Complex)



    /**
    * Returns the <tt>Complex</tt> scaled by a real number.
    *
    * <p>
    * <pre>
    *     scale((x + <i><b>i</b></i>*y), s)  =  (x*s + <i><b>i</b></i>*y*s)
    * </pre>
    * <p>
    * Scaling by the real number <i>2.0</i>, doubles the magnitude, but leaves
    * the <tt>arg()</tt> unchanged.  Scaling by <i>-1.0</i> keeps the magnitude
    * the same, but flips the <tt>arg()</tt> by <tt><b>PI</b></tt> (180 degrees).
    * <p>
    * @param  scalar           A real number scale factor
    * <p>
    * @return                  <tt>Complex</tt> scaled by a real number
    * <p>
    * @see                     Complex#mul(ORG.netlib.math.complex.Complex)
    * @see                     Complex#div(ORG.netlib.math.complex.Complex)
    * @see                     Complex#neg()
    **/

    public Complex
    scale (double scalar) {
        return  cart(scalar*re, scalar*im);
    }//end scale(double)



    /**
    * To perform z1 + z2, you write <tt>z1.add(z2)</tt> .
    *
    * <p>
    * <pre>
    *     (a + <i><b>i</b></i>*b) + (c + <i><b>i</b></i>*d)  =  ((a+c) + <i><b>i</b></i>*(b+d))
    * </pre>
    * <p>
    **/

    public Complex
    add (Complex z) {
        return  cart(re + z.re, im + z.im);
    }//end add(Complex)



    /**
    * To perform z1 - z2, you write <tt>z1.sub(z2)</tt> .
    *
    * <p>
    * <pre>
    *     (a + <i><b>i</b></i>*b) - (c + <i><b>i</b></i>*d)  =  ((a-c) + <i><b>i</b></i>*(b-d))
    * </pre>
    * <p>
    **/

    public Complex
    sub (Complex z) {
        return  cart(re - z.re, im - z.im);
    }//end sub(Complex)



    /**
    * To perform z1 * z2, you write <tt>z1.mul(z2)</tt> .
    *
    * <p>
    * <pre>
    *     (a + <i><b>i</b></i>*b) * (c + <i><b>i</b></i>*d)  =  ( (a*c) - (b*d) + <i><b>i</b></i>*((a*d) + (b*c)) )
    * </pre>
    * <p>
    * @see                     Complex#scale(double)
    **/

    public Complex
    mul (Complex z) {
        return  cart( (re*z.re) - (im*z.im), (re*z.im) + (im*z.re) );
        // return  cart( (re*z.re) - (im*z.im), (re + im)*(z.re + z.im) - re*z.re - im*z.im);
    }//end mul(Complex)



    /**
    * To perform z1 / z2, you write <tt>z1.div(z2)</tt> .
    *
    * <p>
    * <pre>
    *     (a + <i><b>i</b></i>*b) / (c + <i><b>i</b></i>*d)  =  ( (a*c) + (b*d) + <i><b>i</b></i>*((b*c) - (a*d)) ) / norm(c + <i><b>i</b></i>*d)
    * </pre>
    * <p>
    * <i><b>Take care not to divide by zero!</b></i>
    * <p>
    * <i><b>Note:</b><ul> <tt>Complex</tt> arithmetic in Java never causes
    * exceptions.  You have to deliberately check for overflow, division by
    * zero, and so on, <u>for yourself</u>.
    * </ul></i>
    * <p>
    * <i><b>Domain Restrictions:</b><ul> z1/z2 is undefined if z2 = 0
    * </ul></i>
    * <p>
    * @see                     Complex#scale(double)
    **/

    public Complex
    div (Complex z) {
        Complex result =  new Complex(this);
        div(result, z.re, z.im);
        return  result;
    }//end div(Complex)



    static private void
    div (Complex z, double x, double y) {
         // Adapted from "Numerical Recipes in Fortran 77: The Art of Scientific Computing" (ISBN 0-521-43064-X)

        double zRe, zIm;
        double scalar;

        if (Math.abs(x) >= Math.abs(y)) {
            scalar =  1.0 / ( x + y*(y/x) );

            zRe =  scalar * (z.re + z.im*(y/x));
            zIm =  scalar * (z.im - z.re*(y/x));

        } else {
            scalar =  1.0 / ( x*(x/y) + y );

            zRe =  scalar * (z.re*(x/y) + z.im);
            zIm =  scalar * (z.im*(x/y) - z.re);
        }//endif

        z.re = zRe;
        z.im = zIm;
    }//end div(Complex,double,double)



    /**
    * Returns a <tt>Complex</tt> representing one of the two square roots.
    *
    * <p>
    * <pre>
    *     sqrt(z)  =  sqrt(abs(z)) * ( cos(arg(z)/2) + <i><b>i</b></i> * sin(arg(z)/2) )
    * </pre>
    * <p>
    * For any <i>complex</i> number <i>z</i>, <i>sqrt(z)</i> will return the
    * <i>complex</i> root whose <i>arg</i> is <i>arg(z)/2</i>.
    * <p>
    * <i><b>Note:</b><ul> There are always two square roots for each
    * <tt>Complex</tt> number, except for 0 + 0<b>i</b>, or zero.  The other
    * root is the <tt>neg()</tt> of the first one.  Just as the two roots of
    * 4 are 2 and -2, the two roots of -1 are <b>i</b> and - <b>i</b>.
    * </ul></i>
    * <p>
    * @return                  The square root whose <i>arg</i> is <i>arg(z)/2</i>.
    * <p>
    * @see                     Complex#pow(ORG.netlib.math.complex.Complex, double)
    **/

    public Complex
    sqrt () {
        Complex result =  new Complex(this);
        sqrt(result);
        return  result;
    }//end sqrt()


    static private void
    sqrt (Complex z) {
        // Jim Shapiro <jnshapi@argo.ecte.uswc.uswest.com>
        // adapted from "Numerical Recipies in C" (ISBN 0-521-43108-5) by William H. Press et al

        double u   =  0.0;
        double v   =  0.0;
        double mag =  z.abs();

        if (mag > 0.0) {
            if (z.re > 0.0) {
                double temp =  Math.sqrt(0.5 * (mag + z.re));

                z.re =  temp;
                z.im =  0.5 * z.im / temp;
            } else {
                double temp =  Math.sqrt(0.5 * (mag - z.re));

                if (z.im < 0.0) {
                    temp =  -temp;
                }//endif

                z.re =  0.5 * z.im / temp;
                z.im =  temp;
            }//endif
        } else {
            z.re =  0.0;
            z.im =  0.0;
        }//endif
    }//end sqrt(Complex)



    /**
    * Returns this <tt>Complex</tt> raised to the power of a <tt>Complex</tt> exponent.
    *
    * <p>
    * @param  exponent         The exponent "by which to raise"
    * <p>
    * @return                  this <tt>Complex</tt> "raised to the power of" the exponent
    * <p>
    * @see                     Complex#pow(ORG.netlib.math.complex.Complex, ORG.netlib.math.complex.Complex)
    **/

    public Complex
    pow (Complex exponent) {
        return  Complex.pow(this, exponent);
    }//end pow(Complex)



    /**
    * Returns the number <i><b>e</b></i> "raised to" a <tt>Complex</tt> power.
    *
    * <p>
    * <pre>
    *     exp(x + <i><b>i</b></i>*y)  =  exp(x) * ( cos(y) + <i><b>i</b></i> * sin(y) )
    * </pre>
    * <p>
    * <i><b>Note:</b><ul> The value of <i><b>e</b></i>, a transcendental number, is
    * roughly 2.71828182846...
    * <p>
    *
    * Also, the following is quietly amazing:
    * <pre>
    *     <i><b>e</b></i><sup><font size=+0><b>PI</b>*<i><b>i</b></i></font></sup>    =    - 1
    * </pre>
    * </ul>
    * </i>
    * <p>
    * @return                  <i><b>e</b></i> "raised to the power of" this <tt>Complex</tt>
    * <p>
    * @see                     Complex#log()
    * @see                     Complex#pow(double, ORG.netlib.math.complex.Complex)
    **/

    public Complex
    exp () {
        double scalar =  Math.exp(re);                                         // e^ix = cis x
        return  cart( scalar * Math.cos(im), scalar * Math.sin(im) );
    }//end exp()



    /**
    * Returns the <i>principal</i> natural logarithm of a <tt>Complex</tt>
    * number.
    *
    * <p>
    * <pre>
    *     log(z)  =  log(abs(z)) + <i><b>i</b></i> * arg(z)
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>L</b> is the principal solution of <i>log(z)</i>, the others are of
    * the form:
    * <p>
    * <pre>
    *     <b>L</b> + (2*k*<b>PI</b>)*<i><b>i</b></i>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * @return                  Principal <tt>Complex</tt> natural logarithm
    * <p>
    * @see                     Complex#exp()
    **/

    public Complex
    log () {
        return  cart( Math.log(this.abs()), this.arg() );                      // principal value
    }//end log()



    /**
    * Returns the <i>principal</i> logarithm (<i>base 10</i>) of a
    * <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     log10(z)  =  log(z) / log(10)
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>L</b> is the principal solution of <i>log10(z)</i>, the others are
    * of the form:
    * <p>
    * <pre>
    *     <b>L</b> + (2*k*<b>PI</b>)*<i><b>i</b></i>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * @return                  Principal <tt>Complex</tt> logarithm (base 10)
    * <p>
    * @see                     Complex#exp()
    * @see                     Complex#log()
    **/
/* DEPRECATED !!!
    public Complex
    log10 () {
        Complex result;
        // if (debug) enter("log10", this);

            double scalar =  1.0/Math.log(10.0);

            // result =  this.log().scale(scalar);

            result =  cart( scalar * Math.log(this.abs()), scalar * this.arg() );

        // if (debug) leave("log10", result);
        return  result;
    }//end log10()
/* */


    /**
    * Returns the sine of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     sin(z)  =  ( exp(<i><b>i</b></i>*z) - exp(-<i><b>i</b></i>*z) ) / (2*<i><b>i</b></i>)
    * </pre>
    * <p>
    * @return                  The <tt>Complex</tt> sine
    * <p>
    * @see                     Complex#asin()
    * @see                     Complex#sinh()
    * @see                     Complex#cosec()
    * @see                     Complex#cos()
    * @see                     Complex#tan()
    **/

    public Complex
    sin () {
        Complex result;
            //  sin(z)  =  ( exp(i*z) - exp(-i*z) ) / (2*i)

            double scalar;
            double iz_re, iz_im;
            double _re1, _im1;
            double _re2, _im2;

            // iz:      i.mul(z) ...
            iz_re =  -im;
            iz_im =   re;

            // _1:      iz.exp() ...
            scalar =  Math.exp(iz_re);
            _re1 =  scalar * Math.cos(iz_im);
            _im1 =  scalar * Math.sin(iz_im);

            // _2:      iz.neg().exp() ...
            scalar =  Math.exp(-iz_re);
            _re2 =  scalar * Math.cos(-iz_im);
            _im2 =  scalar * Math.sin(-iz_im);

            // _1:      _1.sub(_2) ...
            _re1 = _re1 - _re2;                                                // !!!
            _im1 = _im1 - _im2;                                                // !!!

            // result:  _1.div(2*i) ...
            result =  cart( 0.5*_im1, -0.5*_re1 );
            // result =  cart(_re1, _im1);
            // div(result, 0.0, 2.0);


        return  result;
    }//end sin()



    /**
    * Returns the cosine of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     cos(z)  =  ( exp(<i><b>i</b></i>*z) + exp(-<i><b>i</b></i>*z) ) / 2
    * </pre>
    * <p>
    * @return                  The <tt>Complex</tt> cosine
    * <p>
    * @see                     Complex#acos()
    * @see                     Complex#cosh()
    * @see                     Complex#sec()
    * @see                     Complex#sin()
    * @see                     Complex#tan()
    **/

    public Complex
    cos () {
        Complex result;
            //  cos(z)  =  ( exp(i*z) + exp(-i*z) ) / 2

            double scalar;
            double iz_re, iz_im;
            double _re1, _im1;
            double _re2, _im2;

            // iz:      i.mul(z) ...
            iz_re =  -im;
            iz_im =   re;

            // _1:      iz.exp() ...
            scalar =  Math.exp(iz_re);
            _re1 =  scalar * Math.cos(iz_im);
            _im1 =  scalar * Math.sin(iz_im);

            // _2:      iz.neg().exp() ...
            scalar =  Math.exp(-iz_re);
            _re2 =  scalar * Math.cos(-iz_im);
            _im2 =  scalar * Math.sin(-iz_im);

            // _1:      _1.add(_2) ...
            _re1 = _re1 + _re2;                                                // !!!
            _im1 = _im1 + _im2;                                                // !!!

            // result:  _1.scale(0.5) ...
            result =  cart( 0.5 * _re1, 0.5 * _im1 );
        return  result;
    }//end cos()



    /**
    * Returns the tangent of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     tan(z)  =  sin(z) / cos(z)
    * </pre>
    * <p>
    * <i><b>Domain Restrictions:</b><ul> tan(z) is undefined whenever z = (k + 1/2) * <tt><b>PI</b></tt><br>
    * where k is any integer
    * </ul></i>
    * <p>
    * @return                  The <tt>Complex</tt> tangent
    * <p>
    * @see                     Complex#atan()
    * @see                     Complex#tanh()
    * @see                     Complex#cot()
    * @see                     Complex#sin()
    * @see                     Complex#cos()
    **/

    public Complex
    tan () {
        Complex result;
            //  tan(z)  =  sin(z) / cos(z)

            double scalar;
            double iz_re, iz_im;
            double _re1, _im1;
            double _re2, _im2;
            double _re3, _im3;

            double cs_re, cs_im;

            // sin() ...

            // iz:      i.mul(z) ...
            iz_re =  -im;
            iz_im =   re;

            // _1:      iz.exp() ...
            scalar =  Math.exp(iz_re);
            _re1 =  scalar * Math.cos(iz_im);
            _im1 =  scalar * Math.sin(iz_im);

            // _2:      iz.neg().exp() ...
            scalar =  Math.exp(-iz_re);
            _re2 =  scalar * Math.cos(-iz_im);
            _im2 =  scalar * Math.sin(-iz_im);

            // _3:      _1.sub(_2) ...
            _re3 = _re1 - _re2;
            _im3 = _im1 - _im2;

            // result:  _3.div(2*i) ...
            result =  cart( 0.5*_im3, -0.5*_re3 );
            // result =  cart(_re3, _im3);
            // div(result, 0.0, 2.0);

            // cos() ...

            // _3:      _1.add(_2) ...
            _re3 = _re1 + _re2;
            _im3 = _im1 + _im2;

            // cs:      _3.scale(0.5) ...
            cs_re =  0.5 * _re3;
            cs_im =  0.5 * _im3;

            // result:  result.div(cs) ...
            div(result, cs_re, cs_im);
        return  result;
    }//end tan()



    /**
    * Returns the cosecant of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     cosec(z)  =  1 / sin(z)
    * </pre>
    * <p>
    * <i><b>Domain Restrictions:</b><ul> cosec(z) is undefined whenever z = k * <tt><b>PI</b></tt><br>
    * where k is any integer
    * </ul></i>
    * <p>
    * @return                  The <tt>Complex</tt> cosecant
    * <p>
    * @see                     Complex#sin()
    * @see                     Complex#sec()
    * @see                     Complex#cot()
    **/

    public Complex
    cosec () {
        Complex result;
            //  cosec(z)  =  1 / sin(z)

            double scalar;
            double iz_re, iz_im;
            double _re1, _im1;
            double _re2, _im2;

            // iz:      i.mul(z) ...
            iz_re =  -im;
            iz_im =   re;

            // _1:      iz.exp() ...
            scalar =  Math.exp(iz_re);
            _re1 =  scalar * Math.cos(iz_im);
            _im1 =  scalar * Math.sin(iz_im);

            // _2:      iz.neg().exp() ...
            scalar =  Math.exp(-iz_re);
            _re2 =  scalar * Math.cos(-iz_im);
            _im2 =  scalar * Math.sin(-iz_im);

            // _1:      _1.sub(_2) ...
            _re1 = _re1 - _re2;                                                // !!!
            _im1 = _im1 - _im2;                                                // !!!

            // _result: _1.div(2*i) ...
            result =  cart( 0.5*_im1, -0.5*_re1 );
            // result =  cart(_re1, _im1);
            // div(result, 0.0, 2.0);

            // result:  one.div(_result) ...
            inv(result);
        return  result;
    }//end cosec()



    /**
    * Returns the secant of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     sec(z)  =  1 / cos(z)
    * </pre>
    * <p>
    * <i><b>Domain Restrictions:</b><ul> sec(z) is undefined whenever z = (k + 1/2) * <tt><b>PI</b></tt><br>
    * where k is any integer
    * </ul></i>
    * <p>
    * @return                  The <tt>Complex</tt> secant
    * <p>
    * @see                     Complex#cos()
    * @see                     Complex#cosec()
    * @see                     Complex#cot()
    **/

    public Complex
    sec () {
        Complex result;
            //  sec(z)  =  1 / cos(z)

            double scalar;
            double iz_re, iz_im;
            double _re1, _im1;
            double _re2, _im2;

            // iz:      i.mul(z) ...
            iz_re =  -im;
            iz_im =   re;

            // _1:      iz.exp() ...
            scalar =  Math.exp(iz_re);
            _re1 =  scalar * Math.cos(iz_im);
            _im1 =  scalar * Math.sin(iz_im);

            // _2:      iz.neg().exp() ...
            scalar =  Math.exp(-iz_re);
            _re2 =  scalar * Math.cos(-iz_im);
            _im2 =  scalar * Math.sin(-iz_im);

            // _1:      _1.add(_2) ...
            _re1 = _re1 + _re2;
            _im1 = _im1 + _im2;

            // result: _1.scale(0.5) ...
            result =  cart(0.5*_re1, 0.5*_im1);

            // result:  one.div(result) ...
            inv(result);
        return  result;
    }//end sec()



    /**
    * Returns the cotangent of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     cot(z)  =  1 / tan(z)
    * </pre>
    * <p>
    * <i><b>Domain Restrictions:</b><ul> cot(z) is undefined whenever z = k * <tt><b>PI</b></tt><br>
    * where k is any integer
    * </ul></i>
    * <p>
    * @return                  The <tt>Complex</tt> cotangent
    * <p>
    * @see                     Complex#tan()
    * @see                     Complex#cosec()
    * @see                     Complex#sec()
    **/

    public Complex
    cot () {
        Complex result;
            //  cot(z)  =  1 / tan(z)  =  cos(z) / sin(z)

            double scalar;
            double iz_re, iz_im;
            double _re1, _im1;
            double _re2, _im2;
            double _re3, _im3;

            double sn_re, sn_im;

            // cos() ...

            // iz:      i.mul(z) ...
            iz_re =  -im;
            iz_im =   re;

            // _1:      iz.exp() ...
            scalar =  Math.exp(iz_re);
            _re1 =  scalar * Math.cos(iz_im);
            _im1 =  scalar * Math.sin(iz_im);

            // _2:      iz.neg().exp() ...
            scalar =  Math.exp(-iz_re);
            _re2 =  scalar * Math.cos(-iz_im);
            _im2 =  scalar * Math.sin(-iz_im);

            // _3:      _1.add(_2) ...
            _re3 = _re1 + _re2;
            _im3 = _im1 + _im2;

            // result:  _3.scale(0.5) ...
            result =  cart( 0.5*_re3, 0.5*_im3 );

            // sin() ...

            // _3:      _1.sub(_2) ...
            _re3 = _re1 - _re2;
            _im3 = _im1 - _im2;

            // sn:      _3.div(2*i) ...
            sn_re =    0.5 * _im3;                                             // !!!
            sn_im =  - 0.5 * _re3;                                             // !!!

            // result:  result.div(sn) ...
            div(result, sn_re, sn_im);
        return  result;
    }//end cot()



    /**
    * Returns the hyperbolic sine of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     sinh(z)  =  ( exp(z) - exp(-z) ) / 2
    * </pre>
    * <p>
    * @return                  The <tt>Complex</tt> hyperbolic sine
    * <p>
    * @see                     Complex#sin()
    * @see                     Complex#asinh()
    **/

    public Complex
    sinh () {
        Complex result;
            //  sinh(z)  =  ( exp(z) - exp(-z) ) / 2

            double scalar;
            double _re1, _im1;
            double _re2, _im2;

            // _1:      z.exp() ...
            scalar =  Math.exp(re);
            _re1 =  scalar * Math.cos(im);
            _im1 =  scalar * Math.sin(im);

            // _2:      z.neg().exp() ...
            scalar =  Math.exp(-re);
            _re2 =  scalar * Math.cos(-im);
            _im2 =  scalar * Math.sin(-im);

            // _1:      _1.sub(_2) ...
            _re1 = _re1 - _re2;                                                // !!!
            _im1 = _im1 - _im2;                                                // !!!

            // result:  _1.scale(0.5) ...
            result =  cart( 0.5 * _re1, 0.5 * _im1 );
        return  result;
    }//end sinh()



    /**
    * Returns the hyperbolic cosine of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     cosh(z)  =  ( exp(z) + exp(-z) ) / 2
    * </pre>
    * <p>
    * @return                  The <tt>Complex</tt> hyperbolic cosine
    * <p>
    * @see                     Complex#cos()
    * @see                     Complex#acosh()
    **/

    public Complex
    cosh () {
        Complex result;
            //  cosh(z)  =  ( exp(z) + exp(-z) ) / 2

            double scalar;
            double _re1, _im1;
            double _re2, _im2;

            // _1:      z.exp() ...
            scalar =  Math.exp(re);
            _re1 =  scalar * Math.cos(im);
            _im1 =  scalar * Math.sin(im);

            // _2:      z.neg().exp() ...
            scalar =  Math.exp(-re);
            _re2 =  scalar * Math.cos(-im);
            _im2 =  scalar * Math.sin(-im);

            // _1:  _1.add(_2) ...
            _re1 = _re1 + _re2;                                                // !!!
            _im1 = _im1 + _im2;                                                // !!!

            // result:  _1.scale(0.5) ...
            result =  cart( 0.5 * _re1, 0.5 * _im1 );
        return  result;
    }//end cosh()



    /**
    * Returns the hyperbolic tangent of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     tanh(z)  =  sinh(z) / cosh(z)
    * </pre>
    * <p>
    * @return                  The <tt>Complex</tt> hyperbolic tangent
    * <p>
    * @see                     Complex#tan()
    * @see                     Complex#atanh()
    **/

    public Complex
    tanh () {
        Complex result;
            //  tanh(z)  =  sinh(z) / cosh(z)

            double scalar;
            double _re1, _im1;
            double _re2, _im2;
            double _re3, _im3;

            double ch_re, ch_im;

            // sinh() ...

            // _1:      z.exp() ...
            scalar =  Math.exp(re);
            _re1 =  scalar * Math.cos(im);
            _im1 =  scalar * Math.sin(im);

            // _2:      z.neg().exp() ...
            scalar =  Math.exp(-re);
            _re2 =  scalar * Math.cos(-im);
            _im2 =  scalar * Math.sin(-im);

            // _3:      _1.sub(_2) ...
            _re3 =  _re1 - _re2;
            _im3 =  _im1 - _im2;

            // result:  _3.scale(0.5) ...
            result =  cart(0.5*_re3, 0.5*_im3);

            // cosh() ...

            // _3:      _1.add(_2) ...
            _re3 =  _re1 + _re2;
            _im3 =  _im1 + _im2;

            // ch:      _3.scale(0.5) ...
            ch_re =  0.5 * _re3;
            ch_im =  0.5 * _im3;

            // result:  result.div(ch) ...
            div(result, ch_re, ch_im);
        return  result;
    }//end tanh()



    /**
    * Returns the <i>principal</i> arc sine of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     asin(z)  =  -<i><b>i</b></i> * log(<i><b>i</b></i>*z + sqrt(1 - z*z))
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>A</b> is the principal solution of <i>asin(z)</i>, the others are
    * of the form:
    * <p>
    * <pre>
    *     k*<b>PI</b> + (-1)<sup><font size=-1>k</font></sup>  * <b>A</b>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * @return                  Principal <tt>Complex</tt> arc sine
    * <p>
    * @see                     Complex#sin()
    * @see                     Complex#sinh()
    **/

    public Complex
    asin () {
        Complex result;
            //  asin(z)  =  -i * log(i*z + sqrt(1 - z*z))

            double _re1, _im1;

            // _1:      one.sub(z.mul(z)) ...
            _re1 =  1.0 - ( (re*re) - (im*im) );
            _im1 =  0.0 - ( (re*im) + (im*re) );

            // result:  _1.sqrt() ...
            result =  cart(_re1, _im1);
            sqrt(result);

            // _1:      z.mul(i) ...
            _re1 =  - im;
            _im1 =  + re;

            // result:  _1.add(result) ...
            result.re =  _re1 + result.re;
            result.im =  _im1 + result.im;

            // _1:      result.log() ...
            _re1 =  Math.log(result.abs());
            _im1 =  result.arg();

            // result:  i.neg().mul(_1) ...
            result.re =    _im1;
            result.im =  - _re1;
        return  result;
    }//end asin()



    /**
    * Returns the <i>principal</i> arc cosine of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     acos(z)  =  -<i><b>i</b></i> * log( z + <i><b>i</b></i> * sqrt(1 - z*z) )
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>A</b> is the principal solution of <i>acos(z)</i>, the others are
    * of the form:
    * <p>
    * <pre>
    *     2*k*<b>PI</b> +/- <b>A</b>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * @return                  Principal <tt>Complex</tt> arc cosine
    * <p>
    * @see                     Complex#cos()
    * @see                     Complex#cosh()
    **/

    public Complex
    acos () {
        Complex result;
            //  acos(z)  =  -i * log( z + i * sqrt(1 - z*z) )

            double _re1, _im1;

            // _1:      one.sub(z.mul(z)) ...
            _re1 =  1.0 - ( (re*re) - (im*im) );
            _im1 =  0.0 - ( (re*im) + (im*re) );

            // result:  _1.sqrt() ...
            result =  cart(_re1, _im1);
            sqrt(result);

            // _1:      i.mul(result) ...
            _re1 =  - result.im;
            _im1 =  + result.re;

            // result:  z.add(_1) ...
            result.re =  re + _re1;
            result.im =  im + _im1;

            // _1:      result.log()
            _re1 =  Math.log(result.abs());
            _im1 =  result.arg();

            // result:  i.neg().mul(_1) ...
            result.re =    _im1;
            result.im =  - _re1;
        return  result;
    }//end acos()



    /**
    * Returns the <i>principal</i> arc tangent of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     atan(z)  =  -<i><b>i</b></i>/2 * log( (<i><b>i</b></i>-z)/(<i><b>i</b></i>+z) )
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>A</b> is the principal solution of <i>atan(z)</i>, the others are
    * of the form:
    * <p>
    * <pre>
    *     <b>A</b> + k*<b>PI</b>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * <i><b>Domain Restrictions:</b><ul> atan(z) is undefined for z = + <b>i</b> or z = - <b>i</b>
    * </ul></i>
    * <p>
    * @return                  Principal <tt>Complex</tt> arc tangent
    * <p>
    * @see                     Complex#tan()
    * @see                     Complex#tanh()
    **/

    public Complex
    atan () {
        Complex result;
            //  atan(z)  =  -i/2 * log( (i-z)/(i+z) )

            double _re1, _im1;

            // result:  i.sub(z) ...
            result =  cart(- re, 1.0 - im);

            // _1:      i.add(z) ...
            _re1 =  + re;
            _im1 =  1.0 + im;

            // result:  result.div(_1) ...
            div(result, _re1, _im1);

            // _1:      result.log() ...
            _re1 =  Math.log(result.abs());
            _im1 =  result.arg();

            // result:  half_i.neg().mul(_2) ...
            result.re =   0.5*_im1;
            result.im =  -0.5*_re1;
        return  result;
    }//end atan()



    /**
    * Returns the <i>principal</i> inverse hyperbolic sine of a
    * <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     asinh(z)  =  log(z + sqrt(z*z + 1))
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>A</b> is the principal solution of <i>asinh(z)</i>, the others are
    * of the form:
    * <p>
    * <pre>
    *     k*<b>PI</b>*<b><i>i</i></b> + (-1)<sup><font size=-1>k</font></sup>  * <b>A</b>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * @return                  Principal <tt>Complex</tt> inverse hyperbolic sine
    * <p>
    * @see                     Complex#sinh()
    **/

    /*
    * Many thanks to the mathematicians of aus.mathematics and sci.math, and
    * to Zdislav V. Kovarik of the Department of Mathematics and Statistics,
    * McMaster University and John McGowan <jmcgowan@inch.com> in particular,
    * for their advice on the current naming conventions for "area/argumentus 
    * sinus hyperbolicus".
    */

    public Complex
    asinh () {
        Complex result;
            //  asinh(z)  =  log(z + sqrt(z*z + 1))

            double _re1, _im1;

            // _1:      z.mul(z).add(one) ...
            _re1 =  ( (re*re) - (im*im) ) + 1.0;
            _im1 =  ( (re*im) + (im*re) ) + 0.0;

            // result:  _1.sqrt() ...
            result =  cart(_re1, _im1);
            sqrt(result);

            // result:  z.add(result) ...
            result.re =  re + result.re;                                       // !
            result.im =  im + result.im;                                       // !

            // _1:      result.log() ...
            _re1 =  Math.log(result.abs());
            _im1 =  result.arg();

            // result:  _1 ...
            result.re =  _re1;
            result.im =  _im1;
        return  result;
    }//end asinh()



    /**
    * Returns the <i>principal</i> inverse hyperbolic cosine of a
    * <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     acosh(z)  =  log(z + sqrt(z*z - 1))
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>A</b> is the principal solution of <i>acosh(z)</i>, the others are
    * of the form:
    * <p>
    * <pre>
    *     2*k*<b>PI</b>*<b><i>i</i></b> +/- <b>A</b>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * @return                  Principal <tt>Complex</tt> inverse hyperbolic cosine
    * <p>
    * @see                     Complex#cosh()
    **/

    public Complex
    acosh () {
        Complex result;
            //  acosh(z)  =  log(z + sqrt(z*z - 1))

            double _re1, _im1;

            // _1:  z.mul(z).sub(one) ...
            _re1 =  ( (re*re) - (im*im) ) - 1.0;
            _im1 =  ( (re*im) + (im*re) ) - 0.0;

            // result:  _1.sqrt() ...
            result =  cart(_re1, _im1);
            sqrt(result);

            // result:  z.add(result) ...
            result.re =  re + result.re;                                       // !
            result.im =  im + result.im;                                       // !

            // _1:  result.log() ...
            _re1 =  Math.log(result.abs());
            _im1 =  result.arg();

            // result:  _1 ...
            result.re =  _re1;
            result.im =  _im1;
        return  result;
    }//end acosh()



    /**
    * Returns the <i>principal</i> inverse hyperbolic tangent of a
    * <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     atanh(z)  =  1/2 * log( (1+z)/(1-z) )
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>A</b> is the principal solution of <i>atanh(z)</i>, the others are
    * of the form:
    * <p>
    * <pre>
    *     <b>A</b> + k*<b>PI</b>*<b><i>i</i></b>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * <i><b>Domain Restrictions:</b><ul> atanh(z) is undefined for z = + 1 or z = - 1
    * </ul></i>
    * <p>
    * @return                  Principal <tt>Complex</tt> inverse hyperbolic tangent
    * <p>
    * @see                     Complex#tanh()
    **/

    public Complex
    atanh () {
        Complex result;
            //  atanh(z)  =  1/2 * log( (1+z)/(1-z) )

            double _re1, _im1;

            // result:  one.add(z) ...
            result =  cart(1.0 + re, + im);

            // _1:      one.sub(z) ...
            _re1 =  1.0 - re;
            _im1 =  - im;

            // result:  result.div(_1) ...
            div(result, _re1, _im1);

            // _1:      result.log() ...
            _re1 =  Math.log(result.abs());
            _im1 =  result.arg();

            // result:  _1.scale(0.5) ...
            result.re =  0.5 * _re1;
            result.im =  0.5 * _im1;
        return  result;
    }//end atanh()



    /**
    * Converts a <tt>Complex</tt> into a <tt>String</tt> of the form
    * <tt>(</tt><i>a</i><tt> + </tt><i>b</i><tt>i)</tt>.
    *
    * <p>
    * This enables the <tt>Complex</tt> to be easily printed.  For example, if
    * <tt>z</tt> was <i>2 - 5<b>i</b></i>, then
    * <pre>
    *     System.out.println("z = " + z);
    * </pre>
    * would print
    * <pre>
    *     z = (2 - 5i)
    * </pre>
    * <!--
    * <i><b>Note:</b><ul>Concatenating <tt>String</tt>s, using a system
    * overloaded meaning of the "<tt>+</tt>" operator, in fact causes the
    * <tt>toString()</tt> method to be invoked on the object <tt>z</tt> at
    * runtime.</ul></i>
    * -->
    * <p>
    * @return                  <tt>String</tt> containing the cartesian coordinate representation
    * <p>
    * @see                     Complex#cart(double, double)
    **/

    public String
    toString () {
        if (im < 0.0) {                                                        // ...remembering NaN & Infinity
            return  ("(" + re + " - " + (-im) + "i)");
        } else if (1.0/im == Double.NEGATIVE_INFINITY) {
            return  ("(" + re + " - " + 0.0 + "i)");
        } else {
            return  ("(" + re + " + " + (+im) + "i)");
        }//endif
    }//end toString()



    /*
            I know a young man called Daniel,
            When you meet him, you'll like him, and you'll
            Find him so true, so human and new,
            You'll want to live life with no manual.
    */

}//end Complex




/*         Jim Shapiro <jnshapi@argo.ecte.uswc.uswest.com>


                         Priyantha Jayanetti
                     ---------------------------
                     email: pidge@eece.maine.edu

             Deptolemy.  of Electrical & Computer Engineering
                     University of Maine,  Orono


                          Mr.  Daniel Hirsch
                        <R.D.Hirsch@qmw.ac.uk>


/*  Correct my address in any reply, or it'll be treated as spam:
--                                                          
//     Alexander Anderson    <DELETE_THIS.sandy@almide.demon.co.uk>
//     Home Fone                               +44 (0) 171-794-4543
//     London, UK                    http://www.almide.demon.co.uk/ 
//     PGP print   C6 8C 55 F2 77 7B 99 9B  14 77 66 F5 B8 74 CF 12
*/
