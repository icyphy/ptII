/* An actor that assembles multiple inputs to a RecordToken.

 Copyright (c) 1998-2010 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
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


    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        RecordAssembler newObject = (RecordAssembler) super.clone(workspace);

        // When the user calls typeConstraints(), the _typeConstraints object
        // will be updated.
        newObject._typeConstraints = new HashSet<Inequality>();
        newObject._typeConstraintsVersion = -1;
        return newObject;
    }

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

    /** Override the base class to compute the type constraints.
     *  It is an optimization to force this to happen before scheduling
     *  because this has the side effect of incrementing the workspace
     *  version number, which will force recalculation of the schedule
     *  on the next run.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        typeConstraints();
    }

    /** Return the type constraints of this actor. The type constraint is
     *  that the type of the fields of the output RecordToken is no less
     *  than the type of the corresponding input ports.
     *  @return a list of Inequality.
     */
    public Set<Inequality> typeConstraints() {
        if (workspace().getVersion() == _typeConstraintsVersion) {
            return _typeConstraints;
        }
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
        _typeConstraints = new HashSet<Inequality>();

        // since the output port has a clone of the above RecordType, need to
        // get the type from the output port.
        RecordType outputType = (RecordType) output.getType();

        Iterator inputPorts = inputPortList().iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
            String label = inputPort.getName();
            Inequality inequality = new Inequality(inputPort.getTypeTerm(),
                    outputType.getTypeTerm(label));
            _typeConstraints.add(inequality);
        }
        _typeConstraintsVersion = workspace().getVersion();
        return _typeConstraints;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Cached list of type constraints. */
    private Set<Inequality> _typeConstraints;

    /** Version number when the cache was last updated. */
    private long _typeConstraintsVersion = -1;
}
