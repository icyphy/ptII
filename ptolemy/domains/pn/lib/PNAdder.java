/* A multiple input double precision adder.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

package ptolemy.domains.pn.lib;
import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;
import java.util.Enumeration;
import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// MatrixAdder
/**
A multiple input double precision adder.
Input and output are DoubleMatrixTokens.

@author Mudit Goel
@version $Id$
*/
public class PNAdder extends AtomicActor {

    /** Constructor. Creates ports
     * @exception NameDuplicationException is thrown if more than one port
     *  with the same name is added to the star or if another star with an
     *  an identical name already exists.
     */
    public PNAdder(CompositeActor container, String name)
	    throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
        _input.setMultiport(true);
        _output = new IOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add all the inputs and send the result to output port.
     */
    public void fire() throws IllegalActionException {
        double[][] sum = ((DoubleMatrixToken)_input.get(0)).doubleMatrix();
	for (int i = 1; i < _input.getWidth(); i++) {
	    //FIXME: This is a hack. Generalize it.
	    double[][] data =
                ((DoubleMatrixToken)_input.get(i)).doubleMatrix();
	    //FIXME: Take care. Perhaps throw an exception.
	    if (sum.length != data.length && sum[0].length != data[0].length) {
		System.out.println("Error: Lengths in adder not equal: " +
                        "length 0 = " + data[0].length + "length 1 = " +
                        sum[0].length);
	    }
	    for (int j = 0; j < sum.length; j++) {
		for (int k = 0; k<sum[j].length; k++) {
		    sum[j][k] += data[j][k];
		}
	    }
	}
	DoubleMatrixToken result = new DoubleMatrixToken(sum);
	_output.broadcast(result);
	//debug
	//double[][] debug = result.doubleMatrix();
	//for (int j = 0; j < debug[0].length; j++) {
	//System.out.println(getName()+": debug[0]["+j+"] = "+debug[0][j]);
	//writeTo(_output, result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The input port
    private IOPort _input;
    // The output port
    private IOPort _output;
}
