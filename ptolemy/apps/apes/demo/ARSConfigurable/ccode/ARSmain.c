/*
 * File: ert_main.c
 *
 * Real-Time Workshop code generated for Simulink model carModel.
 *
 * Model version                        : 1.0
 * Real-Time Workshop file version      : 6.0  (R14)  05-May-2004
 * Real-Time Workshop file generated on : Tue Jan 20 15:04:51 2009
 * TLC version                          : 6.0 (Apr 27 2004)
 * C source code generated on           : Tue Jan 20 15:04:51 2009
 *
 * You can customize this banner by specifying a different template.
 */

#include <stdio.h>                      /* This ert_main.c example uses printf/fflush */
#include <stdlib.h> 
#include "dynamicsControll_ert_rtw/dynamicsControll.h" 
#include "motorController_ert_rtw/motorController.h" 
#include "rtwtypes.h"                   /* MathWorks types */
#include "OSEKCodeWrapper.h"
#include "APESCodeWrapper.h"
#include "ARS_OSEK.h"
#include "ARSmain.h"

enum OSEKTasks
{
	appDispatcherTask, dynamicsControllerTask, motorControllerTask
};

enum OSEKEvents
{
	appDispatcherEvent = 0x01, motorControllerEvent=0x02

};

/*****************************************************************************/
static boolean_T OverrunFlag = 0;
double *speed, *front_angle, *rear_angle;
int simStep=0;
int inputSize=0;
bool isExe=FALSE;
bool appRunning=FALSE;

/*****************************************************************************/

int_T main(int_T argc, const char_T *argv[])
{
  return 0;
}
/*****************************************************************************/

int appStartup(){

	int i;
	FILE *inf;


  fprintf(stderr,"Running application startup...\n");
  fflush(stderr);

  fprintf(stderr,"Reading model inputs...\n");
  fflush(stderr);

  inf = fopen("/Users/derler/Documents/workspace/ptII_svn/ptolemy/apps/apes/demo/ARSConfigurable/input_data.txt","r");
  if (inf == NULL){
	  fprintf(stderr,"Error opening the input file. Exiting... \n");
	  fflush(stderr);
	  return 1;
  }
  fscanf(inf,"%d", &inputSize);
  fprintf(stderr,"Input size: %d\n",inputSize);
  fflush(stderr);
  speed = (double *)malloc(inputSize*sizeof(double));
  front_angle = (double *)malloc(inputSize*sizeof(double));
  rear_angle = (double *)malloc(inputSize*sizeof(double));

  if((speed == NULL)||(front_angle==NULL)||(rear_angle==NULL)){
	  fprintf(stderr, "Memory allocation error. Exiting... \n");
	  fflush(stderr);
      return 1;
  }

  for(i=0;i<inputSize;i++){
	  fscanf(inf,"%lf",&(speed[i]));
  }

  for(i=0;i<inputSize;i++){
	  fscanf(inf,"%lf",&(front_angle[i]));
  }
  fclose(inf);

  /* Initialize model */
  dynamicsControll_initialize(1);
  motorController_initialize(1); 
  appRunning=TRUE;
  if(!isExe){
	  ActivateTask(motorControllerTask);
	  ActivateTask(appDispatcherTask);
  }
  fprintf(stderr,"Application startup done!\n");
  fflush(stderr);
  
  return 0;
}
/*****************************************************************************/

/* This should be connected to a trigger with the base rate */
/* OSEK task activated at startup */
void appDispatcher(){ 
	callback(-1,0);
	while (appRunning){ 
		callbackO(0.01, 0, "frontAngle", front_angle[simStep]);
		callbackO(0.01, 0, "speedProfile", speed[simStep]);
		callback(0.08,0);
		WaitEvent(appDispatcherEvent);
		callback(0.01,0);
		ClearEvent(appDispatcherEvent);
		
		if(simStep%5 == 0){
			callback(0.1,0);
			ActivateTask(dynamicsControllerTask);
		}
		
		callback(0.1,0);
		SetEvent(motorControllerTask, motorControllerEvent);
		//carModel_step();
		simStep++;
		
	}
	
	callback(1,0);
	TerminateTask();

}
/*****************************************************************************/

void dynaController(){
		callback(-1,0);
	dynamicsControll_step();
		callback(1,0);
	TerminateTask();
}
/*****************************************************************************/

void motorController(){

			callback(-1,0);

	while (appRunning){
			callback(0.1,0);
			WaitEvent(motorControllerEvent);
			callback(0.01,0);
			ClearEvent(motorControllerEvent);
			motorController_step();
	}
			callback(1,0);

	TerminateTask();	
}
/*****************************************************************************/

/* Interrupt service routine */

void dispatcherIRS(){

		callback(-1,0);

	if (appRunning){
		callback(0.1,0);
		SetEvent(appDispatcherTask, appDispatcherEvent);
	}
		callback(0.1,0);
	TerminateTask();
}
/*****************************************************************************/
