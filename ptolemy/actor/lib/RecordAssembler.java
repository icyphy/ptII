/* An actor that assembles multiple inputs to a RecordToken.

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

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// RecordAssembler

/**
 On each firing, read one token from each input port and assemble them
 into a RecordToken. The labels for the RecordToken are the names of the
 input ports.  To use this class, instantiate it, and then add input ports
 (instances of TypedIOPort).  This actor is polymorphic. The type constraint
 is that the type of each record field is no less than the type of the
 corresponding input port.

 @author Yuhong Xiong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (yuhong)
 @Pt.AcceptedRating Yellow (cxh)
 @see RecordDisassembler
 */
public class RecordAssembler extends TypedAtomicActor {
    /** Construct a RecordAssembler with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public RecordAssembler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"0\" y=\"0\" width=\"6\" "
                + "height=\"40\" style=\"fill:red\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port. Its type is constrained to be a RecordType. */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from each input port, assemble them into a RecordToken,
     *  and send the RecordToken to the output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        Object[] portArray = inputPortList().toArray();
        int size = portArray.length;

        // construct the RecordToken and to output
        String[] labels = new String[size];
        Token[] values = new Token[size];

        for (int i = 0; i < size; i++) {
            IOPort port = (IOPort) portArray[i];
            labels[i] = port.getName();
            values[i] = port.get(0);
        }

        RecordToken result = new RecordToken(labels, values);

        output.send(0, result);
    }

    /** Return true if all input ports have tokens, false if some input
     *  ports do not have a token.
     *  @return True if all input ports have tokens.
     *  @exception IllegalActionException If the hasToken() call to the
     *   input port throws it.
     *  @see ptolemy.actor.IOPort#hasToken(int)
     */
    public boolean prefire() throws IllegalActionException {
        Iterator ports = inputPortList().iterator();

        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();

            if (!port.hasToken(0)) {
                return false;
            }
        }

        return true;
    }

    /** Return the type constraints of this actor. The type constraint is
     *  that the type of the fields of the output RecordToken is no less
     *  than the type of the corresponding input ports.
     *  @return a list of Inequality.
     */
    public List typeConstraintList() {
        Object[] portArray = inputPortList().toArray();
        int size = portArray.length;
        String[] labels = new String[size];
        Type[] types = new Type[size];

        // form the declared type for the output port
        for (int i = 0; i < size; i++) {
            labels[i] = ((Port) portArray[i]).getName();
            types[i] = BaseType.UNKNOWN;
        }

        RecordType declaredType = new RecordType(labels, types);

        output.setTypeEquals(declaredType);

        // set the constraints between record fields and input ports
        List constraints = new LinkedList();

        // since the output port has a clone of the above RecordType, need to
        // get the type from the output port.
        RecordType outputType = (RecordType) output.getType();

        Iterator inputPorts = inputPortList().iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
            String label = inputPort.getName();
            Inequality inequality = new Inequality(inputPort.getTypeTerm(),
                    outputType.getTypeTerm(label));
            constraints.add(inequality);
        }

        return constraints;
    }
}
