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
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


public class TwoPort extends TypedAtomicActor {

    public TwoPort(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {

        super(container, name);

        input1 = new SDFIOPort(this,"input1");
        input1.setInput(true);
        input1.setTokenConsumptionRate(1);
        input1.setTypeEquals(BaseType.DOUBLE);

        input2 = new SDFIOPort(this,"input2");
        input2.setInput(true);
        input2.setTokenConsumptionRate(1);
        input2.setTypeEquals(BaseType.DOUBLE);

        output1 = new SDFIOPort(this,"output1");
        output1.setOutput(true);
        output1.setTokenProductionRate(1);
        output1.setTypeEquals(BaseType.DOUBLE);

        output2 = new SDFIOPort(this,"output2");
        output2.setOutput(true);
        output2.setTokenProductionRate(1);
        output2.setTypeEquals(BaseType.DOUBLE);

        inrate1= new Parameter(this, "inrate1", new IntToken(1));
        _inrate1 = 1;

        inrate2= new Parameter(this, "inrate2", new IntToken(1));
        _inrate2 = 1;

        outrate1 = new Parameter(this, "outrate1", new IntToken(1));
        _outrate1 = 1;

        outrate2 = new Parameter(this, "outrate2", new IntToken(1));
        _outrate2 = 1;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. This has type IntToken. */
    public SDFIOPort input1;
    public SDFIOPort input2;

    /** The output port. This has type BooleanToken. */
    public SDFIOPort output1;
    public SDFIOPort output2;

    public Parameter inrate1;
    public Parameter inrate2;
    public Parameter outrate1;
    public Parameter outrate2;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        Director dir = getDirector();

        if (dir != null) {
            if (attribute == inrate1) {
                _inrate1 = ((IntToken) inrate1.getToken()).intValue();
                input1.setTokenConsumptionRate(_inrate1);
                dir.invalidateSchedule();
            } else if (attribute == inrate2) {
                _inrate2 = ((IntToken) inrate2.getToken()).intValue();
                input2.setTokenConsumptionRate(_inrate2);
                dir.invalidateSchedule();
            } else if (attribute == outrate1) {
                _outrate1 = ((IntToken) outrate1.getToken()).intValue();
                output1.setTokenProductionRate(_outrate1);
                dir.invalidateSchedule();
            } else if (attribute == outrate2) {
                _outrate2 = ((IntToken) outrate2.getToken()).intValue();
                output2.setTokenProductionRate(_outrate2);
                dir.invalidateSchedule();
            }
        }
    }



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
        int integer, remainder;
        DoubleToken token;

        if (input1.getWidth() >= 1) {
            for (i=0; i < _inrate1;i++) {
                token = (DoubleToken) (input1.get(0));
            }
        }

        if (input2.getWidth() >= 1) {
            for (i=0; i < _inrate2;i++) {
                token = (DoubleToken) (input2.get(0));
            }
        }


        for (i=0; i < _outrate1;i++) {
            output1.send(0, new DoubleToken(i));
        }
        for (i=0; i < _outrate2;i++) {
            output2.send(0, new DoubleToken(i));
        }
    }

    private int _inrate1;
    private int _inrate2;
    private int _outrate1;
    private int _outrate2;
}
