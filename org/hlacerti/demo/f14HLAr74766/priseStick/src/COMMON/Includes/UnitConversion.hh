/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

 Header:       UnitConversion.hh
 Authors:      Jean-Baptiste Chaudron & David Saussié

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
SENTRY
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

#ifndef UNIT_CONVERSION_HH
#define UNIT_CONVERSION_HH

/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
INCLUDES
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

#include <float.h>
#include <queue>
#include <string>
#include <sstream>
#include <cmath>
#include <cstdlib>


using std::fabs;
using std::string;

#ifndef M_PI
#  define M_PI 3.1415926535897932384626433832795028841971 
#endif

#if !defined(WIN32) || defined(__GNUC__) || (defined(_MSC_VER) && (_MSC_VER >= 1300))
  using std::max;
#endif


/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
FORWARD DECLARATIONS
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/


/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
CLASS DOCUMENTATION
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

/** JSBSim Base class.
*   This class provides universal constants, utility functions, messaging
*   functions, and enumerated constants to JSBSim.
    @author Jon S. Berndt
    @version $Id$
*/

/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
CLASS DECLARATION
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

class UnitConversion {
    
public:
  /// Constructor for FGJSBBase.
  UnitConversion() {};

  /// Destructor for FGJSBBase.
  ~UnitConversion() {};

  /** Converts from degrees Kelvin to degrees Fahrenheit. */
  static double KelvinToFahrenheit (double kelvin) {
    return 1.8*kelvin - 459.4;
  }
  /** Converts from degrees Celsius to degrees Rankine. */
  static double CelsiusToRankine (double celsius) {
    return celsius * 1.8 + 491.67;
  }
  /** Converts from degrees Rankine to degrees Celsius. */
  static double RankineToCelsius (double rankine) {
    return (rankine - 491.67)/1.8;
  }
  /** Converts from degrees Kelvin to degrees Rankine. */
  static double KelvinToRankine (double kelvin) {
    return kelvin * 1.8;
  }
  /** Converts from degrees Rankine to degrees Kelvin. */
  static double RankineToKelvin (double rankine) {
    return rankine/1.8;
  }
  /** Converts from degrees Fahrenheit to degrees Celsius. */
  static double FahrenheitToCelsius (double fahrenheit) {
    return (fahrenheit - 32.0)/1.8;
  }
  /** Converts from degrees Celsius to degrees Fahrenheit. */
  static double CelsiusToFahrenheit (double celsius) {
    return celsius * 1.8 + 32.0;
  }
  /** Converts from degrees Celsius to degrees Kelvin. */
  static double CelsiusToKelvin (double celsius) {
    return celsius + 273.15;
  }
  /** Converts from degrees Kelvin to degrees Celsius. */
  static double KelvinToCelsius (double kelvin) {
    return kelvin - 273.15;
  }
  /** Finite precision comparison.
      @param a first value to compare
      @param b second value to compare
      @return if the two values can be considered equal up to roundoff */
  static bool EqualToRoundoff(double a, double b) {
    double eps = 2.0*DBL_EPSILON;
    return fabs(a - b) <= eps*max(fabs(a), fabs(b));
  }
  /** Finite precision comparison.
      @param a first value to compare
      @param b second value to compare
      @return if the two values can be considered equal up to roundoff */
  static bool EqualToRoundoff(float a, float b) {
    float eps = 2.0*FLT_EPSILON;
    return fabs(a - b) <= eps*max(fabs(a), fabs(b));
  }
  /** Finite precision comparison.
      @param a first value to compare
      @param b second value to compare
      @return if the two values can be considered equal up to roundoff */
  static bool EqualToRoundoff(float a, double b) {
    return EqualToRoundoff(a, (float)b);
  }
  /** Finite precision comparison.
      @param a first value to compare
      @param b second value to compare
      @return if the two values can be considered equal up to roundoff */
  static bool EqualToRoundoff(double a, float b) {
    return EqualToRoundoff((float)a, b);
  }
  /** Constrain a value between a minimum and a maximum value.
  */
  static double Constrain(double min, double value, double max) {
    return value<min?(min):(value>max?(max):(value));
  }
  
  static double sign(double num) {return num>=0.0?1.0:-1.0;}

  // Moments L, M, N
  enum {eL=1,eM,eN};
  // Rates P, Q, R
  enum {eP=1,eQ,eR};
  // Velocities U, V, W
  enum {eU=1,eV,eW};
  // Positions X, Y, Z
  enum {eX=1,eY,eZ};
  // Euler angles Phi, Theta, Psi
  enum {ePhi=1,eTheta,ePsi};
  // Stability axis forces, Drag, Side force, Lift
  enum {eDrag=1,eSide,eLift};
  // Local frame orientation Roll, Pitch, Yaw
  enum {eRoll=1,ePitch,eYaw};
  // Local frame position North, East, Down
  enum {eNorth=1,eEast,eDown};
  // Locations Radius, Latitude, Longitude
  enum {eLat=1,eLong,eRad};
  // Conversion specifiers
  enum {inNone=0,inDegrees,inRadians,inMeters,inFeet};


protected:
 
  static const double radtodeg;
  static const double degtorad;
  static const double hptoftlbssec;
  static const double psftoinhg;
  static const double psftopa;
  static const double fpstokts;
  static const double ktstofps;
  static const double inchtoft;
  static const double in3tom3;
  static const double m3toft3;
  static const double inhgtopa;
  static const double fttom;
  static const double mtoft;
  static double Reng;         // Specific Gas Constant,m^2/(sec^2*K)
  static const double SHRatio;
  static const double lbtoslug;
  static const double slugtolb;
  static const double kgtolb;
  static const double kgtoslug;

  static const double SLtemperature;
  static const double SLpressure;
  static const double SLdensity;
  static const double SLgravity;

  static double GaussianRandomNumber(void)
   {
    static bool phase = false;
    static double storedX;
    double V1, V2, S, X, polar;

    V1 = V2 = X = 0.0;

    if (!phase) {
      do {
        V1 = 2.0 * double(rand())/double(RAND_MAX) - 1.0;
        V2 = 2.0 * double(rand())/double(RAND_MAX) - 1.0;
        S = V1 * V1 + V2 * V2;
      } while(S >= 1 || S == 0);
      
      polar = sqrt(-2 * log(S) / S);
      storedX = V1 * polar;
      phase = true;
      X = V2 * polar;
    } 
    else {
      X = storedX;
      phase = false;
    }
    return X;
   }


};


//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
#endif

