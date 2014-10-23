/* A composite actor whose clock ticks only when enabled.

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
package ptolemy.domains.sr.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.Dependency;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sr.kernel.SRDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// EnabledComposite

/**
 * A composite actor whose clock ticks only when enabled.

 * <p>This actor will only have clock ticks when provided with a
 * true-valued token on the <i>enabled</i> input port. Thus, it can be
 * used to create subclocks of the master clock.
 *
 * <p> Note that this mechanism is a hierarchical version of what is
 * provided by the Lustre and SIGNAL clock calculi, but it
 * circumvents the undecidability of clock signals by requiring
 * the model designer to be explicit about subclocks.
 *
 * <p>P. Caspi, D. Pilaud, N. Halbwachs, and J. A. Plaice, "LUSTRE: A
 * Declarative Language for Programming Synchronous Systems,"
 * Conference Record of the 14th Annual ACM Symp. on Principles of
 * Programming Languages, Munich, Germany, January, 1987.
 *
 * <p>A. Benveniste and P. Le Guernic, "Hybrid Dynamical Systems Theory
 * and the SIGNAL Language," IEEE Tr. on Automatic Control, Vol. 35,
 * No. 5, pp. 525-546, May 1990.
 *
 * @author Edward A. Lee
 * @version $Id$
 @since Ptolemy II 4.1
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Red (cxh)
 */
public class EnabledComposite extends TypedCompositeActor {

    // NOTE: this might be non-strict because it may
    // contain actors that are non-strict.

    /** Construct an actor in the specified workspace.
     *  This constructor is provided so that this can be a top-level.
     *  Making it a top level only makes sense if it is a class definition,
     *  however.
     *  @param workspace The workspace.
     *  @exception IllegalActionException If constructing the ports and
     *   inside director throws it.
     *  @exception NameDuplicationException If constructing the ports and
     *   inside director throws it.
     */
    public EnabledComposite(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);

        _init();
    }

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public EnabledComposite(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The control port for enabling the composite.
     */
    public TypedIOPort enable;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a causality interface for this actor, which overrides the
     *  default behavior of composite actors to ensure that all outputs
     *  depend on the <i>enable</i> input port.
     *  @return A representation of the dependencies between input ports
     *   and output ports.
     */
    @Override
    public CausalityInterface getCausalityInterface() {
        if (_causalityInterface != null) {
            return _causalityInterface;
        }
        _causalityInterface = new CausalityInterfaceForEnabledComposite(this);
        return _causalityInterface;
    }

    /** If the <i>enable</i> input is not known, then return false;
     *  if the <i>enable</i> input is known
     *  and either absent or false, then also return false;
     *  if it is known and true, then invoke the prefire() method of the
     *  superclass and return what it returns.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        boolean prefireReturnValue = false;
        if (enable.isKnown(0)) {
            if (enable.hasToken(0)) {
                prefireReturnValue = ((BooleanToken) enable.get(0))
                        .booleanValue();
                if (prefireReturnValue) {
                    // This will call prefire() on the contained director.
                    prefireReturnValue = super.prefire();
                }
            }
        }
        if (_debugging) {
            _debug("EnabledComposite: prefire() returns " + prefireReturnValue);
        }
        return prefireReturnValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the enable port and inside director.
     *  @exception IllegalActionException If creating these things throws it.
     *  @exception NameDuplicationException If creating these things throws it.
     */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        enable = new TypedIOPort(this, "enable", true, false);
        enable.setTypeEquals(BaseType.BOOLEAN);

        StringAttribute controlCardinal = new StringAttribute(enable,
                "_cardinal");
        controlCardinal.setExpression("SOUTH");

        Location location = new Location(enable, "_location");
        location.setExpression("[140.0, 35.0]");

        // Create an inside director.
        SRDirector director = new SRDirector(this, "SRDirector");
        location = new Location(director, "_location");
        location.setExpression("[65.0, 35.0]");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Causality interface that overrides the behavior of the base
     *  class to ensure that every output depends on the <i>enable</i>
     *  input port.
     */
    private class CausalityInterfaceForEnabledComposite extends
    CausalityInterfaceForComposites {
        public CausalityInterfaceForEnabledComposite(Actor actor)
                throws IllegalArgumentException {
            super(actor, BooleanDependency.OTIMES_IDENTITY);
        }

        @Override
        protected void _computeActorDepth() throws IllegalActionException {
            if (_actorDepthVersion == ((NamedObj) _actor).workspace()
                    .getVersion()) {
                return;
            }
            super._computeActorDepth();
            // Now fix the internal data structures.
            // First, the equivalence classes. All inputs are equivalent
            // because of the common dependency on the enable port.
            Set<IOPort> allInputs = new HashSet<IOPort>(_actor.inputPortList());
            // Make sure the dependencies are up to date.
            // This can be an issue if the model has changed.
            getDependency(enable, null);
            for (IOPort input : _equivalenceClasses.keySet()) {
                _equivalenceClasses.put(input, allInputs);
            }
            // Next, fix the forward and backward dependencies.
            Map<IOPort, Dependency> enableDependents = new HashMap<IOPort, Dependency>();
            _forwardDependencies.put(enable, enableDependents);
            List<IOPort> outputs = _actor.outputPortList();
            for (IOPort output : outputs) {
                enableDependents.put(output,
                        _defaultDependency.oTimesIdentity());
                Map<IOPort, Dependency> backward = _reverseDependencies
                        .get(output);
                if (backward == null) {
                    backward = new HashMap<IOPort, Dependency>();
                    _reverseDependencies.put(output, backward);
                }
                backward.put(enable, _defaultDependency.oTimesIdentity());
            }
        }
    }
}
