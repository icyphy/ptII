/* Source of PseudoNoise Code.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.comm;

import ptolemy.actor.lib.Source;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// PNcode.java
/**FIXME: documents 

@author Edward A. Lee and Rachel Zhou
@version $Id$
*/

public class PNcode extends Source {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PNcode(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        polynomial = new Parameter(this, "polynomial");
        polynomial.setTypeEquals(BaseType.INT);
        polynomial.setExpression("07");
        
        initial = new Parameter(this, "initial");
        initial.setTypeEquals(BaseType.INT);
        initial.setExpression("1");
        // Declare output data type.
        output.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    public Parameter polynomial;
    public Parameter initial;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** FIXME
     *  @exception IllegalActionException
     *  
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
          if (attribute == initial) {
          int seed = ((IntToken)initial.getToken()).intValue();
          if (seed<=0 ) {
                throw new IllegalActionException(this,
                "shift register's value must be positive.");
            }
            } else if (attribute == polynomial) {
            int mask = ((IntToken)polynomial.getToken()).intValue();
            if (mask <= 0) {
                throw new IllegalActionException(this,
                "Polynomial is required to be strictly positive.");
            }
            if ((mask & 1)==0) {
                throw new IllegalActionException(this,
                "The low-order bit of the the polynomial is not set.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read at most one input token from each channel of the trigger
     *  input and discard it.  If the trigger input is not connected,
     *  then this method does nothing.  Derived classes should be
     *  sure to call super.fire(), or to consume the trigger input
     *  tokens themselves, so that they aren't left unconsumed.
     *  @exception IllegalActionException
     */
    public void fire() throws IllegalActionException {
        //super.fire();
        _latestShiftReg=_shiftReg;
        int mask = ((IntToken)polynomial.getToken()).intValue();
        //int seed = ((IntToken)initial.getToken()).intValue(); 
        int reg = _latestShiftReg<<1;
        int masked = mask & reg;
        int parity=0;
        while (masked >0){
            parity=parity ^ (masked & 1);
            masked = masked >>1;
            }
        _latestShiftReg=reg | parity;
        int out=_shiftReg & 1;     
        if (out==1){
            output.broadcast(_tokenOne);
        }else {
            output.broadcast(_tokenZero);
        }
    }

    /** FIXME
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _latestShiftReg=_shiftReg = ((IntToken)initial.getToken()).intValue();
    }
    
    public boolean postfire() throws IllegalActionException {
        _shiftReg=_latestShiftReg;
        return super.postfire();
    }

    //////////////////////////////////////////////////////////////
    ////                     private variables               /////
    
    private int _shiftReg;
    private int _latestShiftReg;
    
    private static IntToken _tokenOne = new IntToken(1);
    private static IntToken _tokenZero = new IntToken(0);
    
  
}
