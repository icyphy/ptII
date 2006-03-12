/* An actor that disassembles a UnionToken to the corresponding output.

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
package ptolemy.actor.lib;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.UnionToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.data.type.UnionType;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// UnionDisassembler

/**
 On each firing, read one UnionToken from the input port and send out
 the value to the output port that matches the label name of the 
 input token. This actor is polymorphic. The type 
 constraint is that the type of each output port is no less than the 
 type of the corresponding union field.

 @author Yang Zhao
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (ellen_zh)
 @Pt.AcceptedRating Yellow (cxh)
 @see RecordAssembler
 @see ArrayElement
 */
public class UnionDisassembler extends TypedAtomicActor {
    /** Construct a UnionDisassembler with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public UnionDisassembler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"0\" y=\"0\" width=\"6\" "
                + "height=\"40\" style=\"fill:red\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port. Its type is constrained to be a UnionType. */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one RecordToken from the input port and send its fields
     *  to the output ports.
     *  If the input does not have a token, suspend firing and return.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }

        if (input.hasToken(0)) {
            UnionToken union = (UnionToken) input.get(0);
            String label = union.label();
            Token value = union.value();
            
            IOPort port = (IOPort) getPort(label);
            if (port != null) {
                port.send(0, value);
            }
        }
    }

    /** Return the type constraints of this actor. The type constraint is
     *  that the type of the output ports is no less than the type of the
     *  corresponding field of the input union token.
     *  @return a list of Inequality.
     */
    public List typeConstraintList() {
        Object[] portArray = outputPortList().toArray();
        int size = portArray.length;
        String[] labels = new String[size];
        Type[] types = new Type[size];

        // form the declared type for the output port
        for (int i = 0; i < size; i++) {
            labels[i] = ((Port) portArray[i]).getName();
            types[i] = BaseType.GENERAL;
        }

        UnionType declaredType = new UnionType(labels, types);

        //FIXME: is this what we want?
        input.setTypeAtMost(declaredType);

        // set the constraints between union fields and output ports
        List constraints = new LinkedList();

        // since the input port has a clone of the above UnionType, need to
        // get the type from the input port.
        Iterator outputPorts = outputPortList().iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();
            String label = outputPort.getName();
            Inequality inequality = new Inequality(new PortFunction(label),
                    outputPort.getTypeTerm());
            constraints.add(inequality);
        }

        return constraints;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // This class implements a monotonic function of the type of a
    // port and a parameter.
    // The function value is determined by:
    // f(input.getType(), name) =
    //     UNKNOWN,                  if input.getType() = UNKNOWN
    //     input.getType()[name] if input.getType() instanceof RecordToken.
    //
    private class PortFunction extends MonotonicFunction {
        private PortFunction(String name) {
            _name = name;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Type.
         */
        public Object getValue() throws IllegalActionException {
            if (input.getType() == BaseType.UNKNOWN) {
                return BaseType.UNKNOWN;
            } else if (input.getType() instanceof UnionType) {
                UnionType type = (UnionType) input.getType();
                Type fieldType = type.get(_name);

                if (fieldType == null) {
                    return BaseType.UNKNOWN;
                } else {
                    return fieldType;
                }
            } else {
                throw new IllegalActionException(UnionDisassembler.this,
                        "Invalid type for input port");
            }
        }

        /** Return an additional string describing the current value
         *  of this function.
         */
        public String getVerboseString() {
            if (input.getType() instanceof UnionType) {
                UnionType type = (UnionType) input.getType();
                Type fieldType = type.get(_name);

                if (fieldType == null) {
                    return "Input Union doesn't have field named " + _name;
                }
            }

            return null;
        }

        /** Return the type variable in this inequality term. If the
         *  type of the input port is not declared, return an one
         *  element array containing the inequality term representing
         *  the type of the port; otherwise, return an empty array.
         *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            InequalityTerm portTerm = input.getTypeTerm();

            if (portTerm.isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = portTerm;
                return variable;
            }

            return (new InequalityTerm[0]);
        }

        ///////////////////////////////////////////////////////////////
        ////                       private inner variable          ////
        private String _name;
    }
}
