package java.lang;

/**
 * Mathematical functions.
 *
 * @author <a href="mailto:bbagnall@escape.ca">Brian Bagnall</a>
 */
public final class Math {

	private static final float[] DIGIT = {45.0f, 26.56505118f, 14.03624347f, 
		                              7.125016349f, 3.576334375f, 
	                                      1.789910608f, 0.89517371f, 0.447614171f, 
	                                      0.2238105f, 0.111905677f, 0.055952892f, 
	                                      0.027976453f, 0.013988227f, 0.006994114f, 
	                                      0.003497057f};
	
	private static final int[] POWEROF2 = { 1, 2, 4, 8, 16, 32, 64, 128,
	                                        256, 512, 1024, 4096, 8192, 16348,
	                                        32768 };
		
	// Math constants
	public static final double E = 2.718281828459045;
	public static final double PI = 3.141592653589793;
	static final double NaN = 0.0f / 0.0f;

	// These constants are used for method trig()
	private static final byte SIN = 0;
	private static final byte COS = 1;
	private static final byte TAN = 2;

	// Accuracy constant: is used to specify how many digits of
	// accuracy is desired for some methods.
	private static final float ACCURACY = 0.0000001f;

	// Used to generate random numbers.
	private static java.util.Random RAND = new java.util.Random(System.currentTimeMillis());

	//public static boolean isNaN (double d) {
	//  return d != d;
	//}
	
	/**
	* Returns the smallest (closest to negative infinity) double value that is not
	* less than the argument and is equal to a mathematical integer.
 	*/
	public static double ceil(double a) {
		return ((a<0)?(int)a:(int)(a+1));	
	}
	
	/**
	* Returns the largest (closest to positive infinity) double value that is not
	* greater than the argument and is equal to a mathematical integer.
	*/
	public static double floor(double a) {	
		return ((a<0)?(int)(a-1):(int)a);	
	}
	
	/**
	* Returns the closest int to the argument.
	*/	
	public static int round(float a) {	
		return (int)Math.floor(a + 0.5f);
	}
	
	/**
	* Returns the lesser of two integer values.
	*/
	public static int min(int a, int b) {	
		return ((a<b)?a:b);
	}
	
	/**
	*Returns the lesser of two double values.
	*/
	public static double min(double a, double b) {	
		return ((a<b)?a:b);
	}
	
	/**
	*Returns the greater of two integer values.
	*/
	public static int max(int a, int b) {	
		return ((a>b)?a:b);
	}
	
	/**
	*Returns the greater of two double values.
	*/
	public static double max(double a, double b) {	
		return ((a>b)?a:b);
	}
	
	/**
	* Random number generator.
	* Returns a double greater than 0.0 and less than 1.0
	*/
	public static double random()
  {
    final int MAX_INT = 2147483647;
    int n = MAX_INT;
    
    // Just to ensure it does not return 1.0
    while(n == MAX_INT)
    	n = abs (RAND.nextInt());
        
    return n * (1.0 / MAX_INT);
  }
	
	/**
	* Exponential function.  Returns E^x (where E is the base of natural logarithms).
	* Thanks to David Edwards of England for conceiving the code.
	*/	
	public static double exp(double x)
	{
	    double sum = 1;
	    double oldsum = 0;
	    int i = 1;
	    
	    double powTemp = 1;
	    double facTemp = 1;
	
	    while (sum < oldsum - ACCURACY || sum > oldsum + ACCURACY)
	    {
	        oldsum = sum;
	        powTemp *= x;
	        facTemp *= i;
	        sum += powTemp / facTemp;
	        i++;
	    }
	
	    return sum;
	}
	
	/**
	* Natural log function.  Returns log(a) to base E
	* Thanks to David Edwards of England for conceiving the code.
	*/
	public static double log(double a)
	{
	    double best = a;
	    double newx = a + (a / exp(a)) - 1;
	    double oldx = a;
	
	    while (newx < oldx - ACCURACY || newx > oldx + ACCURACY)
	    {
	        oldx = newx;
	        newx = oldx + (a / exp(oldx)) - 1;
	    }
	
	    return newx;
	}

	/**
	* Power function.  This is a slow but accurate method.
	* Thanks to David Edwards of England for conceiving the code.
	*/
	public static double pow(double a, double b) {
		return exp(b * log(a));
	}

// 	/**
// 	* Power function.  This one is faster, but can only do powers of integer.
// 	*/
// 	public static double pow(double a, int b) {
// 		float c = 1.0f;
// 		
// 		if(b==0)
// 			return 1.0;
// 		
// 		if(b > 0) {
// 			for(int i=0;i<b;i++) {
// 				c = c * (float) a;
// 			}
// 		}
// 		else if(b < 0) {
// 			for(int i=0;i>b;i--) {
// 				c = c / (float) a;
// 			}	
// 		}	
// 		
// 		return c;
// 	}
// 
	
	/**
	* Returns the absolute value of a double value. If the argument is not negative, the argument is
  * returned. If the argument is negative, the negation of the argument is returned.
	*/
	public static double abs(double a) {
		return ((a<0)?-a:a);
	}

	/**
	* Returns the absolute value of an integer value. If the argument is not negative, the argument is
  * returned. If the argument is negative, the negation of the argument is returned.
	*/
	public static int abs(int a) {
		return ((a<0)?-a:a);
	}
	
	/**
	* Cosine function.
	*/
	public static double cos(double a) {
		return trig(a, COS);
	}

  /**
	* Sine function.
	*/
	public static double sin(double a) {
		return trig(a, SIN);
	}

	/**
	* Square root function.  Uses Newton-Raphson method.
	*/
	public static double sqrt(double a) {
		// * I'm going to work on a better bestGuess to make it more efficient
		double bestGuess = a;
		double xnew = a;
		
		if(a<0)
			return NaN;
		
		// * Might use a fixed number of loops instead of comparing
		while((xnew*xnew) > (a + ACCURACY)||(xnew*xnew)<(a-ACCURACY))
			xnew = (xnew+bestGuess/xnew)/2;

		return xnew;
	}
	
	/**
  * Tangent function.
	*/
	public static double tan(double a) {
		return trig(a, TAN);
	}
	
	private static double trig(double a, int returnType) {
	 	
   	// This method uses radians input, just like the official java.lang.Math
   	a = (float) toDegrees (a);
   	
   	// ** When a=0, 90, 180, 270 should return even number probably
   	
   	// With positive numbers, subtracting 360 until angle is between 0-360
		while(a >= 360) {
			a = a - 360;
		}
		
		// With negative numbers, add 360 until between 0-360
   	while(a < 0) {
   		a = a + 360;	
   	}
   	   	
   	// Cos is negative in quadrants 2 and 3 (angle 90-270)
   	int cosMult = 1;
   	if((a<270)&&(a>90))
   		cosMult = -1;  		
   	
   	// Sin is negative in quadrants 3 and 4 (angle 180-360)
   	int sinMult = 1;
   	if((a<360)&&(a>180))
   		sinMult = -1;
   	
   	// Transform the starting angle to between 0-90
   	// Since the cordic method is only accurate for angles 0-90, must do
   	// this to handle angles 90-360
   	if(a>=180)
   		a = a - 180;
   		
   	if(a>90)
   		a = 180 - a;
   	
   	// ** The core trig calculations to produce Cos & Sin **
		int N = DIGIT.length - 1;
		float x = 0.607252935f;  // Absolute best accuracy I could calculate with my trusty TI-32 calculator
		float y = 0.0f;
		
		for(int i = 0;i <= N;i++) {
			float dx = x / (float) POWEROF2[i];
    	float dy = y / (float) POWEROF2[i];
    	float da = DIGIT[i];
    	
    	if(a >= 0) {
    		x = x - dy;
    		a = a - da;
    		y = y + dx;
    	}
    	else {
    		x = x + dy;
    		a = a + da;
    		y = y - dx;
    	}
		}  // ** End of core trig calculations **
		
		// Now use multipliers (set at start of routine) to convert sin
		// and cos to +/- (depends on the quadrant):
		y = y * sinMult;
		x = x * cosMult;
		
		if(returnType == SIN)
			return y;
		else if (returnType == COS)
			return x;
		else
			return y/x;
	}
	
	/**
	 * Arc tangent function
	 */
	public static double atan(double a) {
   	int N = DIGIT.length - 1;
		double x = 1.0;
		double y = a;
		double t = 0;
		
		if(a==0)
			return 0.0; // Otherwise returns tiny number
		
		for(int i = 0;i <= N;i++) {		
			double dx = x / POWEROF2[i];
    	double dy = y / POWEROF2[i];
    	double da = DIGIT[i];
    	
    	if(y < 0) {
    		x = x - dy;
    		t = t - da;
    		y = y + dx;
    	}
    	else {
    		x = x + dy;
    		t = t + da;
    		y = y - dx;
    	}
		}
		return toRadians(t);
	}
	/**
	*Converts rectangular coordinates (b, a) to polar (r, theta). This method computes the phase
  *theta by computing an arc tangent of a/b in the range of -pi to pi.
	*/
	public static double atan2(double y, double x) {
		double result = 0.0;
		if(x != 0.0)
			result = atan(y/x);
		else {
			if(y>0)
				return PI/2; // +,0
			else
				if(y<0)
					return -PI/2; // -,0
		}
		
		if(x<0) {
			if(y==0)
				return PI; // 0,-
			else {
				if(y>0)
					return PI + result; // +,-
				else
					return -PI + result; // -,-
			}
		}
		
		return result; // +,+ or -,+ or 0,+
	}
	
	/**
  * Arc cosine function.
	*/
	public static double acos(double a) {
		return atan(sqrt(1-a*a)/a);
	}
	
	/**
  * Arc sine function.
	*/
	public static double asin(double a) {
		return atan(a/sqrt(1-a*a));
	}
	
  /**
  * Converts radians to degrees.
	*/
	public static double toDegrees(double angrad) {
		return angrad * (360.0 / (2 * PI));
	}
	
	/**
	 * Converts degrees to radians.
	 */
	public static double toRadians(double angdeg) {
		return angdeg * ((2 * PI) / 360.0);
	}
}
