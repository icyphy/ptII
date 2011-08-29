
/*** fireBlock($Letter) ***/
	actNs$Letter[actWr$Letter] = currentModelTime.nsecs - nanoSeconds;
	if((MTU20.TIOR.BIT.IO$Letter == 0) && (actNs$Letter[actWr$Letter] < ((4*divideByValue/2)*(65536 + intDel)))) {
		MTU20.TGR$Letter = actNs$Letter[actWr$Letter]/(4*divideByValue/2);
		MTU20.TSR.BIT.TGF$Letter = 0;
		MTU20.TIER.BIT.TGIE$Letter = 1;
		if(actSt$Letter == 0)
			MTU20.TIOR.BIT.IO$Letter = 2;
		else
			MTU20.TIOR.BIT.IO$Letter = 5;
	}
	actWr$Letter = actWr$Letter+1;
	if(actWr$Letter == 10)
		actWr$Letter = 0;

	actNs$Letter[actWr$Letter] = currentModelTime.nsecs + actWidth - nanoSeconds;
	actWr$Letter = actWr$Letter+1;
	if(actWr$Letter == 10)
		actWr$Letter = 0;
/**/
