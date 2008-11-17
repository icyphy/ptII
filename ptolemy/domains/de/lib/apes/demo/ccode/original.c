#include <stdio.h>
#include "CCodeWrapper.h"

/*****************************************************************************/

int g1, g2, g3;

/*****************************************************************************/

void top8ms_offset0(){

	float r=0;
	int i, temp=2.389732592;

	for(i=0;i<10;i++)
		temp=temp*3.1415;

	callback("top8ms_offset0", 1, 2);
l1:	temp=g1;
	temp=temp+2;
	callback("top8ms_offset0", 2, 0.5);
l2:	g2=temp;
	callback("top8ms_offset0", 0, 0);

}

/*****************************************************************************/

void top8ms_offset4(){
	callback("top8ms_offset4", 1, 0);
	callback("top8ms_offset4", 2, 1.5);
l3:	g3=g2*2;
	callback("top8ms_offset4", 0, 0);
}


/*****************************************************************************/

void eventf(){
	callback("eventf", 1, 0);
	callback("eventf", 2, 2);
l4:	g1=g1-10;
	callback("eventf", 0, 0);
}

int getG1(){
	return g1;
}
int getG2() {
	return g2;
}
int getG3() {
	return g3;
}
