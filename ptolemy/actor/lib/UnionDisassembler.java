/* An actor that disassembles a UnionToken to the corresponding output.

 Copyright (c) 2006-2012 The Regents of the University of California.
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
import java.util.Set;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.ConstructAssociativeType;
import ptolemy.actor.util.ExtractFieldType;
import ptolemy.data.Token;
import ptolemy.data.UnionToken;
import ptolemy.data.type.UnionType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// UnionDisassembler

/**
 On each firing, read one UnionToken from the input port and send out
 the value to the output port that matches the label name of the
 input token. This actor is polymorphic. The labels for the UnionToken must
 match the names of the output ports.

 This is achieved using three type constraints:
 <ul>
 <li><tt>input &gt;= {|x = typeOf(outputPortX), y = typeOf(outputPortY), ..|}</tt>,
 which requires the types of the fields in the input union to be compatible
 with the corresponding output ports. This constraint is set in the
 constructor of this class.
 </li>
 <li><tt>each output &gt;= the type of the corresponding field inside the input
 union</tt>, which is similar to the usual default constraints, however this
 constraint establishes a dependency between fields inside the input union
 and the outputs, instead of just between inputs and outputs.
 </li>
 </ul>
 Note that the constraint <tt>input &gt;= {|x = GENERAL, y = GENERAL, ..|}
 </tt>, which is used in <code>RecordDisassembler</code> to force the input
 to  contain a corresponding field for each output port, is useless for
 <code>UnionDisassembler</code>. This is due to the inverse width subtyping
 of <code>UnionToken</code>.

 @author Yang Zhao, Marten Lohstroh
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (marten)
 @Pt.AcceptedRating Red (cxh)
 @see RecordDisassembler
 @see UnionType
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

    /** The input port. */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one UnionToken from the input port and send its fields
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set up and returns two type constraints.
     *  <ul>
     *  <li><tt>input >= {|x = typeOf(outputPortX), y = typeOf(outputPortY)
     *  , ..|}</tt>, which requires the types of the fields in the input union
     *  to be compatible with the types of the corresponding output ports.</li>
     *
     *  <li><tt>each output >= the type of the corresponding field inside the
     *  input union</tt>, which is similar to the usual default constraints,
     *  however this constraint establishes a dependency between fields inside
     *  the input union and the outputs of this actor, instead of just between
     *  its inputs and outputs.</li>
     *  </ul>
     *
     *  <p>Note that the constraint <tt>input &lt;= {|x = GENERAL, y = GENERAL, ..|}
     *  </tt>, which is used in RecordDisassembler to force the input to
     *  contain a corresponding field for each output port, is useless for
     *  UnionDisassembler. This is due to the inverse width subtyping of
     *  <code>UnionToken</code>.</p>
     *
     *  @return A set of Inequality instances
     *  @see ConstructAssociativeType
     *  @see ExtractFieldType
     */
    @Override
    protected Set<Inequality> _customTypeConstraints() {
        Set<Inequality> result = new HashSet<Inequality>();

        // constrain the fields in the input union to be greater than or
        // equal to the declared or resolved types of the output ports:
        // input >= {| x = typeOf(outputPortX), y = typeOf(outputPortY), ..|}
        result.add(new Inequality(new ConstructAssociativeType(
                outputPortList(), UnionType.class), input.getTypeTerm()));

        for (TypedIOPort output : outputPortList()) {
            // constrain each output to be >= the type of the corresponding
            // field inside the input union
            result.add(new Inequality(new ExtractFieldType(input, output
                    .getName()), output.getTypeTerm()));
        }

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
}
