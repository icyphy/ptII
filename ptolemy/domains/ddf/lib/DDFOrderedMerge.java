/* An actor that merges two monotonically increasing streams into one.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.ddf.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.OrderedMerge;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;


//////////////////////////////////////////////////////////////////////////
//// DDFOrderedMerge

/**
   This actor merges two monotonically nondecreasing streams of tokens
   into one monotonically nondecreasing stream. On each firing, it reads
   data from one of the inputs. On the first firing, it simply records
   that token. On the second firing, it reads data from the other input
   and outputs the smaller of the recorded token and the one it just read.
   If they are equal, then it outputs the recorded token. It then records
   the larger token. On each subsequent firing, it reads a token from the
   input port that did not provide the recorded token, and produces at the
   output the smaller of the recorded token and the one just read.
   <p>
   If both input sequences are nondecreasing, then the output sequence
   will be nondecreasing.
   Note that if the inputs are not nondecreasing, then the output is
   rather complex. The key is that in each firing, it produces the smaller
   of the recorded token and the token it is currently reading.
   This derived class only updates rate parameters to indicate the next input
   port.

   @author Gang Zhou
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Yellow (zgang)
   @Pt.AcceptedRating Yellow (cxh)
*/
public class DDFOrderedMerge extends OrderedMerge {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DDFOrderedMerge(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        inputA_tokenConsumptionRate = new Parameter(inputA,
                "tokenConsumptionRate");
        inputA_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        inputA_tokenConsumptionRate.setTypeEquals(BaseType.INT);

        inputB_tokenConsumptionRate = new Parameter(inputB,
                "tokenConsumptionRate");
        inputB_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        inputB_tokenConsumptionRate.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       parameters                          ////

    /** The rate parameter for the input port A.The type is int.
     */
    public Parameter inputA_tokenConsumptionRate;

    /** The rate parameter for the input port B. The type is int.
     */
    public Parameter inputB_tokenConsumptionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize rate parameters to indicate the first token will be
     *  read from inputA.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        inputA_tokenConsumptionRate.setToken(_one);
        inputB_tokenConsumptionRate.setToken(_zero);
    }

    /** Update rate parameters indicating the next input port.
     *  @return True if execution can continue into the next iteration.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException.
     */
    public boolean postfire() throws IllegalActionException {
        // Call postfire first so that next input port is updated.
        boolean postfireReturn = super.postfire();
        TypedIOPort nextPort = _getNextPort();

        if (nextPort == inputA) {
            inputA_tokenConsumptionRate.setToken(_one);
            inputB_tokenConsumptionRate.setToken(_zero);
        } else {
            inputA_tokenConsumptionRate.setToken(_zero);
            inputB_tokenConsumptionRate.setToken(_one);
        }

        return postfireReturn;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A final static IntToken with value 0.
     */
    private final static IntToken _zero = new IntToken(0);

    /** A final static IntToken with value 1.
     */
    private final static IntToken _one = new IntToken(1);
}
