#include <stdio.h>
#include "CCodeWrapper.h"

/*****************************************************************************/

double middle, upper, lower;

/*****************************************************************************/

void task(){
    double tmpUpper, tmpLower;
	callback(-1, 0.1);
	tmpUpper = upper;
	callback(0.2, 0.1);
	tmpLower = lower;
	callback(0.2, 0.3);
	middle = (tmpUpper + tmpLower) / 2;
	callbackV(1, 0, "middle", middle);
	terminateTask();
}



void setLower(double l) {
	lower = l;
}

void setUpper(double u) {
	upper = u;
}



