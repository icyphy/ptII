/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

 Module:       UnitConversion.cc
 Authors:      Jean-Baptiste Chaudron & David Saussié

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
INCLUDES
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

#define BASE

#include "UnitConversion.hh"


/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
CLASS IMPLEMENTATION
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/


const double UnitConversion::radtodeg      = 180/M_PI;
const double UnitConversion::degtorad      = 1.0/radtodeg;
const double UnitConversion::hptoftlbssec  = 550.0;
const double UnitConversion::psftoinhg     = 0.014139032;
const double UnitConversion::psftopa       = 47.880259;
const double UnitConversion::fpstokts      = 0.5924838;
const double UnitConversion::ktstofps      = 1.0/fpstokts;
const double UnitConversion::inchtoft      = 0.08333333;
const double UnitConversion::in3tom3       = 1.638706E-5;
const double UnitConversion::m3toft3       = 1.0/(fttom*fttom*fttom);
const double UnitConversion::inhgtopa      = 3386.38;
const double UnitConversion::fttom         = 0.3048;
const double UnitConversion::mtoft         = 1.0/fttom;
      double UnitConversion::Reng          = 287.05;
const double UnitConversion::SHRatio       = 1.40;
const double UnitConversion::slugtolb      = 32.174049;
const double UnitConversion::lbtoslug      = 1.0/slugtolb;
const double UnitConversion::kgtolb        = 2.20462;
const double UnitConversion::kgtoslug      = 0.06852168;

const double UnitConversion::SLtemperature = 288.15;   // kelvin
const double UnitConversion::SLpressure    = 101325.0; // Pa
const double UnitConversion::SLdensity     = 1.225;    // kg/m^3
const double UnitConversion::SLgravity     = 9.80665;  // m/s^2



//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%



