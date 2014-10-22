/* An actor that disassembles a RecordToken to multiple outputs.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.ConstructAssociativeType;
import ptolemy.actor.util.ExtractFieldType;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeConstant;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// RecordDisassembler

/**
 On each firing, read one RecordToken from the input port and send out
 the fields of the RecordToken to multiple output ports.
 The labels for the RecordToken must match the names of the output ports.
 This is achieved using three type constraints:
 <ul>
 <li><tt>input >= {x = typeOf(outputPortX), y = typeOf(outputPortY), ..}
 </tt>, which requires the types of the fields in the input record to be
 compatible with the types of the corresponding output ports.
 </li>
 <li><tt>input <= {x = GENERAL, y = GENERAL, ..}</tt>, which requires the
 input record to contain a corresponding field for each output port.
 </li>
 <li><tt>each output >= the type of the corresponding field inside the input
 record</tt>, which is similar to the usual default constraints, however
 this constraint establishes a dependency between fields inside the input
 record and the outputs of this actor, instead of just between its inputs
 and outputs.
 </li>
 </ul>

 <p>If the received Token contains more fields than the output ports, the extra
 fields are ignored.</p>

 <p>To use this class, instantiate it, and then add output ports (instances
 of TypedIOPort).  This actor is polymorphic. The type constraint is that
 the type of each output port is no less than the type of the corresponding
 record field.</p>

 <p>Note that if the display name of a port is set, display name is used in
 the type constraints instead of name. This is useful in case fields to
 extract from the record contain a period, because periods are not allowed
 in port names.</p>

 @author Yuhong Xiong, Steve Neuendorffer, Edward A. Lee, Marten Lohstroh
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (yuhong)
 @Pt.AcceptedRating Yellow (cxh)
 @see RecordAssembler
 */
public class RecordDisassembler extends TypedAtomicActor {
    /** Construct a RecordDisassembler with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public RecordDisassembler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"0\" y=\"0\" width=\"6\" "
                + "height=\"40\" style=\"fill:red\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port. */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        RecordDisassembler newObject = (RecordDisassembler) super
                .clone(workspace);
        newObject._portMap = new HashMap<String, TypedIOPort>();
        return newObject;
    }

    /** Read one RecordToken from the input port and send its fields
     *  to the output ports.
     *  If the input does not have a token, suspend firing and return.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }

        if (input.hasToken(0)) {
            RecordToken record = (RecordToken) input.get(0);
            Iterator<?> labels = record.labelSet().iterator();

            while (labels.hasNext()) {
                String label = (String) labels.next();
                Token value = record.get(label);
                TypedIOPort port = _portMap.get(label);

                // since the record received may contain more fields than the
                // output ports, some fields may not have a corresponding
                // output port.
                if (port != null) {
                    port.send(0, value);
                }
            }
        }
    }

    /** React to a name change of contained ports. Update the internal
     *  mapping from names and aliases to port objects, and invalidate
     *  the resolved types.
     *  @param object The object that changed.
     */
    @Override
    public void notifyOfNameChange(NamedObj object) {
        if (object instanceof TypedIOPort) {
            _mapPorts();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set up and return three type constraints.
     *  <ul>
     *  <li><tt>input >= {x = typeOf(outputPortX), y = typeOf(outputPortY), ..}
     *  </tt>, which requires the types of the fields in the input record to be
     *  compatible with the types of the corresponding output ports.
     *  </li>
     *  <li><tt>input <= {x = GENERAL, y = GENERAL, ..}</tt>, which requires
     *  the input record to contain a corresponding field for each output port.
     *  </li>
     *  <li><tt>each output >= the type of the corresponding field inside the
     *  input record</tt>, which is similar to the usual default constraints,
     *  however this constraint establishes a dependency between fields inside
     *  the input record and the outputs of this actor, instead of just between
     *  its inputs and outputs.
     *  </li>
     *  </ul>
     *  @return A set of Inequality instances
     *  @see ConstructAssociativeType
     *  @see ExtractFieldType
     */
    @Override
    protected Set<Inequality> _customTypeConstraints() {
        Set<Inequality> result = new HashSet<Inequality>();
        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<Type> types = new ArrayList<Type>();

        // make sure the ports are mapped
        _mapPorts();

        // constrain the fields in the input record to be greater than or
        // equal to the declared or resolved types of the output ports:
        // input >= {x = typeOf(outputPortX), y = typeOf(outputPortY), ..}
        result.add(new Inequality(new ConstructAssociativeType(_portMap
                .values(), RecordType.class), input.getTypeTerm()));

        for (Entry<String, TypedIOPort> entry : _portMap.entrySet()) {
            String outputName = entry.getKey();
            TypedIOPort output = entry.getValue();

            labels.add(outputName);
            types.add(BaseType.GENERAL);

            // constrain each output to be >= the type of the corresponding
            // field inside the input record
            result.add(new Inequality(new ExtractFieldType(input, outputName),
                    output.getTypeTerm()));
        }
        // constrain the input record to have the required fields:
        // input <= {x = GENERAL, y = GENERAL}
        result.add(new Inequality(input.getTypeTerm(), new TypeConstant(
                new RecordType(labels.toArray(new String[labels.size()]), types
                        .toArray(new Type[types.size()])))));

        // NOTE: refrain from using port.setTypeAtMost() or
        // port.setTypeAtLeast(), because after removing an output port, the
        // constraint referring to this removed port will remain to exist in
        // the input port, which will result in type errors.
        return result;
    }

    /** Do not establish the usual default type constraints.
     *  @return null
     */
    @Override
    protected Set<Inequality> _defaultTypeConstraints() {
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Map port names or aliases to port objects. If the mapping
     *  has changed, then invalidate the resolved types, which
     *  forces new type constraints with appropriate field names
     *  to be generated.
     */
    private void _mapPorts() {
        // Retrieve the manager.
        Manager manager = this.getManager();

        // Generate a new mapping from names/aliases to ports.
        Map<String, TypedIOPort> oldMap = _portMap;
        _portMap = new HashMap<String, TypedIOPort>();
        for (TypedIOPort p : this.outputPortList()) {
            String name = p.getName();
            String alias = p.getDisplayName();
            // ignore unconnected ports
            if (p.numberOfSinks() < 1) {
                continue;
            }
            if (alias == null || alias.equals("")) {
                _portMap.put(name, p);
            } else {
                _portMap.put(alias, p);
            }
        }

        // Only invalidate resolved types if there actually was a name change.
        // As a result, new type constraints will be generated.
        if (manager != null && (oldMap == null || !_portMap.equals(oldMap))) {
            manager.invalidateResolvedTypes();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Keeps track of which name or alias is associated with which port. */
    private Map<String, TypedIOPort> _portMap;

}
