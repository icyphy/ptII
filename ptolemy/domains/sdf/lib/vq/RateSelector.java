/* Partition an image into smaller subimages.
@Copyright (c) 1998-2000 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

                                                PT_COPYRIGHT_VERSION 2
                                                COPYRIGHTENDKEY
@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/
package ptolemy.domains.sdf.lib.vq;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import java.io.*;
import ptolemy.actor.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// RateSelector
/**


@author Steve Neuendorffer
@version $Id$
*/

public class RateSelector extends SDFAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public RateSelector(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        data = (SDFIOPort) newPort("data");
	data.setTypeEquals(BaseType.INT_MATRIX);
        data.setInput(true);
        data.setMultiport(true);

	rate = (SDFIOPort) newPort("rate");
        rate.setTypeEquals(BaseType.INT);
        rate.setInput(true);
        rate.setMultiport(true);

        output = (SDFIOPort) newPort("output");
	output.setTypeEquals(BaseType.INT_MATRIX);
        output.setOutput(true);

        maxRate =
            new Parameter(this, "maxRate", new IntToken("8000"));
	blocks =
	    new Parameter(this, "blocks", new IntToken("1584"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The data port. */
    public SDFIOPort data;

    /** The rate port. */
    public SDFIOPort rate;

    /** The output port. */
    public SDFIOPort output;

    public Parameter maxRate;
    public Parameter blocks;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            RateSelector newobj = (RateSelector)(super.clone(ws));
            newobj.data = (SDFIOPort)newobj.getPort("data");
            newobj.rate = (SDFIOPort)newobj.getPort("rate");
            newobj.output = (SDFIOPort)newobj.getPort("output");
	    newobj.maxRate =
		(Parameter)newobj.getAttribute("maxRate");
	    newobj.blocks = (Parameter)newobj.getAttribute("blocks");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /**
     * Initialize this actor
     * @exception IllegalActionException If a parameter does not contain a
     * legal value, or partitionColumns does not equally divide imageColumns,
     * or partitionRows does not equally divide imageRows.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

    }

    /**
     * Fire this actor.
     *
     */
    public void fire() throws IllegalActionException {
        int i, j;
	int x, y;
        int partitionNumber;
	double _maxRate =
	    ((ScalarToken)maxRate.getToken()).doubleValue();
	int _blocks =
	    ((ScalarToken)blocks.getToken()).intValue();

	IntMatrixToken optimalData[] = new IntMatrixToken[_blocks];
        IntToken optimalRate[] = new IntToken[_blocks];
        double bestDistortion[] = new double[_blocks];
        IntMatrixToken bestData[] = new IntMatrixToken[_blocks];

        int width = data.getWidth();
	if(data.getWidth() != rate.getWidth()) {
            throw new IllegalActionException("Widths of input ports must " +
                    "be the same.");
        }
        IntMatrixToken _data[][] = new IntMatrixToken[width][_blocks];
	IntToken _rate[][] = new IntToken[width][_blocks];
	double lambda[][] = new double[width][_blocks];
	double distortion[][] = new double[width][_blocks];
	int channel[] = new int[_blocks];

	for(i = 0; i < width; i++) {
	    data.getArray(i, _data[i]);
	    rate.getArray(i, _rate[i]);
	}

	for(j = 0; j < _blocks; j++) {
	    for(i = 0; i < width; i++) {
		IntMatrixToken difference =
		    (IntMatrixToken) _data[0][j].subtract(_data[i][j]);
		double dist = 0;
		int diff[] = difference.intArray();
		for(int k = 0; k < diff.length; k++) {
		    dist = diff[k] * diff[k];
		}
		distortion[i][j] = dist;
	    }
	}

	int totalrate = 0;
	for(j = 0; j < _blocks; j++) {
	    for(i = 1; i < width; i++) {
		lambda[i][j] = (distortion[i][j] - distortion[i-1][j])/
		    (_rate[i][j].intValue() - _rate[i-1][j].intValue());
		if(lambda[i][j] < 0)
		    lambda[i][j] = -lambda[i][j];
	    }
	    channel[j] = width - 1;
	    totalrate += _rate[width - 1][j].intValue();
	}

	while(totalrate > ((ScalarToken)maxRate.getToken()).intValue()) {
	    double bestLambda = 0;
	    int bestBlock = -1;
	    for(j = 0; j < _blocks; j++) {
		double l = lambda[channel[j]][j];
		if(l > bestLambda) {
		    bestLambda = l;
		    bestBlock = j;
		}
	    }
	    channel[bestBlock]--;
	    totalrate += _rate[channel[bestBlock]][bestBlock].intValue() -
		_rate[channel[bestBlock]++][bestBlock].intValue();
	    System.out.println("totalrate = " + totalrate);
	}

	IntMatrixToken outputs[] = new IntMatrixToken[_blocks];
	for(j = 0; j < _blocks; j++) {
	    outputs[j] = _data[channel[j]][j];
	}

        output.sendArray(0, outputs);
    }
}
