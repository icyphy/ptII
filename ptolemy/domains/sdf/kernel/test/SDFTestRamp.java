/*
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
@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/
package ptolemy.domains.sdf.kernel.test;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;


/**
 * Create an increasing sequence of integer tokens,
 * starting with value zero, and incrementing by one.
 * This actor is aware of the rate that is set on its port and
 * will create the proper number of tokens with every firing.
 *
 * @version $Id$
 * @author Steve Neuendorffer
 */
public class SDFTestRamp extends SDFAtomicActor {
    public SDFTestRamp(TypedCompositeActor container, String name)
            throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        try{
            output = (SDFIOPort) newPort("output");
            output.setOutput(true);
            output.setTokenProductionRate(1);
            output.setTypeEquals(BaseType.INT);
        }
        catch (IllegalActionException e1) {
            System.out.println("SDFTestRamp: constructor error");
        }
        _value = 0;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public SDFIOPort output;

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            SDFTestRamp newobj = (SDFTestRamp)(super.clone(ws));
            newobj.output = (SDFIOPort)newobj.getPort("output");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /**
     * Produce several integer tokens with values with incremental values.
     * The number of tokens produced during each firing is determined by
     * the rates on the ports, and the sequence of values continues across
     * firings.
     * @exception IllegalActionException If a contained method throws it.
     */
    public void fire() throws IllegalActionException {
        int i;

        int tokens = output.getTokenProductionRate();
        for(i = 0; i < tokens; i++) {
            Token message = new IntToken(_value);
            _value = _value + 1;
            output.send(0, message);
        }
    }

    /**
     * Initialize the sequence so the first token created has value zero.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _value = 0;
    }

    private int _value;
}
