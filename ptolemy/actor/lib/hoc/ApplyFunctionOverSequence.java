/* An actor that applies a function over each element of a sequence.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.hoc;

import java.util.Iterator;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.DFUtilities;
import ptolemy.data.ArrayToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ApplyFunctionOverSequence

/**
 Apply a function over one or more input sequences.
 This actor will collect tokens from each input port into arrays
 and, when enough input tokens have arrived, pass those arrays
 to the function specified either at the <i>function</i> parameter
 or the port.
 <p>
 To use this actor, create any number of input ports, add
 a parameter named <i>tokenConsumptionRate</i> to each input
 port, and set the value of this parameter to the number
 of tokens that you would like to be collected into an
 array for each function application. Also, create
 and set a parameter named <i>tokenProductionRate</i>
 in the output port.  (If <i>tokenConsumptionRate</i>
 or <i>tokenProductionRate</i> are not defined, then they
 will be assumed to have value one).
 Then define a function that takes as many array-valued
 arguments as there are input ports and returns an
 array-valued result. For example, the following function
 will compute the FFT of the input array using the FFT()
 function in the expression language:
 <pre>
 function(x:{double}) abs(FFT(x, 8))
 </pre>
 Note that if the <i>tokenConsumptionRate</i> of a port is
 changed during the execution of the model, the change is
 ignored until the next execution of the model.

 @author Steve Neuendorffer (Contributor: Edward A. Lee)
 @deprecated Use SequenceToArray followed by ApplyFunction.
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 @see ptolemy.actor.lib.hoc.ApplyFunction
 */
@Deprecated
public class ApplyFunctionOverSequence extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ApplyFunctionOverSequence(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output = new TypedIOPort(this, "output", false, true);
        function = new PortParameter(this, "function");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port for function definition. The type of this port is
     *  undeclared, but to have this actor work, the designer has to provide
     *  a matched function token for it.
     *  Note: The reason that we don't declare the type for it is because
     *  currently there is not cast supported in the FunctionType class.
     *  we'll fix this later.
     */
    public PortParameter function;

    /** The output port.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume the inputs, apply the function, and produce the result.
     *  @exception IllegalActionException If a runtime type error occurs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        // Update the function parameterPort.
        function.update();

        FunctionToken functionValue = (FunctionToken) function.getToken();
        Token[] arguments = new Token[inputPortList().size() - 1];
        int i = 0;
        Iterator ports = inputPortList().iterator();

        // Skip the function port.
        ports.next();

        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();

            if (_rate[i] == -1) {
                arguments[i] = port.get(0);
            } else {
                Token[] tokens = port.get(0, _rate[i]);
                arguments[i] = new ArrayToken(port.getType(), tokens);
            }

            i++;
        }

        Token result = functionValue.apply(arguments);

        if (_outputRate == -1) {
            output.broadcast(result);
        } else {
            // FIXME: Check size.
            ArrayToken resultArray = (ArrayToken) result;
            output.broadcast(resultArray.arrayValue(), resultArray.length());
        }
    }

    /** Return true if the input ports have enough tokens.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        super.prefire();

        Iterator ports = inputPortList().iterator();
        int i = 0;

        // Skip the function port.
        ports.next();

        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();

            if (_rate[i] == -1) {
                if (!port.hasToken(0)) {
                    return false;
                }
            } else {
                if (!port.hasToken(0, _rate[i])) {
                    return false;
                }
            }
        }

        return true;
    }

    /** Preinitialize the actor.  Set the type of the ports based on
     *  the type of the function parameter.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        FunctionType type = (FunctionType) function.getType();

        if (type.getReturnType() instanceof ArrayType) {
            output.setTypeEquals(((ArrayType) type.getReturnType())
                    .getElementType());
            _outputRate = DFUtilities.getTokenProductionRate(output);
        } else {
            output.setTypeEquals(type.getReturnType());
            _outputRate = -1;
        }

        int i = 0;
        _rate = new int[inputPortList().size() - 1];

        Iterator ports = inputPortList().iterator();

        // Skip the function port.
        ports.next();

        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();
            Type portType = type.getArgType(i);

            if (portType instanceof ArrayType) {
                port.setTypeEquals(((ArrayType) portType).getElementType());
                _rate[i] = DFUtilities.getTokenConsumptionRate(port);
            } else {
                port.setTypeEquals(portType);
                _rate[i] = -1;
            }

            i++;
        }
    }

    private int _outputRate;

    private int[] _rate;
}
