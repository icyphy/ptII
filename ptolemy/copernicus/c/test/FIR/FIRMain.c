
/* Main program that accompanies the FIR.java test 

   @author Shuvra S. Bhattacharyya 
   @version $Id$
*/

#include "FIR.h"
#include <stdio.h>

#include "FIRconfig.h"

int main(void) {

    float input[10] = INPUT_VALUES;
    float output[10] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    float data[20];
    float taps[NUM_TAPS] = TAP_VALUES;
    struct i69615_FIR fir;
    int i;

    f0651603000_initialize(&fir, taps, NUM_TAPS, data, INTERP, DEC, DECPHASE);
    f01625738578_fire(&fir, input, output);

    for (i=0; i<10; i++) {
        printf("%f\n", output[i]);
    }

    return 0;
}
