/*** fireBlock($Letter, $timerNumber) ***/

        while(MTU2$timerNumber.TSR.BIT.TGF$Letter != 1) // change to MTU23
                ;

        MTU2$timerNumber.TSR.BIT.TGF$Letter = 0;
        set_imask(15);
          xxx = nanoSeconds + MTU2$timerNumber.TGR$Letter*(4*divideByValue/2);
        if(MTU2$timerNumber.TGR$Letter > MTU2$timerNumber.TCNT) {
                xxx = xxx - (4*divideByValue/2)*65536;
        }
        if(xxx <= 1000000000) {
                currentModelTime.secs = Seconds;
                currentModelTime.nsecs = xxx;
        } else {
                currentModelTime.secs = Seconds + 1;
                currentModelTime.nsecs = xxx-1000000000;
        }
        $put(output#0, 1);

        set_imask(0);
        processEvents();
/**/

