/*** setSeedBlock0($hashCode) ***/
$actorSymbol(current) = time (NULL) + $hashCode;
/**/

/*** setSeedBlock1($hashCode) ***/
$actorSymbol(current) = $val(seed) + $hashCode;
ColtRandomSource_setSeed(&$actorSymbol(current));

/**/

/***preinitBlock***/
int $actorSymbol(current);
double $actorSymbol(seed);
/**/


/*** sharedBlock ***/
#ifdef sun
#include <nan.h>  // Needed for ColtPoisson and IsINF() under Solaris.
#endif

#ifdef PT_NO_TIME
/* Atmel AVR does not have time() */
#define time(x)
#endif

#ifndef min
#define min(a,b) ((a)<(b) ? (a) : (b))
#endif

// The contents of ColtRandomSource_setSeed(), ColtRandomSource_next(), ColtRandomSource_setSeed(),
// ColtRandomSource_nextDouble(), ColtRandomSource_nextInt() and ColtRandomSource_nextLong(), ColtRandomSource_raw()
// are from Colt and are found in
// colt/src/cern/jet/random/engine/{RandomEngine,DRand}
// Colt's copyright is:
// Copyright (c) 1999-2008 CERN - European Organization for Nuclear Research.
// Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose
// is hereby granted without fee, provided that the above copyright notice appear in all copies and
// that both that copyright notice and this permission notice appear in supporting documentation.
// CERN makes no representations about the suitability of this software for any purpose.
// It is provided "as is" without expressed or implied warranty.

int ColtRandomSource_next(int bits, int* seed) {
    *seed = (((long long) *seed * 0x5DEECE66DLL) + 0xBLL) & ((1LL << 48) - 1);
    return (int)((signed long long) *seed >> (48 - bits));
}

void ColtRandomSource_setSeed(int* seed) {
    if (*seed<0) *seed = -*seed;
    int limit = (int)((pow(2,32)-1) /4); // --> 536870911
    if (*seed >= limit) *seed = *seed >> 3;

    *seed = 4* *seed+1;
}

/**
 * Returns a 32 bit uniformly distributed random number in the closed
 * interval <tt>[Integer.MIN_VALUE,Integer.MAX_VALUE]</tt> (including
 * <tt>Integer.MIN_VALUE</tt> and <tt>Integer.MAX_VALUE</tt>).
 */
int ColtRandomSource_nextInt(int *current) {
    *current *= 0x278DDE6D;     /* z(i+1)=a*z(i) (mod 2**32) */
    // a == 0x278DDE6D == 663608941
    return *current;
}

/**
 * Returns a 64 bit uniformly distributed random number in the closed
 * interval <tt>[Long.MIN_VALUE,Long.MAX_VALUE]</tt> (including
 * <tt>Long.MIN_VALUE</tt> and <tt>Long.MAX_VALUE</tt>).
 */
long ColtRandomSource_nextLong(int *current) {
    // The contents of this method is from colt/src/cern/jet/random/engine/RandomEngine.java

    // concatenate two 32-bit strings into one 64-bit string
    return ((ColtRandomSource_nextInt(current) & 0xFFFFFFFFL) << 32)
           |  ((ColtRandomSource_nextInt(current) & 0xFFFFFFFFL));
}

double ColtRandomSource_nextDouble(int* current) {

    double nextDouble;
    do {
        // -9.223372036854776E18 == (double) Long.MIN_VALUE
        // 5.421010862427522E-20 == 1 / Math.pow(2,64) == 1 / ((double) Long.MAX_VALUE - (double) Long.MIN_VALUE);
        nextDouble = ((double) ColtRandomSource_nextLong(current) - -9.223372036854776E18)  *  5.421010862427522E-20;
    }
    // catch loss of precision of long --> double conversion
    while (! (nextDouble>0.0 && nextDouble<1.0));

    // --> in (0.0,1.0)
    return nextDouble;

    /*
            nextLong == Long.MAX_VALUE         --> 1.0
            nextLong == Long.MIN_VALUE         --> 0.0
            nextLong == Long.MAX_VALUE-1       --> 1.0
            nextLong == Long.MAX_VALUE-100000L --> 0.9999999999999946
            nextLong == Long.MIN_VALUE+1       --> 0.0
            nextLong == Long.MIN_VALUE-100000L --> 0.9999999999999946
            nextLong == 1L                     --> 0.5
            nextLong == -1L                    --> 0.5
            nextLong == 2L                     --> 0.5
            nextLong == -2L                    --> 0.5
            nextLong == 2L+100000L             --> 0.5000000000000054
            nextLong == -2L-100000L            --> 0.49999999999999456
    */

    //return (((long long)ColtRandomSource_next(26, seed) << 27) + ColtRandomSource_next(27, seed)) / (double)(1LL << 53);
}

/**
 * Returns a 32 bit uniformly distributed random number in the open
 * unit interval <code>(0.0,1.0)</code> (excluding 0.0 and 1.0).
 */
double ColtRandomSource_raw(int *current) {
    int nextInt;
    do { // accept anything but zero
        nextInt = ColtRandomSource_nextInt(current); // in [Integer.MIN_VALUE,Integer.MAX_VALUE]-interval
    } while (nextInt==0);

    // transform to (0.0,1.0)-interval
    // 2.3283064365386963E-10 == 1.0 / Math.pow(2,32)
    return (double) (nextInt & 0xFFFFFFFFL) * 2.3283064365386963E-10;

    /*
            nextInt == Integer.MAX_VALUE   --> 0.49999999976716936
            nextInt == Integer.MIN_VALUE   --> 0.5
            nextInt == Integer.MAX_VALUE-1 --> 0.4999999995343387
            nextInt == Integer.MIN_VALUE+1 --> 0.5000000002328306
            nextInt == 1                   --> 2.3283064365386963E-10
            nextInt == -1                  --> 0.9999999997671694
            nextInt == 2                   --> 4.6566128730773926E-10
            nextInt == -2                  --> 0.9999999995343387
    */
}
/**/



/*** gaussianBlock ***/

double ColtRandomSource_nextGaussian(int* seed, boolean* haveNextNextGaussian, double* nextNextGaussian) {
    double multiplier;
    double v1;
    double v2;
    double s;

    if (*haveNextNextGaussian) {
        *haveNextNextGaussian = false;
        return *nextNextGaussian;
    } else {
        do {
            v1 = 2 * ColtRandomSource_nextDouble(seed) - 1;   // between -1.0 and 1.0
            v2 = 2 * ColtRandomSource_nextDouble(seed) - 1;   // between -1.0 and 1.0
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


// The contents of ColtRandomSource_stirlingCorrection(), ColtRandomSource_generateBinomial()
// ColtRandomSource_nextIntNP(), ColtRandomSource_generateBinomial()
// are from Colt and are found in
// colt/src/cern/jet/random/engine/{RandomEngine,DRand}
// Colt's copyright is:
// Copyright (c) 1999-2008 CERN - European Organization for Nuclear Research.
// Permission to use, copy, modify, distribute and sell this software
// and its documentation for any purpose
// is hereby granted without fee, provided that the above copyright notice appear in all copies and
// that both that copyright notice and this permission notice appear in supporting documentation.
// CERN makes no representations about the suitability of this software for any purpose.
// It is provided "as is" without expressed or implied warranty.

/**
 * Returns the StirlingCorrection.
 * <p>
 * Correction term of the Stirling approximation for <tt>log(k!)</tt>
 * (series in 1/k, or table values for small k)
 * with int parameter k.
 * <p>
 * <tt>
 * log k! = (k + 1/2)log(k + 1) - (k + 1) + (1/2)log(2Pi) +
 *          stirlingCorrection(k + 1)
 * <p>
 * log k! = (k + 1/2)log(k)     -  k      + (1/2)log(2Pi) +
 *          stirlingCorrection(k)
 * </tt>
 */
static double ColtRandomSource_stirlingCorrection(int k) {
    static double stirlingCorrection[31] =  {
        0.0,
        8.106146679532726e-02, 4.134069595540929e-02,
        2.767792568499834e-02, 2.079067210376509e-02,
        1.664469118982119e-02, 1.387612882307075e-02,
        1.189670994589177e-02, 1.041126526197209e-02,
        9.255462182712733e-03, 8.330563433362871e-03,
        7.573675487951841e-03, 6.942840107209530e-03,
        6.408994188004207e-03, 5.951370112758848e-03,
        5.554733551962801e-03, 5.207655919609640e-03,
        4.901395948434738e-03, 4.629153749334029e-03,
        4.385560249232324e-03, 4.166319691996922e-03,
        3.967954218640860e-03, 3.787618068444430e-03,
        3.622960224683090e-03, 3.472021382978770e-03,
        3.333155636728090e-03, 3.204970228055040e-03,
        3.086278682608780e-03, 2.976063983550410e-03,
        2.873449362352470e-03, 2.777674929752690e-03,
    };

    double C1 =  8.33333333333333333e-02;     //  +1/12
    double C3 = -2.77777777777777778e-03;     //  -1/360
    double C5 =  7.93650793650793651e-04;     //  +1/1260
    double C7 = -5.95238095238095238e-04;     //  -1/1680

    double r, rr;

    if (k > 30) {
        r = 1.0 / (double) k;
        rr = r * r;
        return r*(C1 + rr*(C3 + rr*(C5 + rr*C7)));
    }
    else return stirlingCorrection[k];
}

// FIXME: these need to be specific to this actor.
int    n_last = -1,  n_prev = -1;
double par,np,p0,q,p_last = -1.0, p_prev = -1.0;
int    b,m,nm;
double pq, rc, ss, xm, xl, xr, ll, lr, c, p1, p2, p3, p4, ch;
/**
 *                                                                *
 *     Binomial-Distribution - Acceptance Rejection/Inversion     *
 *                                                                *
 *                                                                *
 * Acceptance Rejection method combined with Inversion for        *
 * generating Binomial random numbers with parameters             *
 * n (number of trials) and p (probability of success).           *
 * For  min(n*p,n*(1-p)) < 10  the Inversion method is applied:   *
 * The random numbers are generated via sequential search,        *
 * starting at the lowest index k=0. The cumulative probabilities *
 * are avoided by using the technique of chop-down.               *
 * For  min(n*p,n*(1-p)) >= 10  Acceptance Rejection is used:     *
 * The algorithm is based on a hat-function which is uniform in   *
 * the centre region and exponential in the tails.                *
 * A triangular immediate acceptance region in the centre speeds  *
 * up the generation of binomial variates.                        *
 * If candidate k is near the mode, f(k) is computed recursively  *
 * starting at the mode m.                                        *
 * The acceptance test by Stirling's formula is modified          *
 * according to W. Hoermann (1992): The generation of binomial    *
 * random variates, to appear in J. Statist. Comput. Simul.       *
 * If  p < .5  the algorithm is applied to parameters n, p.       *
 * Otherwise p is replaced by 1-p, and k is replaced by n - k.    *
 *                                                                *
 ******************************************************************
 *                                                                *
 * FUNCTION:    - samples a random number from the binomial       *
 *                distribution with parameters n and p  and is    *
 *                valid for  n*min(p,1-p)  >  0.                  *
 * REFERENCE:   - V. Kachitvichyanukul, B.W. Schmeiser (1988):    *
 *                Binomial random variate generation,             *
 *                Communications of the ACM 31, 216-222.          *
 * SUBPROGRAMS: - StirlingCorrection()                            *
 *                            ... Correction term of the Stirling *
 *                                approximation for log(k!)       *
 *                                (series in 1/k or table values  *
 *                                for small k) with long int k    *
 *              - randomGenerator    ... (0,1)-Uniform engine     *
 *                                                                *
 ******************************************************************/
int ColtRandomSource_generateBinomial(int n, double p, int* current) {
    double C1_3 = 0.33333333333333333;
    double C5_8 = 0.62500000000000000;
    double C1_6 = 0.16666666666666667;
    int DMAX_KM = 20;


    int     bh,i, K, Km, nK;
    double  f, rm, U, V, X, T, E;

    if (n != n_last || p != p_last) {                 // set-up
        n_last = n;
        p_last = p;
        par=min(p,1.0-p);
        q=1.0-par;
        np = n*par;

        // Check for invalid input values

        if ( np <= 0.0 ) return -1;

        rm = np + par;
        m  = (int) rm;                                        // mode, integer
        if (np<10) {
            p0=exp(n*log(q));               // Chop-down
            bh=(int)(np+10.0*sqrt(np*q));
            b=min(n,bh);
        }
        else {
            rc = (n + 1.0) * (pq = par / q);          // recurr. relat.
            ss = np * q;                              // variance
            i  = (int) (2.195*sqrt(ss) - 4.6*q); // i = p1 - 0.5
            xm = m + 0.5;
            xl = (double) (m - i);                    // limit left
            xr = (double) (m + i + 1L);               // limit right
            f  = (rm - xl) / (rm - xl*par);
            ll = f * (1.0 + 0.5*f);
            f  = (xr - rm) / (xr * q);
            lr = f * (1.0 + 0.5*f);
            c  = 0.134 + 20.5/(15.3 + (double) m);    // parallelogram
            // height
            p1 = i + 0.5;
            p2 = p1 * (1.0 + c + c);                  // probabilities
            p3 = p2 + c/ll;                           // of regions 1-4
            p4 = p3 + c/lr;
        }
    }

    if (np<10) {                                      //Inversion Chop-down
        double pk;

        K=0;
        pk=p0;
        U=ColtRandomSource_raw(current);
        while (U>pk) {
            ++K;
            if (K>b) {
                U=ColtRandomSource_raw(current);
                K=0;
                pk=p0;
            }
            else {
                U-=pk;
                pk=(double)(((n-K+1)*par*pk)/(K*q));
            }
        }
        return ((p>0.5) ? (n-K):K);
    }

    for (;;) {
        V = ColtRandomSource_raw(current);
        if ((U = ColtRandomSource_raw(current) * p4) <= p1) {    // triangular region
            K=(int) (xm - U + p1*V);
            return (p>0.5) ? (n-K):K;  // immediate accept
        }
        if (U <= p2) {                                        // parallelogram
            X = xl + (U - p1)/c;
            if ((V = V*c + 1.0 - fabs(xm - X)/p1) >= 1.0)  continue;
            K = (int) X;
        }
        else if (U <= p3) {                                    // left tail
            if ((X = xl + log(V)/ll) < 0.0)  continue;
            K = (int) X;
            V *= (U - p2) * ll;
        }
        else {                                                 // right tail
            if ((K = (int) (xr - log(V)/lr)) > n)  continue;
            V *= (U - p3) * lr;
        }

        // acceptance test :  two cases, depending on |K - m|
        if ((Km = abs(K - m)) <= DMAX_KM || Km + Km + 2L >= ss) {

            // computation of p(K) via recurrence relationship from the mode
            f = 1.0;                              // f(m)
            if (m < K) {
                for (i = m; i < K; ) {
                    if ((f *= (rc / ++i - pq)) < V)  break;  // multiply  f
                }
            }
            else {
                for (i = K; i < m; ) {
                    if ((V *= (rc / ++i - pq)) > f)  break;  // multiply  V
                }
            }
            if (V <= f)  break;                                        // acceptance test
        }
        else {

            // lower and upper squeeze tests, based on lower bounds for log p(K)
            V = log(V);
            T = - Km * Km / (ss + ss);
            E =  (Km / ss) * ((Km * (Km * C1_3 + C5_8) + C1_6) / ss + 0.5);
            if (V <= T - E)  break;
            if (V <= T + E) {
                if (n != n_prev || par != p_prev) {
                    n_prev = n;
                    p_prev = par;

                    nm = n - m + 1;
                    ch = xm * log((m + 1.0)/(pq * nm)) +
                         ColtRandomSource_stirlingCorrection(m + 1) + ColtRandomSource_stirlingCorrection(nm);
                }
                nK = n - K + 1;

                // computation of log f(K) via Stirling's formula
                // final acceptance-rejection test
                if (V <= ch + (n + 1.0)*log((double) nm / (double) nK) +
                        (K + 0.5)*log(nK * pq / (K + 1.0)) -
                        ColtRandomSource_stirlingCorrection(K + 1) - ColtRandomSource_stirlingCorrection(nK))  break;
            }
        }
    }
    return (p>0.5) ? (n-K):K;
}

/**
 * Returns a random number from the distribution with the given parameters n and p; bypasses the internal state.
 * @param n the number of trials
 * @param p the probability of success.
 * @throws IllegalArgumentException if <tt>n*Math.min(p,1-p) &lt;= 0.0</tt>
 */
int ColtRandomSource_nextIntNP(int n, double p, int *current) {
    if (n*min(p,1-p) <= 0.0) {
    }
    return ColtRandomSource_generateBinomial(n,p, current);
}

int ColtRandomSource_BinomialDistribution(int n, double p, int* current) {
    int result;
    if (n == 0) {
        result = 0;
    } else if (p == 0.0) {
        result = 0;
    } else if (p == 1.0) {
        result = n;
    } else {
        result = ColtRandomSource_nextIntNP(n, p, current);
    }
    return result;
}
/**/

/*** poissonBlock ***/

// The following copyright applies to the ColtRandomSource_LogFactorial() and ColtRandomSource_PoissonHelper functions:
//
// This code is in the public domain

static double ColtRandomSource_LogFactorial(double k) {
    double  C0 = 9.18938533204672742e-01;
    double  C1 = 8.33333333333333333e-02;
    double  C3 = -2.77777777777777778e-03;
    double  C5 = 7.93650793650793651e-04;
    double  C7 = -5.95238095238095238e-04;

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

static double ColtRandomSource_PoissonHelper(double k, double l_nu, double c_pm) {
    return  exp(k * l_nu - ColtRandomSource_LogFactorial(k) - c_pm);
}

// The ColtRandomSource_PoissonDistribution() function is based on the
// nextInt() method in the cern.jet.random.Poisson class.
//
// The folowing copyright applies to ColtRandomSource_PoissonDistribution():
//
// Copyright ? 1999 CERN - European Organization for Nuclear Research.
// Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose
// is hereby granted without fee, provided that the above copyright notice appear in all copies and
// that both that copyright notice and this permission notice appear in supporting documentation.
// CERN makes no representations about the suitability of this software for any purpose.
// It is provided "as is" without expressed or implied warranty.

static double ColtRandomSource_PoissonDistribution(double mean, int* current) {
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
        //if (my != my_old) {
        //                my_old = my;
        llll = 0;
        p = exp(-my);
        q = p;
        p0 = p;
        for (k=pp_length; --k >=0; ) pp[k] = 0;
        //        }
        m = (my > 1.0) ? (int)my : 1;
        for (;;) {
            u = ColtRandomSource_raw(current);           // Step U. Uniform sample
            k = 0;
            if (u <= p0) {
                return(k);
            }
            if (llll != 0) {              // Step T. Table comparison
                i = (u > 0.458) ? min(llll,m) : 1;
                for (k = i; k <=llll; k++) {
                    if (u <= pp[k]) {
                        return(k);
                    }
                }
                if (llll == 35) continue;
            }
            for (k = llll +1; k <= 35; k++) { // Step C. Creation of new prob.
                p *= my/(double)k;
                q += p;
                pp[k] = q;
                if (u <= q) {
                    llll = k;
                    return(k);

                }
            }
            llll = 35;
        }
    }     // end my < SWITCH_MEAN
    else if (my < MEAN_MAX ) { // CASE A: acceptance complement
        //static double        my_last = -1.0;
        //static long int      m,  k2, k4, k1, k5;
        //static double        dl, dr, r1, r2, r4, r5, ll, lr, l_my, c_pm,
        //                                           f1, f2, f4, f5, p1, p2, p3, p4, p5, p6;
        static long int      m,  k2, k4, k1, k5;
        static double        dl, dr, r1, r2, r4, r5, ll, lr, l_my, c_pm;
        static double        f1, f2, f4, f5, p1, p2, p3, p4, p5, p6;
        int    Dk, X, Y;
        double Ds, U, V, W;

        m  = (int) my;
        //                if (my != my_last) { //  set-up
        //        my_last = my;

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
        c_pm = m * l_my - ColtRandomSource_LogFactorial(m);

        // function values f(k) = p(k)/p(m) at k = k2, k4, k1, k5
        f2 = ColtRandomSource_PoissonHelper(k2, l_my, c_pm);
        f4 = ColtRandomSource_PoissonHelper(k4, l_my, c_pm);
        f1 = ColtRandomSource_PoissonHelper(k1, l_my, c_pm);
        f5 = ColtRandomSource_PoissonHelper(k5, l_my, c_pm);

        // area of the two centre and the two exponential tail regions
        // area of the two immediate acceptance regions between k2, k4
        p1 = f2 * (dl + 1.0);                    // immed. left
        p2 = f2 * dl         + p1;               // centre left
        p3 = f4 * (dr + 1.0) + p2;               // immed. right
        p4 = f4 * dr         + p3;               // centre right
        p5 = f1 / ll         + p4;               // expon. tail left
        p6 = f5 / lr         + p5;               // expon. tail right
        //                } // end set-up

        for (;;) {
            // generate uniform number U -- U(0, p6)
            // case distinction corresponding to U
            if ((U = ColtRandomSource_raw(current) * p6) < p2) {         // centre left

                // immediate acceptance region R2 = [k2, m) *[0, f2),  X = k2, ... m -1
                if ((V = U - p1) < 0.0)  return(k2 + (int)(U/f2));
                // immediate acceptance region R1 = [k1, k2)*[0, f1),  X = k1, ... k2-1
                if ((W = V / dl) < f1 )  return(k1 + (int)(V/f1));

                // computation of candidate X < k2, and its counterpart Y > k2
                // either squeeze-acceptance of X or acceptance-rejection of Y
                Dk = (int)(dl * ColtRandomSource_raw(current)) + 1;
                if (W <= f2 - Dk * (f2 - f2/r2)) {            // quick accept of
                    return(k2 - Dk);                          // X = k2 - Dk
                }
                if ((V = f2 + f2 - W) < 1.0) {                // quick reject of Y
                    Y = k2 + Dk;
                    if (V <= f2 + Dk * (1.0 - f2)/(dl + 1.0)) {// quick accept of
                        return(Y);                             // Y = k2 + Dk
                    }
                    if (V <= ColtRandomSource_PoissonHelper(Y, l_my, c_pm)) {
                        return(Y);    // final accept of Y
                    }
                }
                X = k2 - Dk;
            }
            else if (U < p4) {                                 // centre right
                // immediate acceptance region R3 = [m, k4+1)*[0, f4), X = m, ... k4
                if ((V = U - p3) < 0.0)  return(k4 - (int)((U - p2)/f4));
                // immediate acceptance region R4 = [k4+1, k5+1)*[0, f5)
                if ((W = V / dr) < f5 )  return(k5 - (int)(V/f5));

                // computation of candidate X > k4, and its counterpart Y < k4
                // either squeeze-acceptance of X or acceptance-rejection of Y
                Dk = (int)(dr * ColtRandomSource_raw(current)) + 1;
                if (W <= f4 - Dk * (f4 - f4*r4)) {             // quick accept of
                    return(k4 + Dk);                           // X = k4 + Dk
                }
                if ((V = f4 + f4 - W) < 1.0) {                 // quick reject of Y
                    Y = k4 - Dk;
                    if (V <= f4 + Dk * (1.0 - f4)/ dr) {       // quick accept of
                        return(Y);                             // Y = k4 - Dk
                    }
                    if (V <= ColtRandomSource_PoissonHelper(Y, l_my, c_pm))
                        return(Y);    // final accept of Y
                }
                X = k4 + Dk;
            }
            else {
                W = ColtRandomSource_raw(current);
                if (U < p5)        {                                  // expon. tail left
                    Dk = (int)(1.0 - log(W)/ll);
                    if ((X = k1 - Dk) < 0)  continue;          // 0 <= X <= k1 - 1
                    W *= (U - p4) * ll;                        // W -- U(0, h(x))
                    if (W <= f1 - Dk * (f1 - f1/r1))  return(X); // quick accept of X
                }
                else {                                         // expon. tail right
                    Dk = (int)(1.0 - log(W)/lr);
                    X  = k5 + Dk;                              // X >= k5 + 1
                    W *= (U - p5) * lr;                        // W -- U(0, h(x))
                    if (W <= f5 - Dk * (f5 - f5*r5))  return(X); // quick accept of X
                }
            }

            // acceptance-rejection test of candidate X from the original area
            // test, whether  W <= f(k),    with  W = U*h(x)  and  U -- U(0, 1)
            // log f(X) = (X - m)*log(my) - log X! + log m!
            if (log(W) <= X * l_my - ColtRandomSource_LogFactorial(X) - c_pm)  return(X);

        }
    }
    else { // mean is too large
#ifdef sun
        if (IsINF(mean)) {
#else
        if (isinf(mean)) {
#endif
            // If the input mean is infinity, return max int.
            // In C, casting infinity to an int returns -2147483648, which
            // is different than Java, which returns 2147483647.
             return INT_MAX;
        }
        return (int) my;
    }
}
/**/


