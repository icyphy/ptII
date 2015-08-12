/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
package ptolemy.actor.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// GLBFunction

/** This class implements a monotonic function that returns the greatest
 *  lower bound (GLB) of its arguments. These arguments are the port type
 *  variables of the destination ports of the TypedIOPort passed to the
 *  constructor. This function is used to define a type constraint asserting
 *  that the type of the output of this port is greater than or equal to the
 *  GLB of its destinations.
 *  <p>
 *  NOTE: It may seem counterintuitive that the constraint is "greater than
 *  or equal to" rather than "less than or equal to". But the latter
 *  constraint is already implied by the connections, since the output port
 *  type is required to be less than or equal to each destination port type.
 *  The combination of these constraints has the effect of setting the type
 *  of the output equal to the GLB of the types of the destination ports.
 *  This resolved type is, in fact, the most specific type that satisfies the
 *  constraints of all the downstream ports.
 * @author Edward A. Lee, Marten Lohstroh
 * @version $Id: GLBFunction.java$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (eal)
 * @Pt.AcceptedRating Red (cxh)
 */
public class GLBFunction extends MonotonicFunction {

    /** Construct a GLBFunction that finds the greatest lower bound of the
     *  type variable of the destination ports connected to the TypedIOPort
     *  that is given as an argument. If the boolean argument inside is true
     *  then the port is required to be an input port and the arguments for
     *  this <code>GLBFunction</code> will be the inside destination ports.
     *  Otherwise, the port is required to be an output port and the arguments
     *  for this <code>GLBFunction</code> will be the outside destination ports.
     *
     *  @param sourcePort The port connected to the ports of which their type
     *  variables are used to calculate the greatest lower bound.
     *
     */
    public GLBFunction(TypedIOPort sourcePort) {
        _sourcePort = sourcePort;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current value of this monotonic function.
     *  @return A Type.
     *  @exception IllegalActionException If thrown while getting the
     *  value of the cached terms.
     */
    @Override
    public Object getValue() throws IllegalActionException {
        _updateArguments();

        Set<Type> types = new HashSet<Type>();
        types.addAll(_cachedTypes);
        for (InequalityTerm _cachedTerm : _cachedTerms) {
            Type type = (Type) _cachedTerm.getValue();
            // if (type != BaseType.UNKNOWN)
            // enabling this will make the function non-monotonic which may
            // cause type resolution to diverge
            types.add(type);
        }
        // If there are no destination outputs at all, then set
        // the output type to unknown.
        if (types.size() == 0) {
            return BaseType.UNKNOWN;
        }
        // If there is only one destination, the GLB is equal to the
        // type of that port.
        if (types.size() == 1) {
            return types.toArray()[0];
        }

        return TypeLattice.lattice().greatestLowerBound(types);
    }

    /** Return the type variables for this function, which are
     *  the type variables for all the destination ports.
     *  @return An array of InequalityTerms.
     */
    @Override
    public InequalityTerm[] getVariables() {
        _updateArguments();
        return _cachedTerms;
    }

    /** Return the type constants for this function, which are
     *  the type constant types for all the destination ports.
     *  @return An array of Types.
     */
    public Type[] getConstants() {
        _updateArguments();
        return _cachedTypes.toArray(new Type[_cachedTypes.size()]);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Update the arguments used in <code>getValue()</code>, which are the
     *  InequalityTerms and Types of the destination ports. The arguments are
     *  only updated if the workspace version has changed.
     */
    protected void _updateArguments() {
        List<IOPort> destinations = null;
        if (_sourcePort.getContainer().workspace().getVersion() == _previousWorkspaceVersion) {
            return;
        }
        ArrayList<InequalityTerm> portTypeTermList = new ArrayList<InequalityTerm>();
        _cachedTypes = new HashSet<Type>();
        // Make sure to support ports that are both input and output.
        if (_sourcePort.isOutput()) {
            destinations = _sourcePort.sinkPortList();
        }
        if (_sourcePort.isInput()) {
            if (destinations == null) {
                destinations = _sourcePort.insideSinkPortList();
            } else {
                destinations = new LinkedList<IOPort>(destinations);
                destinations.addAll(_sourcePort.insideSinkPortList());
            }
        }

        for (IOPort destination : destinations) {
            InequalityTerm destinationTypeTerm = ((TypedIOPort) destination)
                    .getTypeTerm();
            if (destinationTypeTerm.isSettable()) {
                portTypeTermList.add(destinationTypeTerm);
            } else {
                _cachedTypes.add(((TypedIOPort) destination).getType());
            }
        }
        _cachedTerms = portTypeTermList
                .toArray(new InequalityTerm[portTypeTermList.size()]);
        _previousWorkspaceVersion = _sourcePort.getContainer().workspace()
                .getVersion();
    }

    /**
     * Provide a more descriptive string representation.
     * @return A description of this term.
     */
    @Override
    public String toString() {
        return "GreatestLowerBound(destinations)";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The constant types found in destination ports. */
    protected Set<Type> _cachedTypes;

    /** The types terms found in destination ports. */
    protected InequalityTerm[] _cachedTerms;

    /** The workspace version number at time of last update of arguments. */
    protected long _previousWorkspaceVersion = -1;

    /** The source port. */
    protected TypedIOPort _sourcePort;
}
