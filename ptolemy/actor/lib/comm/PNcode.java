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
/**
This actor generates a pseudo random code using a feedback shift register.
The taps of the feedback shift register are given by the <i>polynomial</i>
parameter, which should be a positive integer. The low-order bit is called
the 0-th bit, and should always be set. The initial state of the shift
register is given by the parameter <i>initial</i>, which should be a positive
integer.
<p>
Note: To generate the max-length pseudo random code, the polynomial should
be a <i>primitive polynomial</i>. For convenience, we list here a set of
primitive polynomials (expressed in octal numbers.)
<pre>
order    polynomial
2        07
3        013
4        023
5        045
6        0103
7        0211
8        0435
9        01021
10       02011
11       04005
12       010123
13       020033
14       042103
15       0100003
16       0210013
17       0400011
18       01000201
19       02000047
20       04000011
21       010000005
22       020000003
23       040000041
24       0100000207
25       0200000011
26       0400000107
27       01000000047
28       02000000011
29       04000000005
30       010040000007
</pre>
<p>
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

     /**Polynomial, whose coefficents indicates the taps of the
      * feedback shift register. It should be a positive integer
      * and the lower-order bit should be 1.
     */
    public Parameter polynomial;
   
    /**Initial State of the shift register. This should be a 
     * positive interger. The state "zero" is a lock-up state.
    */
    public Parameter initial;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>initial</i>, then verify
        that is a positive interger; if it is <i>polynomial</i>, then
        verify that is a positive interger and the lower-order bit is 1.
     *  @exception IllegalActionException If <i>initial</i> is non-positive
     *  or polynomial is non-positive or the lower-order bit is not 1.
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
     */
    public void fire() throws IllegalActionException {
        _latestShiftReg=_shiftReg;
        int mask = ((IntToken)polynomial.getToken()).intValue(); 
        int reg = _latestShiftReg<<1;
        int masked = mask & reg;
        int parity=0;
        while (masked >0){
            parity=parity ^ (masked & 1);
            masked = masked >>1;
            }
        _latestShiftReg=reg | parity;
        //last bit of the shift register
        int out=_shiftReg & 1;     
        if (out==1){
            output.broadcast(_tokenOne);
        }else {
            output.broadcast(_tokenZero);
        }
    }

    /** Initialize the actor by resetting the shift register state
     *  equal to the value of <i>initial</i>
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _latestShiftReg=_shiftReg = ((IntToken)initial.getToken()).intValue();
    }
   
    /** Record the most recent shift register state as the new 
     * initial state for the next iteration. 
     * @exception IllegalActionException If the base class throws it 
    */ 
    public boolean postfire() throws IllegalActionException {
        _shiftReg=_latestShiftReg;
        return super.postfire();
    }

    //////////////////////////////////////////////////////////////
    ////                     private variables               /////
    
    //record the state of the shift register 
    private int _shiftReg;
    
    //the latest state of the shift register
    private int _latestShiftReg;

    //Since this actor always sends one of the two tokens, we statically 
    //create those tokens to avoid unnecessary object construction.
    private static IntToken _tokenOne = new IntToken(1);
    private static IntToken _tokenZero = new IntToken(0);
    
  
}
