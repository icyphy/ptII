/* One Port Test Actor for DT
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
import ptolemy.data.IntMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


public class OnePort extends TypedAtomicActor {
    public OnePort(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setInput(true);
        input.setTypeEquals(BaseType.DOUBLE);
        inrate = new Parameter(this, "inrate", new IntToken(1));
        _inrate = 1;

        input_tokenConsumptionRate = 
            new Parameter(input, "tokenConsumptionRate");
        input_tokenConsumptionRate.setExpression("inrate");
        

        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeSameAs(input);
        outrate = new Parameter(this, "outrate", new IntToken(1));
        _outrate = 1;
        
        output_tokenProductionRate = 
            new Parameter(input, "tokenProductionRate");
        output_tokenProductionRate.setExpression("outrate");

        initialOutputs = new Parameter(this, "initialOutputs",
                new IntMatrixToken(defaultValues));
    }

    public TypedIOPort input;
    public Parameter input_tokenConsumptionRate;
    public TypedIOPort output;
    public Parameter output_tokenProductionRate;

    public Parameter inrate;
    public Parameter outrate;

    public Parameter initialOutputs;

    public Parameter value;
    public Parameter step;


    public final void fire() throws IllegalActionException  {
        int i;
        DoubleToken token = new DoubleToken(0.0);
        _inrate = ((IntToken)inrate.getToken()).intValue();
        _outrate = ((IntToken)outrate.getToken()).intValue();
        _buffer = new Token[_inrate];

        _buffer[0] = token;

        if (input.getWidth() >= 1) {
            for (i=0; i < _inrate; i++) {
                // FIXME: should consider port widths
                //if (input.hasToken(0)) {
                //token = (DoubleToken) (input.get(0));
                _buffer[i] = input.get(0);
                //} else {
                //    throw new IllegalActionException(
                //              "no Tokens available for OnePort during firing");
                //}
            }
        }

        for (i=0; i < _outrate; i++) {
            output.send(0, _buffer[i % _inrate]);
        }
    }

    private int _inrate;
    private int _outrate;
    private int defaultValues[][] = {{0,0}};
    private Token[] _buffer;
}
