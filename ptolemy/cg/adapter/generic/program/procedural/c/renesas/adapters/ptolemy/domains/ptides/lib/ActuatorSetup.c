
/*** fireBlock($Letter, $timerNumber) ***/
                actNs$Letter[actWr$Letter] = currentModelTime.nsecs - nanoSeconds;
                if (currentModelTime.nsecs < nanoSeconds) {
                        actS$Letter[actWr$Letter]--;
                        actNs$Letter[actWr$Letter] = currentModelTime.nsecs + 1000000000 - nanoSeconds;
            } else {
                        actNs$Letter[actWr$Letter] = currentModelTime.nsecs - nanoSeconds;
            }
                if((MTU2$timerNumber.TIOR.BIT.IO$Letter == 0) && (actS$Letter[actWr$Letter] == 0) &&
                                (actNs$Letter[actWr$Letter] < ((4*divideByValue/2)*(65536 + intDel)))) {
                MTU2$timerNumber.TGR$Letter = actNs$Letter[actWr$Letter]/(4*divideByValue/2);
                MTU2$timerNumber.TSR.BIT.TGF$Letter = 0;
                MTU2$timerNumber.TIER.BIT.TGIE$Letter = 1;
                if(actSt$Letter == 0)
                MTU2$timerNumber.TIOR.BIT.IO$Letter = 2;
                else
                MTU2$timerNumber.TIOR.BIT.IO$Letter = 5;
            }
            actWr$Letter = actWr$Letter+1;
            if(actWr$Letter == 10)
            actWr$Letter = 0;
            actS$Letter[actWr$Letter] = currentModelTime.secs - Seconds;
                if (currentModelTime.nsecs + actWidth < nanoSeconds) {
                        actS$Letter[actWr$Letter]--;
                        actNs$Letter[actWr$Letter] = currentModelTime.nsecs + actWidth + 1000000000 - nanoSeconds;
                } else {
                        actNs$Letter[actWr$Letter] = currentModelTime.nsecs + actWidth - nanoSeconds;
                }
            actWr$Letter = actWr$Letter+1;
            if(actWr$Letter == 10)
            actWr$Letter = 0;
/**/

