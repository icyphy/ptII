/* One line description of file.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@ProposedRating Red (acataldo@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.apps.softwalls;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ThreeDFunction
/**
This creates a function of three variables, defined over some subset
of R^3.  The function is read from a file which stores the value of the
function along a lattice of gridpoints.  The begining of the file also
specifes the index value at each gridpoint, which in turn specifies the
subset of R^3 for which the dataset is defined.

@author Adam Cataldo
@version $Id$
@since Ptolemy II 2.0.1
*/
public class ThreeDFunction {
    /** Constructs the 3D dataset.
     *  @exception IllegalActionException if any exception is 
     *     is generated during file i/o.
     */
    public ThreeDFunction(String fileName) throws IllegalActionException {
	String line;
	StringTokenizer tokenizer;
	try {
	    BufferedReader in = 
		new BufferedReader(new FileReader(fileName));
	    
	    /*Read the dimension of the state space anc ignore, since
	      we know it's 3*/
	    line = _readLine(in);

	    //Read x grid information.
	    line = _readLine(in);
	    tokenizer = new StringTokenizer(line);
	    _xLowerBound = 
		(new Double(tokenizer.nextToken())).doubleValue();
	    _xStepSize =
		(new Double(tokenizer.nextToken())).doubleValue();
	    _xUpperBound =
		(new Double(tokenizer.nextToken())).doubleValue();

	    //Read y grid information.
	    line = _readLine(in);
	    tokenizer = new StringTokenizer(line);
	    _yLowerBound = 
		(new Double(tokenizer.nextToken())).doubleValue();
	    _yStepSize =
		(new Double(tokenizer.nextToken())).doubleValue();
	    _yUpperBound =
		(new Double(tokenizer.nextToken())).doubleValue();

	    //Read theta grid information.
	    line = _readLine(in);
	    tokenizer = new StringTokenizer(line);
	    _thetaLowerBound = 
		(new Double(tokenizer.nextToken())).doubleValue();
	    _thetaStepSize =
		(new Double(tokenizer.nextToken())).doubleValue();
	    _thetaUpperBound =
		(new Double(tokenizer.nextToken())).doubleValue();

	    in.close();
	}
	catch (FileNotFoundException f) {
	    throw new IllegalActionException(f.getMessage());
	}
	catch (IOException i) {
	    throw new IllegalActionException(i.getMessage());
	}
	catch (NumberFormatException n) {
	    throw new IllegalActionException(n.getMessage());
	}
	catch (NoSuchElementException e) {
	    throw new IllegalActionException(e.getMessage());
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////
    
    /* Lower bound for each dimension */
    double _xLowerBound;
    double _yLowerBound;    
    double _thetaLowerBound;	

    /* Step size for each dimension */
    double _xStepSize;
    double _yStepSize;
    double _thetaStepSize;
	
    /* Upper bound for each dimension */
    double _xUpperBound;
    double _yUpperBound;
    double _thetaUpperBound;


    ///////////////////////////////////////////////////////////////////
    ////                       private methods                     ////
    
    /*  If a line has no data, it tries to return the next line.
     *  If no next line exists, it returns null, 
     **/
    private String _readLine(BufferedReader reader) {
   	try {
	    String line = reader.readLine();
	    StringTokenizer tokenizer = new StringTokenizer(line);
	    if (tokenizer.hasMoreTokens()) {
	    	return line;
	    }
	    else {
	    	return reader.readLine();
	    }
	}
	catch (IOException i) {
	    return null;
	}
    }
}
