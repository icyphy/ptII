/* A commutator that processes a single token per iteration.

Copyright (c) 1997-2004 The Regents of the University of California.
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

import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sr.lib.SingleTokenCommutator;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// DDFSingleTokenCommutator
/**
   The DDFSingleTokenCommutator has a multiport input port and an output
   port.  The types of the ports are undeclared and will be resolved by
   the type resolution mechanism, with the constraint that the output
   type must be greater than or equal to the input type. On each call to
   the fire method, the actor reads one token from the current input,
   and writes one token to an output channel. In the following postfire
   method, it will update the ArrayToken with consumption rate for each 
   input channel indicating it will read token from the next channel in
   the next iteration. 

   @author Gang Zhou
*/
public class  DDFSingleTokenCommutator extends SingleTokenCommutator {

    /** Construct an actor in the specified container with the specified name.
     *  @param container The container.
     *  @param name This is the name of this distributor within the container.
     *  @exception NameDuplicationException If an actor
     *   with an identical name already exists in the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public DDFSingleTokenCommutator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        inputTokenConsumptionRate = 
        	    new Parameter(input, "tokenConsumptionRate");
        inputTokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        inputTokenConsumptionRate.setTypeEquals(new ArrayType(BaseType.INT));
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                          parameters                       ////
    
    /** This parameter provides token consumption rate for each input
     *  channel.
     */
    public Parameter inputTokenConsumptionRate;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Begin execution by setting rate parameter indicating it will 
     *  read the zeroth input channel. 
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        Token[] rates = new IntToken[input.getWidth()];
        rates[0] = new IntToken(1);
        for (int i=1; i < input.getWidth(); i++) {
            rates[i] = new IntToken(0);
        }
        inputTokenConsumptionRate.setToken(new ArrayToken(rates));
    }

    /** Update rate parameter indicating the next input channel.
     *  @return True if execution can continue into the next iteration. 
     *  @exception IllegalActionException If any called method throws 
     *   IllegalActionException. 
     */
    public boolean postfire() throws IllegalActionException {
        
        // Call postfire first so that current input position is updated.
        boolean postfireReturn = super.postfire();
        
        Token[] rates = new IntToken[input.getWidth()];
        int currentInputPosition = _getCurrentInputPosition();
        rates[currentInputPosition] = new IntToken(1);
        for (int i=0; i < input.getWidth(); i++) {
            if ( i != currentInputPosition)
                rates[i] = new IntToken(0);
        }
        inputTokenConsumptionRate.setToken(new ArrayToken(rates));
        
        return postfireReturn;
    }

}
