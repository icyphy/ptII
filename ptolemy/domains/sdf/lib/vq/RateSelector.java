/* Partition an image into smaller subimages.
@Copyright (c) 1998-1999 The Regents of the University of California.
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
        data.setInput(true);
        data.setMultiport(true);
      
        //        rate = (SDFIOPort) newPort("rate");
        //rate.setInput(true);
        //rate.setMultiport(true);

        output = (SDFIOPort) newPort("output");
        output.setOutput(true);

        maxDistortion = 
            new Parameter(this, "maxDistortion", new DoubleToken("50.0"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The data port. */
    public SDFIOPort data;

     /** The rate port. */
    public SDFIOPort rate;

    /** The output port. */
    public SDFIOPort output;

    public Parameter maxDistortion;

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
        IntMatrixToken _data, _rate;
        IntMatrixToken optimalData = null;
        double bestDistortion;
        IntMatrixToken bestData;
            
        
        int width = data.getWidth();
        //        if(data.getWidth() != rate.getWidth()) {
        //    throw new IllegalActionException("Widths of input ports must " +
        //            "be the same.");
        // }
        optimalData = (IntMatrixToken)data.get(0);
        bestDistortion = -1;
        bestData = null;
        for(i = 1; i < width; i++) {
            _data = (IntMatrixToken)data.get(i);
            //    rateToken = rate.get(i);
            IntMatrixToken difference = 
                (IntMatrixToken) optimalData.subtract(_data);
            double distortion = 0;
            int diff[] = difference.intArray();
            for(i = 0; i < diff.length; i++) {
                distortion = diff[i] * diff[i];
            }
            if((bestDistortion == -1) || 
                    (distortion < ((ScalarToken)maxDistortion.getToken()).doubleValue() &&
                            distortion > bestDistortion)) {
                bestDistortion = distortion;
                bestData = _data;
            }
        }
        if(bestData == null) {
            throw new IllegalActionException("no data found to send!");
        }

        output.send(0, bestData);
    }
}
