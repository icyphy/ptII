/* An actor that assembles multiple inputs to a RecordToken.

 Copyright (c) 1998-2012 The Regents of the University of California.
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
import ptolemy.actor.util.ConstructAssociativeType;
import ptolemy.actor.util.ExtractFieldType;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.RecordType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// RecordAssembler

/**
 On each firing, read one token from each input port and assemble them
 into a RecordToken. The labels for the RecordToken much match the names
 of the input ports. This is achieved using two type constraints:

 <ul>
 <li><tt>output >= {x = typeOf(inputPortX), y = typeOf(inputPortY), ..}
 </tt>, which requires the types of the input ports to be compatible
 with the corresponding types in the output record.
 </li>
 <li><tt>each input >= the type of the corresponding field inside the
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
 <p>Note that while port names may have spaces in them, record labels
 may not because record labels that have spaces in them are not
 parseable by the expression language.  Spaces in record labels
 are converted to underscores in the RecordToken constructors an
 toString() method.  If this actor has ports that have a space
 or other character that is substituted, then at runtime the port
 may resolve to an unknown type with a message like:</p>
 <pre>
Caused by: ptolemy.actor.TypeConflictException: Types resolved
  to unacceptable types in .Router due to the following objects:
  (port .Router.Record Disassembler.sequence number: unknown)
 </pre>

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
            labels[i] = StringUtilities.sanitizeName(port.getName());
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
        Iterator<?> ports = inputPortList().iterator();

        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();

            if (!port.hasToken(0)) {
                return false;
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set up and return two type constraints.
     *  <ul>
     *  <li><tt>output >= {x = typeOf(inputPortX), y = typeOf(inputPortY), ..}
     *  </tt>, which requires the types of the input ports to be compatible
     *  with the corresponding types in the output record.
     *  </li>
     *  <li><tt>each input >= the type of the corresponding field inside the
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

        // constrain the type of every input to be greater than or equal to 
        // the resolved type of the corresponding field in the output record
        for (TypedIOPort input : inputPortList()) {
            // only include ports that have no type declared
            if (input.getTypeTerm().isSettable()) {
                result.add(new Inequality(new ExtractFieldType(output, input
                        .getName()), input.getTypeTerm()));
            }
        }

        // constrain the fields in the output record to be greater than or
        // equal to the declared or resolved types of the input ports:
        // output >= {x = typeOf(outputPortX), y = typeOf(outputPortY), ..}
        result.add(new Inequality(new ConstructAssociativeType(inputPortList(),
                RecordType.class), output.getTypeTerm()));

        return result;
    }

    /** Do not establish the usual default type constraints.
     *  @return null
     */
    @Override
    protected Set<Inequality> _defaultTypeConstraints() {
        return null;
    }

}
