/***initLCD***/
#ifndef _initlcd_
#define _initlcd_
RIT128x96x4Init(1000000);
#endif
/**/

/***preinitBlock***/

/**/
/***declareTaskHandle($name)***/
 xTaskHandle $name_handle;
 /**/

/***createTask($name, $stackSize, $priority)***/
xTaskCreate($name, "$name", $stackSize, NULL,$priority,$name_handle);
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
