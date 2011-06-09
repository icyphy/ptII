#include <stdio.h>
#include "CCodeWrapper.h"

/*****************************************************************************/

int g1, g2, g3;

/*****************************************************************************/

void top8ms_offset0(){
	callback(-1, 1);
	float r=0;
	int i, temp=2.389732592;

	for(i=0;i<10;i++)
		temp=temp*3.1415;

	callback(2, 0.2);
l1:	temp=g1;
	temp=temp+2;
	callback(0.5, 0.1);
l2:	g2=temp;
	callback(0.2, 0);
	terminateTask();

}

/*****************************************************************************/

void top8ms_offset4(){ 
    int i = 0;
	callback(-1, 0.1); 
	for (i = 0; i < 10; i++) {
		callback(0.2, 0.1);
l3:		g3=g2*2;
	}
	callback(0.2, 0);
	terminateTask();
}


/*****************************************************************************/

void eventf(){
	callback(-1, 1);
	callback(2, 0);
l4:	g1=g1-10;
	callback(0.1, 0);
	terminateTask();
}


void irsa() {
	fprintf(stderr, "IRSA "); 
	 callback(-1, 0);
	 activateTask(1);
	 callback(0.2, 0);
	 terminateTask();
}

void irsb() {
	 fprintf(stderr, "IRSB "); 
	 callback(-1, 0);
	 activateTask(2);
	 callback(0.2, 0);
	 terminateTask();
     return;
 }
 
void irsc() { 
	 fprintf(stderr, "IRSC "); 
	 callback(-1, 0);
	 activateTask(3);
	 callback(0.2, 0);
	 terminateTask();
     return;
}


