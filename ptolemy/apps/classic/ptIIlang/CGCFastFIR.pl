defstar {
	name {FastFIR}
	domain {CGC}
	desc {
A Finite Impulse Response (FIR) filter.
Coefficients are in the "taps" state variable.
Default coefficients give an 8th order, linear phase lowpass
filter. To read coefficients from a file, replace the default
coefficients with "fileName".
	}
	version {@(#)CGCFastFIR.pl	1.6 04/08/97}
	author { Soonhoi Ha, Bill Chen, and John Reekie }
	copyright {
Copyright (c) 1990-2005 The Regents of the University of California.
All rights reserved.
See the file $PTOLEMY/copyright for copyright notice,
limitation of liability, and disclaimer of warranty provisions.
	}
	location { CGC main library }
	htmldoc {
This FIR filter produces fast code by eliminating the need for a 
Circular buffer.
	}
	input {
		name {signalIn}
		type {float}
	}
	output {
		name {signalOut}
		type {float}
	}
	defstate {
		name {taps}
		type {floatarray}
		default {
	"-.040609 -.001628 .17853 .37665 .37665 .17853 -.001628 -.040609"
		}
		desc { Filter tap values. }
	}
	defstate {
		name {tapSize}
		type {int}
		default {0}
		attributes { A_NONCONSTANT|A_NONSETTABLE }
	}
	setup {
	        tapSize = taps.size();
		signalIn.setSDFParams(1,0);
		signalOut.setSDFParams(1,0);
	}
	codeblock(mainDecl){
	  int currentValue,i;
	  double src[$val(tapSize)], fir[$val(tapSize)];
	}

	codeblock(initialize){
	  currentValue = 0;
	  for(i=0;i<$val(tapSize);i++){
	    fir[i]=$ref2(taps,i);
	    src[i] = 0.0;
	  }
	}
	initCode{
	  addDeclaration(mainDecl);
	  addCode(initialize);
	}
	codeblock(bodyDecl) {
	  double accum;
	  int nminusk,k;
	}
	codeblock(body) {

	  if(currentValue > $val(tapSize)-1){
	    currentValue -= $val(tapSize);
	  }
	  
	  accum = 0.0;
	  nminusk = currentValue;
	  src[nminusk] = $ref(signalIn);
	  for (k=0; k < currentValue + 1; k++){
	    accum += fir[k] * src[nminusk];
	    nminusk--;
	  }

	  nminusk = $val(tapSize)-1;
	  for (k = currentValue+1; k < $val(tapSize); k++){
	    accum += fir[k] * src[nminusk];
	    nminusk--;
	  }

	  $ref(signalOut) = accum;
	  currentValue++;
	}	
	go {
	  addCode(bodyDecl);
	  addCode(body);
	}
}
