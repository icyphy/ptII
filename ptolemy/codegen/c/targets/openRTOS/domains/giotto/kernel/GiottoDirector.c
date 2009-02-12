/***initLCD***/
// put the initialization code here.. ie.. rti init,
 RIT128x96x4Init(1000000);
/**/

/***preinitBlock***/
 xTaskHandle schedulerhandle;
/**/
/***declareTaskHandle($name)***/
    xTaskHandle $actorSymbol($name_handle);
/**/

/***createTask($name, $stackSize, $priority)***/
xTaskCreate($actorSymbol($name), "$actorSymbol($name)", $stackSize, NULL,$priority, $actorSymbol($name_handle));
/**/

/***createSchedulerThread($period)***/
 static void scheduler(void * pvParameters){
  portTickType xLastWakeTime;
  const portTickType xFrequency = ($period*1000)/portTICK_RATE_MS;
  xLastWakeTime xTaskGetTickCount();
  for(;;){
   vTaskDealUntil(&xLastWakeTime,xFrequency);
  //handle updates, mode switches, and switching the double buffer pointers
  }
}
/**/

/***createFrequencyThread($period,$frequency)***/
static void frequency$frequency(void * pvParameters){
portTickType xLastWakeTime;
const portTickType xFrequency = ($period*1000)/ frequency/portTICK_RATE_MS;
xLastWakeTime xTaskGetTickCount();
for(;;){
vTaskDealUntil(&xLastWakeTime,xFrequency);
//call the methods for the tasks at this frequency
}
/**/







