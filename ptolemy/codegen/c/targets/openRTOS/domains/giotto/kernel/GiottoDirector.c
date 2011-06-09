/***initLCD***/
#ifndef _initlcd_
#define _initlcd_
RIT128x96x4Init(1000000);
#endif
/**/

/***preinitBlock***/
#include "semphr.h"
/**/

/***declareTaskHandle($name)***/
 xTaskHandle $name_task;
 /**/

 /***declareSemaphoreHandle($name)***/
 xSemaphoreHandle $name;
 /**/

/***createTask($name, $stackSize, $priority)***/
xTaskCreate($name, "$name", $stackSize, NULL,$priority,$name_task);
/**/

/***createCountingSemaphore($name, $maxCount, $initialValue)***/
$name_handle = xSemaphoreCreateCounting($maxCount,$initialValue );
/**/

/***createBinarySemaphore($name)***/
vSemaphoreCreateBinary($name);
/**/

/***createSchedulerThread($period)***/
 static void scheduler(void * pvParameters){
  portTickType xLastWakeTime;
  const portTickType xFrequency = ($period)/portTICK_RATE_MS;
  xLastWakeTime = xTaskGetTickCount();
  for(;;){
   vTaskDelayUntil(&xLastWakeTime,xFrequency);
  //handle updates, mode switches, and switching the double buffer pointers
  }
}
/**/

/***createFrequencyThread($period,$frequency)***/
static void frequency$frequency(void * pvParameters){
portTickType xLastWakeTime;
const portTickType xFrequency = ($period)/ frequency/portTICK_RATE_MS;
xLastWakeTime = xTaskGetTickCount();
for(;;){
vTaskDelayUntil(&xLastWakeTime,xFrequency);
//call the methods for the tasks at this frequency
}
/**/



/*** driverCode ($actorName, $code)***/
void $actorName() {
           $code
}
/**/

/*** updatePort($sinkPort, $srcPort) ***/
        $sinkPort = $srcPort;
/**/

/*** mainCode ***/
g_ulSystemClock = SysCtlClockGet();
vTaskStartScheduler();
/**/
