#include <stdio.h>
#include "CCodeWrapper.h"

/*****************************************************************************/

double middle, upper, lower;

/*****************************************************************************/

void task(){
    double tmpUpper, tmpLower;

	callbackI(-1.0, 0.1, "upper");

	tmpUpper = upper;

	callbackI(0.2, 0.1, "lower");

	tmpLower = lower;

	callback(0.2, 0.3);
	middle = (tmpUpper + tmpLower) / 2;
	callbackO(1.0, 0.0, "middle", middle);
	terminateTask();
}



void setGlobalVariable(char * name, double d) {
	if (strcmp(name, "lower") == 0)
		lower = d;
	else if (strcmp(name, "upper") == 0)
		upper = d;
}

void irs() {
	 fprintf(stderr, "IRS Task "); 
	 callback(-1.0, 0.0);
	 activateTask(1);
	 callback(0.2, 0.0);
	 terminateTask();
}



