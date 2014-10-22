/* Monotonic function that constructs a composite type from a list of ports.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

package ptolemy.actor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.AssociativeType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

/** A function that, given a list of ports, returns a <code>StructuredType</code>
 *  of which the fields names and field types correspond with the given ports.
 *  The arguments to this function (the array returned by getVariables())
 *  represent the types of the ports, in the same order as the ports
 *  are defined. The concrete subclass of <code>StructuredType</code> that
 *  is to be instantiated by this function depends on the type parameter
 *  passed to the constructor.
 *
 * @author Edward A. Lee, Marten Lohstroh
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (marten)
 * @Pt.AcceptedRating Red
 */
public class ConstructAssociativeType extends MonotonicFunction {

    /** Construct a new monotonic function.
     *  @param ports A list of ports used to construct the AssociativeType
     *  @param type A specific subclass to instantiate
     */
    public ConstructAssociativeType(Collection<TypedIOPort> ports,
            Class<? extends AssociativeType> type) {

        _ports = new LinkedList<TypedIOPort>();

        for (TypedIOPort port : ports) {
            // only consider ports that are connected
            if (port.isOutput() && port.numberOfSinks() > 0) {
                _ports.add(port);
            } else if (port.isInput() && port.numberOfSources() > 0) {
                _ports.add(port);
            }
        }

        _type = type;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public inner methods            ////

    /** Return a RecordType with field names equal to the given port names,
     *  and field types equal to the current inferred or declared type of the
     *  corresponding ports.
     *  @return A RecordType.
     *  @exception IllegalActionException If thrown while getting the
     *  value of the typeTerm of the port or while instantiating a type.
     *
     */
    @Override
    public Object getValue() throws IllegalActionException {
        Type[] types = new Type[_ports.size()];
        String[] labels = new String[_ports.size()];

        int i = 0;
        for (TypedIOPort port : _ports) {
            InequalityTerm portTypeTerm = port.getTypeTerm();
            if (port.getDisplayName() == null
                    || port.getDisplayName().equals("")) {
                labels[i] = port.getName();
            } else {
                labels[i] = port.getDisplayName();
            }

            if (portTypeTerm.isSettable()) {
                types[i] = (Type) portTypeTerm.getValue();
            } else {
                types[i] = port.getType();
            }
            i++;
        }

        // construct a new AssociativeType (RecordType, UnionType, ..)
        Object[] arglist = { labels, types };
        try {
            return _type.getConstructor(String[].class, Type[].class)
                    .newInstance(arglist);
        } catch (Exception e) {
            throw new IllegalActionException(e.getCause().getMessage());
        }
    }

    /** Return the type variables for this function, which are
     *  the type variables for all the ports that do not have declared types.
     *  @return An array of InequalityTerm.
     */
    @Override
    public InequalityTerm[] getVariables() {
        ArrayList<InequalityTerm> portTypeTermList = new ArrayList<InequalityTerm>();
        for (TypedIOPort port : _ports) {
            InequalityTerm portTypeTerm = port.getTypeTerm();
            if (portTypeTerm.isSettable()) {
                portTypeTermList.add(portTypeTerm);
            }
        }
        return portTypeTermList.toArray(new InequalityTerm[portTypeTermList
                .size()]);
    }

    /** The list of ports used to construct the AssociativeType. */
    private List<TypedIOPort> _ports;

    /** The specific subclass to instantiate. */
    private Class<? extends AssociativeType> _type;
}
