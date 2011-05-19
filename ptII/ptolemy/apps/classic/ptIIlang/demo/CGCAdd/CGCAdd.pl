defstar {
	name {Add}
	domain {CGC}
	desc { Output the sum of the inputs, as a floating value.  }
	version { @(#)CGCAdd.pl	1.8	7/11/96 }
	author { S. Ha }
	copyright {
Copyright (c) 1990-2005 The Regents of the University of California.
All rights reserved.
See the file $PTOLEMY/copyright for copyright notice,
limitation of liability, and disclaimer of warranty provisions.
	}
	location { CGC main library }
	inmulti {
		name {input}
		type {float}
	}
	output {
		name {output}
		type {float}
	}
	constructor {
		noInternalState();
	}
	go {
		StringList out = "\t$ref(output) = ";
		for (int i = 1; i <= input.numberPorts(); i++) {
			out << "$ref(input#" << i << ")";
			if (i < input.numberPorts())
			  out << " + ";
			else
			  out << ";\n";
		}
		addCode(out);
	}
	exectime {
		return input.numberPorts();
	}
}
