#include <stdio.h>
#include "CCodeWrapper.h"

/*****************************************************************************/

double middle, upper, lower;

/*****************************************************************************/

void task(){
    double tmpUpper, tmpLower;fprintf(stderr, "0 ");

	callback(-1, 0.1);fprintf(stderr, "1");

	tmpUpper = upper;fprintf(stderr, "2 ");

	callback(0.2, 0.1);fprintf(stderr, "3 ");

	tmpLower = lower;fprintf(stderr, "4 ");

	callback(0.2, 0.3);
	middle = (tmpUpper + tmpLower) / 2;
	callbackV(1, 0, "middle", middle);
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
	 callback(-1, 0);
	 activateTask(1);
	 callback(0.2, 0);
	 terminateTask();
}



