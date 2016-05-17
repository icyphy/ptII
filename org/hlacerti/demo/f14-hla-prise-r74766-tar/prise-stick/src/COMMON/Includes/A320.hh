/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

 Header:       A320.hh
 Authors:      Jean-Baptiste Chaudron & David Saussié

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
SENTRY
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

#ifndef A320_HH
#define A320_HH

/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
INCLUDES
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

#include <float.h>
#include <queue>
#include <string>
#include <sstream>
#include <cmath>
#include <cstdlib>
#include "FGColumnVector3.hh"


using std::fabs;
using std::string;

#ifndef M_PI
#  define M_PI 3.1415926535897932384626433832795028841971 
#endif

#if !defined(WIN32) || defined(__GNUC__) || (defined(_MSC_VER) && (_MSC_VER >= 1300))
  using std::max;
#endif

using namespace std;


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
    @version $Id: FGJSBBase.h,v 1.24 2009/06/13 02:41:58 jberndt Exp $
*/

/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
CLASS DECLARATION
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

class A320 {
    
public:
  /// Constructor for FGJSBBase.
  A320() {};

  /// Destructor for FGJSBBase.
  ~A320() {};


protected:
 
// Dimensions

  static const double wingarea; 
  static const double wingspan;
  static const double chord;    
  static const double htailarea;
  static const double htailarm; 
  static const double vtailarea; 
  static const double vtailarm;  

// Mass and Inertia

  static const double EmptyMass; 
  static const double FullMass;  
  static const double Ixx;       
  static const double Iyy;      
  static const double Izz;      
  static const double Ixy;       
  static const double Ixz;      
  static const double Iyz;      

// CG Location

  static const FGColumnVector3 CG;

// Engine location

  static const FGColumnVector3 LeftEngineLocation; 
  static const FGColumnVector3 RightEngineLocation;

// Aerodynamic coefficients

// Drag 
  static const double CD0;
  static const double CDalpha[27][5]; 
  static const double CDde; 
  static const double CDbeta;
  static const double CDgear;
  static const double CDspeedbrake;

// Side
  static const double CYb[3][2];
  
// Lift
  static const double CLalpha[18][6];
  static const double CLde;

// Roll
  static const double Clb[3][2];
  static const double Clp; 
  static const double Clr;
  static const double Clda;
  static const double Cldr;

// Pitch

  static const double Cm0[2][2];
  static const double Cmalpha;
  static const double Cmde;
  static const double Cmq;
  static const double Cmalphadot;

// Yaw

  static const double Cnr;
  static const double Cnb;
  static const double Cndr;


};


//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
#endif

