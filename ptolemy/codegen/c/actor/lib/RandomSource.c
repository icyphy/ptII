// The algorithm of generating the random number with RandomSource distribution
// is based on source code from Java.util.Random. Given the same seed, it
// generates the same list of random numbers as the java.util.Random object.

/*** sharedBlock ***/
#ifdef PT_NO_TIME
/* Atmel AVR does not have time() */
#define time(x)
#endif
int RandomSource_next(int bits, double* seed) {
    *seed = (((long long) *seed * 0x5DEECE66DLL) + 0xBLL) & ((1LL << 48) - 1);
    return (int)((signed long long) *seed >> (48 - bits));
}

double RandomSource_nextDouble(double* seed) {
    return (((long long)RandomSource_next(26, seed) << 27) + RandomSource_next(27, seed)) / (double)(1LL << 53);
}
/**/


     
/*** gaussianBlock ***/

double RandomSource_nextGaussian(double* seed, boolean* haveNextNextGaussian, double* nextNextGaussian) {
    double multiplier;
    double v1;
    double v2;
    double s;
    
    if (*haveNextNextGaussian) {
        *haveNextNextGaussian = false;
        return *nextNextGaussian;
    } else {
        do { 
            v1 = 2 * RandomSource_nextDouble(seed) - 1;   // between -1.0 and 1.0
            v2 = 2 * RandomSource_nextDouble(seed) - 1;   // between -1.0 and 1.0
            s = v1 * v1 + v2 * v2;
        } while (s >= 1 || s == 0);

        multiplier = sqrt(-2 * log(s)/s);
        *nextNextGaussian = v2 * multiplier;
        *haveNextNextGaussian = true;
        return v1 * multiplier;
    }
}

/**/

/*** binomialBlock ***/

// The following copyright information applies to the binomialBlock.

// The C clustering library for cDNA microarray data.
// Copyright (C) 2002 Michiel Jan Laurens de Hoon.
//
// This library was written at the Laboratory of DNA Information Analysis,
// Human Genome Center, Institute of Medical Science, University of Tokyo,
// 4-6-1 Shirokanedai, Minato-ku, Tokyo 108-8639, Japan.
// Contact: mdehoon@c2b2.columbia.edu
//
// Permission to use, copy, modify, and distribute this software and its
// documentation with or without modifications and for any purpose and
// without fee is hereby granted, provided that any copyright notices
// appear in all copies and that both those copyright notices and this
// permission notice appear in supporting documentation, and that the
// names of the contributors or copyright holders not be used in
// advertising or publicity pertaining to distribution of the software
// without specific prior permission.
//
// THE CONTRIBUTORS AND COPYRIGHT HOLDERS OF THIS SOFTWARE DISCLAIM ALL
// WARRANTIES WITH REGARD TO THIS SOFTWARE, INCLUDING ALL IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO EVENT SHALL THE
// CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY SPECIAL, INDIRECT
// OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
// OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE
// OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE
// OR PERFORMANCE OF THIS SOFTWARE.

// The algorithm of generating the random number with RandomSource distribution
// is based on source code from Java.util.Random. Given the same seed, it
// generates the same list of random numbers as the java.util.Random object.

int RandomSource_BinomialDistribution(int n, double p, double* seed) {
    double q1 = 1 - p;
    if (n*p < 30.0) { /* Algorithm BINV */
        const double s = p/q1;
        const double a = (n+1)*s;
        double r = exp(n*log(q1)); /* pow() causes a crash on AIX */
        int x = 0;
        double u = RandomSource_nextDouble(seed);
        while(1) {
            if (u < r) {
                return x;
            }
            u-=r;
            x++;
            r *= (a/x)-s;
        }
    } else { /* Algorithm BTPE */
        /* Step 0 */
        const double fm = n*p + p;
        const int m = (int) fm;
        const double p1 = floor(2.195*sqrt(n*p*q1) -4.6*q1) + 0.5;
        const double xm = m + 0.5;
        const double xl = xm - p1;
        const double xr = xm + p1;
        const double c = 0.134 + 20.5/(15.3+m);
        const double a = (fm-xl)/(fm-xl*p);
        const double b = (xr-fm)/(xr*q1);
        const double lambdal = a*(1.0+0.5*a);
        const double lambdar = b*(1.0+0.5*b);
        const double p2 = p1*(1+2*c);
        const double p3 = p2 + c/lambdal;
        const double p4 = p3 + c/lambdar;
        while (1) { /* Step 1 */
            int y;
            int k;
            double u = RandomSource_nextDouble(seed);
            double v = RandomSource_nextDouble(seed);
            u *= p4;
            if (u <= p1) {
                return (int)(xm-p1*v+u);
            }
            /* Step 2 */
            if (u > p2) { /* Step 3 */
                if (u > p3) { /* Step 4 */
                    y = (int)(xr-log(v)/lambdar);
                    if (y > n)
                        continue; /* Go to step 5 */
                    v = v*(u-p3)*lambdar;
                } else {
                    y = (int)(xl+log(v)/lambdal);
                    if (y < 0)
                        continue; /* Go to step 5 */
                    v = v*(u-p2)*lambdal;
                }
            } else {
                const double x = xl + (u-p1)/c;
                v = v*c + 1.0 - fabs(m-x+0.5)/p1;
                if (v > 1)
                    continue; /* Go to step 5 */
                y = (int)x;
            }
            /* Step 5 */
            /* Step 5.0 */
            k = abs(y-m);
            if (k > 20 && k < 0.5*n*p*q1-1.0) { /* Step 5.2 */
                double rho = (k/(n*p*q1))*((k*(k/3.0 + 0.625) + 0.1666666666666)/(n*p*q1)+0.5);
                double t = -k*k/(2*n*p*q1);
                double A = log(v);
                if (A < t-rho) {
                    return y;
                } else if (A > t+rho) {
                    continue;
                } else { /* Step 5.3 */
                    double x1 = y+1;
                    double f1 = m+1;
                    double z = n+1-m;
                    double w = n-y+1;
                    double x2 = x1*x1;
                    double f2 = f1*f1;
                    double z2 = z*z;
                    double w2 = w*w;
                    if (A > xm * log(f1/x1) + (n-m+0.5)*log(z/w)
                          + (y-m)*log(w*p/(x1*q1))
                          + (13860.-(462.-(132.-(99.-140./f2)/f2)/f2)/f2)/f1/166320.
                          + (13860.-(462.-(132.-(99.-140./z2)/z2)/z2)/z2)/z/166320.
                          + (13860.-(462.-(132.-(99.-140./x2)/x2)/x2)/x2)/x1/166320.
                          + (13860.-(462.-(132.-(99.-140./w2)/w2)/w2)/w2)/w/166320.)
                        continue;
                    return y;
                }
            } else { /* Step 5.1 */
                int i;
                const double s = p/q1;
                const double aa = s*(n+1);
                double f = 1.0;
                for (i = m; i < y; f *= (aa/(++i)-s))
                    ;
                for (i = y; i < m; f /= (aa/(++i)-s))
                    ;
                if (v > f)
                    continue;
                return y;
            }
        }
    }
}
/**/

/*** poissonBlock ***/

// The following copyright applies to the RandomSource_LogFactorial() and RandomSource_PoissonHelper functions:
//
// This code is in the public domain

static double RandomSource_LogFactorial(double k) {
#define       C0      9.18938533204672742e-01
#define       C1      8.33333333333333333e-02
#define       C3     -2.77777777777777778e-03
#define       C5      7.93650793650793651e-04
#define       C7     -5.95238095238095238e-04

    static double logfak[30L] = {
        0.00000000000000000,   0.00000000000000000,   0.69314718055994531,
        1.79175946922805500,   3.17805383034794562,   4.78749174278204599,
        6.57925121201010100,   8.52516136106541430,  10.60460290274525023,
        12.80182748008146961,  15.10441257307551530,  17.50230784587388584,
        19.98721449566188615,  22.55216385312342289,  25.19122118273868150,
        27.89927138384089157,  30.67186010608067280,  33.50507345013688888,
        36.39544520803305358,  39.33988418719949404,  42.33561646075348503,
        45.38013889847690803,  48.47118135183522388,  51.60667556776437357,
        54.78472939811231919,  58.00360522298051994,  61.26170176100200198,
        64.55753862700633106,  67.88974313718153498,  71.25703896716800901
    };

    double  r, rr;

    if (k >= 30.0) {
        r  = 1.0 / k;
        rr = r * r;
        return( (k + 0.5)*log(k) - k + C0 + r*(C1 + rr*(C3 + rr*(C5 + rr*C7))) );
    } else {
        return(logfak[(int)k]);
    }
}

static double RandomSource_PoissonHelper(double k, double l_nu, double c_pm) {
    return  exp(k * l_nu - RandomSource_LogFactorial(k) - c_pm);
}

// The RandomSource_PoissonDistribution function is based on the nextInt() method in the cern.jet.random.Poisson class.
// The folowing copyright applies to the it:
//
// Copyright ? 1999 CERN - European Organization for Nuclear Research.
// Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose
// is hereby granted without fee, provided that the above copyright notice appear in all copies and
// that both that copyright notice and this permission notice appear in supporting documentation.
// CERN makes no representations about the suitability of this software for any purpose.
// It is provided "as is" without expressed or implied warranty.

static double RandomSource_PoissonDistribution(double mean, double* seed) {
    static double SWITCH_MEAN = 10.0;
    static int MEAN_MAX = 99999999;
    static int pp_length = 36;

    double my = mean;
    double p,q,p0,pp[pp_length];
    long m;
    double u;
    int k,i;

    int llll;

    if (my < SWITCH_MEAN) { // CASE B: Inversion- start new table and calculate p0
        llll = 0;
        p = exp(-my);
        q = p;
        p0 = p;
        for (k=pp_length; --k >=0; )
            pp[k] = 0;
        m = (my > 1.0) ? (int) my : 1;
        for(;;) {
            u = RandomSource_nextDouble(seed);    // Step U. Uniform sample
            k = 0;
            if (u <= p0)
                return(k);
            if (llll != 0) {              // Step T. Table comparison
                i = (u > 0.458) ? ((llll < m) ? llll: m) : 1;
                for (k = i; k <= llll; k++)
                    if (u <= pp[k])
                        return(k);
                if (llll == pp_length - 1)
                    continue;
            }
            for (k = llll + 1; k <= pp_length - 1; k++) { // Step C. Creation of new prob.
                p *= my / (double) k;
                q += p;
                pp[k] = q;
                if (u <= q) {
                    llll = k;
                    return(k);
                }
            }
            llll = pp_length - 1;
        }
    } else if (my < MEAN_MAX ) { // CASE A: acceptance complement
        static long int      m,  k2, k4, k1, k5;
        static double        dl, dr, r1, r2, r4, r5, ll, lr, l_my, c_pm;
        static double        f1, f2, f4, f5, p1, p2, p3, p4, p5, p6;
        int    Dk, X, Y;
        double Ds, U, V, W;

        m  = (int) my;
        // approximate deviation of reflection points k2, k4 from my - 1/2
        Ds = sqrt(my + 0.25);

        // mode m, reflection points k2 and k4, and points k1 and k5, which
        // delimit the centre region of h(x)
        k2 = (int) ceil(my - 0.5 - Ds);
        k4 = (int)     (my - 0.5 + Ds);
        k1 = k2 + k2 - m + 1;
        k5 = k4 + k4 - m;

        // range width of the critical left and right centre region
        dl = (double) (k2 - k1);
        dr = (double) (k5 - k4);

        // recurrence constants r(k) = p(k)/p(k-1) at k = k1, k2, k4+1, k5+1
        r1 = my / (double) k1;
        r2 = my / (double) k2;
        r4 = my / (double)(k4 + 1);
        r5 = my / (double)(k5 + 1);

        // reciprocal values of the scale parameters of expon. tail envelopes
        ll =  log(r1);                     // expon. tail left
        lr = -log(r5);                     // expon. tail right

        // Poisson constants, necessary for computing function values f(k)
        l_my = log(my);
        //c_pm = m * l_my - Arithmetic.logFactorial(m);
        c_pm = m * l_my - RandomSource_LogFactorial(m);

        // function values f(k) = p(k)/p(m) at k = k2, k4, k1, k5
        f2 = RandomSource_PoissonHelper(k2, l_my, c_pm);
        f4 = RandomSource_PoissonHelper(k4, l_my, c_pm);
        f1 = RandomSource_PoissonHelper(k1, l_my, c_pm);
        f5 = RandomSource_PoissonHelper(k5, l_my, c_pm);

        // area of the two centre and the two exponential tail regions
        // area of the two immediate acceptance regions between k2, k4
        p1 = f2 * (dl + 1.0);                    // immed. left
        p2 = f2 * dl         + p1;               // centre left
        p3 = f4 * (dr + 1.0) + p2;               // immed. right
        p4 = f4 * dr         + p3;               // centre right
        p5 = f1 / ll         + p4;               // expon. tail left
        p6 = f5 / lr         + p5;               // expon. tail right

        for (;;) {
            // generate uniform number U -- U(0, p6)
            // case distinction corresponding to U
            if ((U = RandomSource_nextDouble(seed) * p6) < p2) { // centre left

                // immediate acceptance region R2 = [k2, m) *[0, f2),  X = k2, ... m -1
                if ((V = U - p1) < 0.0)
                    return(k2 + (int) (U / f2));
                // immediate acceptance region R1 = [k1, k2)*[0, f1),  X = k1, ... k2-1
                if ((W = V / dl) < f1 )
                    return(k1 + (int) (V / f1));

                // computation of candidate X < k2, and its counterpart Y > k2
                // either squeeze-acceptance of X or acceptance-rejection of Y
                Dk = (int)(dl * RandomSource_nextDouble(seed)) + 1;
                if (W <= f2 - Dk * (f2 - f2 / r2)) {        // quick accept of
                    return(k2 - Dk);                        // X = k2 - Dk
                }
                if ((V = f2 + f2 - W) < 1.0) {              // quick reject of Y
                    Y = k2 + Dk;
                    if (V <= f2 + Dk * (1.0 - f2) / (dl + 1.0)) {// quick accept of
                        return(Y);                          // Y = k2 + Dk
                    }
                    if (V <= RandomSource_PoissonHelper(Y, l_my, c_pm))
                        return(Y);    // final accept of Y
                }
                X = k2 - Dk;
            } else if (U < p4) {                            // centre right
                // immediate acceptance region R3 = [m, k4+1)*[0, f4), X = m, ... k4
                if ((V = U - p3) < 0.0)
                    return(k4 - (int) ((U - p2) / f4));
                // immediate acceptance region R4 = [k4+1, k5+1)*[0, f5)
                if ((W = V / dr) < f5 )
                    return(k5 - (int) (V / f5));

                // computation of candidate X > k4, and its counterpart Y < k4
                // either squeeze-acceptance of X or acceptance-rejection of Y
                Dk = (int)(dr * RandomSource_nextDouble(seed)) + 1;
                if (W <= f4 - Dk * (f4 - f4 * r4)) {        // quick accept of
                    return(k4 + Dk);                        // X = k4 + Dk
                }
                if ((V = f4 + f4 - W) < 1.0) {              // quick reject of Y
                    Y = k4 - Dk;
                    if (V <= f4 + Dk * (1.0 - f4) / dr) {   // quick accept of
                        return(Y);                          // Y = k4 - Dk
                    }
                    if (V <= RandomSource_PoissonHelper(Y, l_my, c_pm))
                        return(Y);    // final accept of Y
                }
                X = k4 + Dk;
            } else {
                W = RandomSource_nextDouble(seed);
                if (U < p5) {                               // expon. tail left
                    Dk = (int)(1.0 - log(W) / ll);
                    if ((X = k1 - Dk) < 0)
                        continue;          // 0 <= X <= k1 - 1
                    W *= (U - p4) * ll;                     // W -- U(0, h(x))
                    if (W <= f1 - Dk * (f1 - f1 / r1))
                        return(X); // quick accept of X
                } else {                                    // expon. tail right
                    Dk = (int)(1.0 - log(W) / lr);
                    X  = k5 + Dk;                           // X >= k5 + 1
                    W *= (U - p5) * lr;                     // W -- U(0, h(x))
                    if (W <= f5 - Dk * (f5 - f5 * r5))
                        return(X); // quick accept of X
                }
            }

            // acceptance-rejection test of candidate X from the original area
            // test, whether  W <= f(k),    with  W = U*h(x)  and  U -- U(0, 1)
            // log f(X) = (X - m)*log(my) - log X! + log m!
            if (log(W) <= X * l_my - RandomSource_LogFactorial(X) - c_pm)
                return(X);
        }
    } else { // mean is too large
        return (int) my;
    }
}
/**/

/*** setSeedBlock0($hashCode) ***/
$actorSymbol(seed) = time (NULL) + $hashCode;
/**/

/*** setSeedBlock1($hashCode) ***/
/* see documentation from http://java.sun.com/j2se/1.4.2/docs/api/java/util/Random.html#setSeed(long) */
//this.seed = (seed ^ 0x5DEECE66DL) & ((1L << 48) - 1);
$actorSymbol(seed) = (((long long) $val(seed) + $hashCode) ^ 0x5DEECE66DLL)  & ((1LL << 48) - 1);
/**/

/*** preinitBlock ***/
double $actorSymbol(seed);
/**/


