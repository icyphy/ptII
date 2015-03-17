/* An actor that assembles multiple inputs to a RecordToken.

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

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.ConstructAssociativeType;
import ptolemy.actor.util.ExtractFieldType;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.RecordType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// RecordAssembler

/**
 On each firing, read one token from each connected input port and assemble
 them into a RecordToken. Disconnected input ports are ignored. The labels for
 the RecordToken much match the names of the input ports. This is achieved
 using two type constraints:

 <ul>
 <li><tt>output &ge; {x = typeOf(inputPortX), y = typeOf(inputPortY), ..}
 </tt>, which requires the types of the input ports to be compatible
 with the corresponding types in the output record.
 </li>
 <li><tt>each input &ge; the type of the corresponding field inside the
 output record</tt>, which together with the first constraint forces
 the input types to be exactly equal to the types of the corresponding
 fields in the output record. This constraint is intended to back-
 propagate type information upstream, not to assure type compatibility.
 Therefore, this constraint is only set up for input ports that do not
 already have a type declared.</li>
 </ul>
 Note that the output record is not required to contain a corresponding
 field for every input, as downstream actors might require fewer fields
 in the record they accept for input.
 <p>
 To use this class, instantiate it, and then add input ports
 (instances of TypedIOPort).  This actor is polymorphic. The type constraint
 is that the type of each record field is no less than the type of the
 corresponding input port.
 </p>
 <p>Note that if the display name of a port is set, display name is used in
 the type constraints instead of name. This is useful in case fields to
 add to the record contain a period, because periods are not allowed in
 port names.</p>


 @author Yuhong Xiong, Marten Lohstroh
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

    /** The output port. */
    public TypedIOPort output;

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
        RecordAssembler newObject = (RecordAssembler) super.clone(workspace);
        newObject._portMap = _newPortMap();
        return newObject;
    }

    /** Read one token from each input port, assemble them into a RecordToken,
     *  and send the RecordToken to the output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        int i = 0;
        Set<Entry<String, TypedIOPort>> entries = _portMap.entrySet();
        String[] labels = new String[entries.size()];
        Token[] values = new Token[entries.size()];

        for (Entry<String, TypedIOPort> entry : entries) {
            labels[i] = entry.getKey();
            values[i] = entry.getValue().get(0);
            i++;
        }

        RecordToken result = new RecordToken(labels, values);

        output.send(0, result);
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

    /** Return true if all connected input ports have tokens, false if some
     *  connected input ports do not have a token.
     *  @return True if all connected input ports have tokens and the
     *  parent method returns true.
     *  @exception IllegalActionException If the hasToken() call to the
     *   input port throws it.
     *  @see ptolemy.actor.IOPort#hasToken(int)
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        boolean superReturnValue = super.prefire();
        for (TypedIOPort port : _portMap.values()) {
            if (!port.hasToken(0)) {
                if (_debugging) {
                    _debug("Port " + port.getName()
                            + " does not have a token, prefire()"
                            + " will return false.");
                }
                return false;
            }
        }

        return true && superReturnValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set up and return two type constraints.
     *  <ul>
     *  <li><tt>output &ge; {x = typeOf(inputPortX), y = typeOf(inputPortY), ..}
     *  </tt>, which requires the types of the input ports to be compatible
     *  with the corresponding types in the output record.
     *  </li>
     *  <li><tt>each input &ge; the type of the corresponding field inside the
     *  output record</tt>, which together with the first constraint forces
     *  the input types to be exactly equal to the types of the corresponding
     *  fields in the output record. This constraint is intended to back-
     *  propagate type information upstream, not to assure type compatibility.
     *  Therefore, this constraint is only set up for input ports that do not
     *  already have a type declared.</li>
     *  </ul>
     *  Note that the output record is not required to contain a corresponding
     *  field for every input, as downstream actors might require fewer fields
     *  in the record they accept for input.
     *  @return A set of type constraints
     *  @see ConstructAssociativeType
     *  @see ExtractFieldType
     */
    @Override
    protected Set<Inequality> _customTypeConstraints() {
        Set<Inequality> result = new HashSet<Inequality>();

        // make sure the ports are mapped
        _mapPorts();

        // constrain the type of every input to be greater than or equal to
        // the resolved type of the corresponding field in the output record
        for (Entry<String, TypedIOPort> entry : _portMap.entrySet()) {
            String inputName = entry.getKey();
            TypedIOPort input = entry.getValue();
            // only include ports that have no type declared
            if (input.getTypeTerm().isSettable()) {
                result.add(new Inequality(new ExtractFieldType(output,
                        inputName), input.getTypeTerm()));
            }
        }

        // constrain the fields in the output record to be greater than or
        // equal to the declared or resolved types of the input ports:
        // output >= {x = typeOf(outputPortX), y = typeOf(outputPortY), ..}
        result.add(new Inequality(new ConstructAssociativeType(_portMap
                .values(), RecordType.class), output.getTypeTerm()));

        return result;
    }

    /** Do not establish the usual default type constraints.
     *  @return null
     */
    @Override
    protected Set<Inequality> _defaultTypeConstraints() {
        return null;
    }

    /** Return a new _portMap, which is a map between
     *  port names and strings.  Derived classes
     *  like OrderedRecordAssembler would return
     *  a map with a different ordering.
     *  @return a Map from port names to TypedIOPorts.
     */
    protected Map<String, TypedIOPort> _newPortMap() {
        // RecordToken._initializeStorage() should probably
        // use a similar Collection class.
        return new TreeMap<String, TypedIOPort>();
    }

    /** Map port names or aliases to port objects. If the mapping
     *  has changed, then invalidate the resolved types, which
     *  forces new type constraints with appropriate field names
     *  to be generated.
     */
    protected void _mapPorts() {
        // Retrieve the manager.
        Manager manager = this.getManager();

        // Generate a new mapping from names/aliases to ports.
        Map<String, TypedIOPort> oldMap = _portMap;
        _portMap = _newPortMap();
        for (TypedIOPort p : this.inputPortList()) {
            String name = p.getName();
            String alias = p.getDisplayName();
            // ignore unconnected ports
            if (p.numberOfSources() < 1) {
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
    ////                         protected variables               ////

    /** Keeps track of which name or alias is associated with which port. */
    protected Map<String, TypedIOPort> _portMap;

}
