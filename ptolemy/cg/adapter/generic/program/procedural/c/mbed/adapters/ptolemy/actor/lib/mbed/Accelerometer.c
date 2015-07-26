/***preinitBlock***/
//#include "mbed.h"
#include "FXOS8700Q.h"
FXOS8700Q_acc acc( PTE25, PTE24, FXOS8700CQ_SLAVE_ADDR1);
/**/

/***initBlock***/
acc.enable();
/**/

/***fireBlock***/
// The following is ordinary C code, except for
// the macro references to the input and output
// ports.
float faX, faY, faZ;
acc.getX(&faX);
acc.getY(&faY);
acc.getZ(&faZ);
$put(x, faX);
$put(y, faY);
$put(z, faZ);
/**/

/***wrapupBlock***/
/**/


