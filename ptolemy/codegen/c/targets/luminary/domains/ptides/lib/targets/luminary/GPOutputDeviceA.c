/***preinitBlock***/
int $actorSymbol(result) = 0;
//FIXME: This line does not appear for some reason...
/**/

/*** sharedBlock ***/
//GPIOA_Transmitter sharedBlock
/**/

/*** initBlock ***/
//GPIOA_Transmitter initBlock
/**/

/*** fireBlock($actuator) ***/
Time currentPhysicalTime;

getRealTime(&currentPhysicalTime);
 
// Compare time with tagged time, if not safe to actuate, then add events
// and wait until time to actuate. If safe to actuate, actuate now.
if (timeCompare(currentModelTime, currentPhysicalTime) == MORE) {
	/* This can be used for debugging to determine when events are received 
		by an actuator
	if (thisActuator == ACTUATORS[0]) {
		GPIOPinWrite(GPIO_PORTC_BASE, GPIO_PIN_7, GPIO_PIN_7);
	} else if (thisActuator == ACTUATORS[1]) {
	 	GPIOPinWrite(GPIO_PORTA_BASE, GPIO_PIN_7, GPIO_PIN_7);
	}*/
	
	setActuationInterrupt($actuator);

	/*	This clears the GPIO pins after debugging information has been set
		FIXME: This is extraneous. This can be removed.
	if (thisActuator == ACTUATORS[0]) {
		GPIOPinWrite(GPIO_PORTC_BASE, GPIO_PIN_7, 0);
	} else if (thisActuator == ACTUATORS[1]) {
	 	GPIOPinWrite(GPIO_PORTA_BASE, GPIO_PIN_7, 0);
	}*/
			
}											   
else
{
	// FIXME: do something!
	/*	Debug - write to GPIO on deadline miss
	GPIOPinWrite(GPIO_PORTC_BASE,GPIO_PIN_7,GPIO_PIN_7);
	while (i < 999) {
		i++;
	}*/

	#ifdef LCD_DEBUG
	sprintf(str,"dead miss");
	RIT128x96x4StringDraw(str, 0,90,15);
	#endif
}
#ifdef LCD_DEBUG
//sprintf(str,"actuatorfired %d",fireActuatorCount);
//RIT128x96x4StringDraw(str, 0,60,15);
#endif	   
/**/
 
/*** actuationBlock($pin) ***/
$actorSymbol(result) ^= $actorSymbol(result);	//Toggle pin value
GPIOPinWrite(GPIO_PORTA_BASE,GPIO_PIN_$pin,GPIO_PIN_$pin*$actorSymbol(result));
/**/
