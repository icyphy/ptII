/* A DoubleToken quantizer.
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2014-2015 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

PT_COPYRIGHT_VERSION_2
COPYRIGHTENDKEY

 */
package org.ptolemy.qss;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DoubleTokenQuantizer

/**
A DoubleToken quantizer.
This quantizer is designed to take an input signal and send it to its output port
if the signal has changed by more than a quantum relative to the last signal seen 
at the input port. 

@author Thierry S. Nouidui
@version $Id: DoubleTokenQuantizer.java 71415 2015-01-19 01:14:58Z eal $
@since Ptolemy II 11.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (cxh)
 */
public class DoubleTokenQuantizer extends TypedAtomicActor {

	/**
    /** Construct a new instance of the quantizer.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If setting up ports and parameters fails.
     *  @exception NameDuplicationException If the container already contains an object with this name.
	 */
	public DoubleTokenQuantizer(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		input = new TypedIOPort(this, "input", true, false);
		input.setTypeEquals(BaseType.DOUBLE);
		output = new TypedIOPort(this, "output", false, true);
		output.setTypeEquals(BaseType.DOUBLE);

		quantum = new Parameter(this, "quantum");
		quantum.setTypeEquals(BaseType.DOUBLE);
		quantum.setExpression("1.0");
	}

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

	/** Input. */
	public TypedIOPort input;

	/** Output. */
	public TypedIOPort output;

	/** Nominal value of the input. */
	public Parameter quantum;
	

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
	
	/**
	 * Produce an output equal to the input if the input has crossed the quantum.
	 * 
	 * @throws IllegalActionException If sending an output fails.
	 */
	public void fire() throws IllegalActionException {
		super.fire();
		if (input.hasToken(0)) {
			Token curInput = input.get(0);
			final double newInput = ((DoubleToken) curInput).doubleValue();
			if (Math.abs(newInput - _lastInput) > Math.abs((_quantum))) {
				_lastInput = newInput;
				output.send(0, new DoubleToken(newInput));
			}
		}
	}
	
	/**
	 * Initialize this actor.
	 */
	public void initialize() throws IllegalActionException {
		super.initialize();
		// Initialize the lastInput with 0.0.
		_lastInput = 0.0;
		// Get the quantum.
		_quantum = ((DoubleToken) quantum.getToken()).doubleValue();
	}

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

	/** The last recent input. */
	private double _lastInput;

	/** Quantum. */
	private double _quantum;
}
