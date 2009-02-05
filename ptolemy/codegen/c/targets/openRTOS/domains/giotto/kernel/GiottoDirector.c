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
xTaskCreate($actorSymbol($name), "$actorSymbol($name)", $stackSize, NULL, $priority, $actorSymbol($name_handle));
/**/
