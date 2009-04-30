/*** preinitBlock($zero) ***/
int $actorSymbol(result) = 0;
//FIXME: This line does not appear for some reason...
/**/

/*** fireBlock($pin) ***/
$actorSymbol(result) ^= $actorSymbol(result);	//Toggle pin value
GPIOPinWrite(GPIO_PORTA_BASE,GPIO_PIN_$pin,GPIO_PIN_$pin*$actorSymbol(result));
/**/