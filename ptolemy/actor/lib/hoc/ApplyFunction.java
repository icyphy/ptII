/* An actor that dynamically applies functions to its input.

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
import ptolemy.data.BooleanToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.Token;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ApplyFunction

/**
 This actor applies a function to its inputs and outputs the
 results. But rather than has the function specified statically,
 this actor allows dynamic change to the function, which means the
 computation of this actor can be changed during executing. Its
 second input accept a function token for the new function's
 definition. The function token can be given by actors in the local
 model or remote actors.

 @author Steve Neuendorffer, Yang Zhao
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class ApplyFunction extends TypedAtomicActor {
    /** Construct a ApplyFunction in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ApplyFunction(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        output = new TypedIOPort(this, "output", false, true);
        function = new PortParameter(this, "function");
        new SingletonParameter(function, "_showName")
                .setToken(BooleanToken.TRUE);
    }

    /** Construct a ApplyFunction with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ApplyFunction(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new TypedIOPort(this, "output", false, true);
        function = new PortParameter(this, "function");

        output.setTypeAtLeast(new ReturnTypeFunction());
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

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type of the output port of the
     *  new object.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ApplyFunction newObject = (ApplyFunction) super.clone(workspace);
        newObject.output.setTypeAtLeast(new ReturnTypeFunction());
        return newObject;
    }

    /** If the function is not specified, then perform identity function;
     *  otherwise, apply the specified function to its input and output
     *  the result.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's fire() method throws it, or if the actor is not
     *   opaque.
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
            arguments[i++] = port.get(0);
        }

        Token t = functionValue.apply(arguments);
        output.broadcast(t);
    }

    /** Return true if the actor either of its input port has token.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        super.prefire();

        Iterator ports = inputPortList().iterator();

        // Skip the function port.
        ports.next();

        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();

            if (!port.hasToken(0)) {
                return false;
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Montonic function of the function parameter that return
     *  unknown if the function is unknown, the return type of
     *  the function if the function is known and is a function,
     *  and throws an exception otherwise.
     */
    private class ReturnTypeFunction extends MonotonicFunction {

        @Override
        public Object getValue() throws IllegalActionException {
            Type functionType = function.getType();
            if (functionType.equals(BaseType.UNKNOWN)) {
                return BaseType.UNKNOWN;
            }
            if (functionType instanceof FunctionType) {
                return ((FunctionType) functionType).getReturnType();
            }
            throw new IllegalActionException(ApplyFunction.this,
                    "function is not a function. It is a " + functionType);
        }

        @Override
        public InequalityTerm[] getVariables() {
            InequalityTerm[] result = new InequalityTerm[1];
            result[0] = function.getTypeTerm();
            return result;
        }
    }
}
