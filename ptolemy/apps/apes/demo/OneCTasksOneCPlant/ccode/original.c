#include <stdio.h>
#include "CCodeWrapper.h"

/*****************************************************************************/

double middle, upper, lower;

/*****************************************************************************/

void task(){
	callback(-1, 0.1);
	tmpUpper = upper;
	callback(0.2, 0.1);
	tmpLower = lower;
	callback(0.2, 0.3);
	middle = (tmpUpper + tmpLower) / 2;
	callback(1, 0);
	terminateTask();
}

/*****************************************************************************/

void plant() { 
    lower ++;
	if (lower == 5)
		lower = 0;
	terminateTask();
}


