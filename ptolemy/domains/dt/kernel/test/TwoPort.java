/* Two Port Test Actor for dt
@Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Red
@AcceptedRating Red
*/

package ptolemy.domains.dt.kernel.test;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


public class TwoPort extends TypedAtomicActor {

    public TwoPort(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {

        super(container, name);

        input1 = new TypedIOPort(this,"input1");
        input1.setInput(true);
        input1.setTypeEquals(BaseType.DOUBLE);


        input2 = new TypedIOPort(this,"input2");
        input2.setInput(true);
        input2.setTypeEquals(BaseType.DOUBLE);

        output1 = new TypedIOPort(this,"output1");
        output1.setOutput(true);
        output1.setTypeEquals(BaseType.DOUBLE);

        output2 = new TypedIOPort(this,"output2");
        output2.setOutput(true);
        output2.setTypeEquals(BaseType.DOUBLE);

        inrate1 = new Parameter(this, "inrate1", new IntToken(1));
        _inrate1 = 1;

        inrate2= new Parameter(this, "inrate2", new IntToken(1));
        _inrate2 = 1;

        outrate1 = new Parameter(this, "outrate1", new IntToken(1));
        _outrate1 = 1;

        outrate2 = new Parameter(this, "outrate2", new IntToken(1));
        _outrate2 = 1;

        input1_tokenConsumptionRate = 
            new Parameter(input1, "tokenConsumptionRate");
        input1_tokenConsumptionRate.setExpression("inrate1");
        
        input2_tokenConsumptionRate = 
            new Parameter(input2, "tokenConsumptionRate");
        input2_tokenConsumptionRate.setExpression("inrate2");

        output1_tokenProductionRate = 
            new Parameter(output1, "tokenProductionRate");
        output1_tokenProductionRate.setExpression("outrate1");

        output2_tokenProductionRate = 
            new Parameter(output2, "tokenProductionRate");
        output2_tokenProductionRate.setExpression("outrate2");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. This has type IntToken. */
    public TypedIOPort input1;
    public TypedIOPort input2;

    /** The output port. This has type BooleanToken. */
    public TypedIOPort output1;
    public TypedIOPort output2;

    public Parameter inrate1;
    public Parameter inrate2;
    public Parameter outrate1;
    public Parameter outrate2;

    public Parameter input1_tokenConsumptionRate;
    public Parameter input2_tokenConsumptionRate;
    public Parameter output1_tokenProductionRate;
    public Parameter output2_tokenProductionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Consume a single IntToken on the input. Produce 32 consecutive
     *  BooleanTokens on the output port which is the bitwise
     *  representation of the input IntToken.
     *  The most significant bit is the first boolean
     *  token send out. The least significant bit is the last
     *  boolean token send out.
     *
     *  @exception IllegalActionException If there is no director.
     */

    public final void fire() throws IllegalActionException  {
        int i;
        
        _inrate1 = ((IntToken)inrate1.getToken()).intValue();
        _inrate2 = ((IntToken)inrate2.getToken()).intValue();
        _outrate1 = ((IntToken)outrate1.getToken()).intValue();
        _outrate2 = ((IntToken)outrate2.getToken()).intValue();


        if (input1.getWidth() >= 1) {
            for (i = 0; i < _inrate1; i++) {
                input1.get(0);
            }
        }

        if (input2.getWidth() >= 1) {
            for (i = 0; i < _inrate2; i++) {
                input2.get(0);
            }
        }

        for (i = 0; i < _outrate1; i++) {
            output1.send(0, new DoubleToken(i));
        }
        for (i = 0; i < _outrate2; i++) {
            output2.send(0, new DoubleToken(i));
        }
    }

    private int _inrate1;
    private int _inrate2;
    private int _outrate1;
    private int _outrate2;
}
