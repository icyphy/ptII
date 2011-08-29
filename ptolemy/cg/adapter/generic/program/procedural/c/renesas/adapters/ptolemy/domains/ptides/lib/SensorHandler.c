/*** fireBlock($Letter) ***/
	PB.DR.BIT.B9 = 1;

	while(MTU20.TSR.BIT.TGF$Letter != 1)
		;

	MTU20.TSR.BIT.TGF$Letter = 0;

	set_imask(15);

  	xxx = nanoSeconds + MTU20.TGR$Letter*(4*divideByValue/2);

	if(MTU20.TGR$Letter > MTU20.TCNT) {
		xxx = xxx - (4*divideByValue/2)<<16;
	}

	if(xxx <= 1000000000) {
		currentModelTime.secs = Seconds;
		currentModelTime.nsecs = xxx;
	}
	else {
		currentModelTime.secs = Seconds + 1;
		currentModelTime.nsecs = xxx-1000000000;
	}

	$put(output#0, 1);

	set_imask(0);

	PB.DR.BIT.B9 = 0;

	processEvents();
/**/

