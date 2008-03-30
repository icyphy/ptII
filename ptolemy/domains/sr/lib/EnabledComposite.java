/* A composite actor whose clock ticks only when enabled.

 Copyright (c) 2004-2006 The Regents of the University of California.
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

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.FunctionDependency;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sr.kernel.SRDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
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

        // NOTE: this might be non-strict because it may
        // contain actors that are non-strict.

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
    ////                     ports and parameters                  ////

    /** The control port for enabling the composite.
     */
    public TypedIOPort enable;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the <i>enable</i> input is known and true, then invoke the
     *  fire() method of the superclass.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void fire() throws IllegalActionException {
        if (_enabled) {
            super.fire();
        }
    }

    /** Return a representation of the function dependencies that output
     *  ports have on input ports.
     *  @return A representation of the function dependencies of the
     *   ports of this actor.
     *  @see ptolemy.actor.util.FunctionDependency
     */
    public FunctionDependency getFunctionDependency() {
        if (_functionDependency == null) {
            try {
                _functionDependency = new FunctionDependencyOfEnabledCompositeActor(
                        this);
            } catch (NameDuplicationException e) {
                // This should not happen.
                throw new InternalErrorException("Failed to construct a "
                        + "function dependency object for " + getFullName());
            } catch (IllegalActionException e) {
                // This should not happen.
                throw new InternalErrorException("Failed to construct a "
                        + "function dependency object for " + getFullName());
            }
        }

        return _functionDependency;
    }

    /** If the <i>enable</i> input is known and true, then invoke the
     *  postfire() method of the superclass and return its value. Otherwise,
     *  return true.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        if (_enabled) {
            return super.postfire();
        } else {
            return true;
        }
    }

    /** If the <i>enable</i> input is not known, then return true,
     *  and set a flag that prevents fire() and postfire() from
     *  doing anything;  if the <i>enable</i> input is known
     *  and either absent or false, then return false;
     *  if it is known and true, then invoke the prefire() method of the
     *  superclass and return what it returns.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("EnabledComposite: Calling prefire()");
        }
        // By default prefire() returns true indicating it is ready to be
        // prefired and fired again.  Note that it must return true
        // if the inputs are not known since otherwise, if it returns
        // false, the FixedPointDirector will set all its outputs
        // to absent.
        boolean prefireReturnValue = true;
        // By default (in most cases), this actor is disabled.
        _enabled = false;
        if (enable.isKnown(0)) {
            if (enable.hasToken(0)) {
                _enabled = ((BooleanToken) enable.get(0)).booleanValue();
            }
            if (!_enabled) {
                // Not enabled. Return false and the outputs will be
                // cleared by the director.
                if (_debugging) {
                    _debug("Not enabled: prefire() returns false.");
                }
                prefireReturnValue = false;
            } else {
                // This will call prefire() on the contained director.
                prefireReturnValue = super.prefire();
            }
        }
        return prefireReturnValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Local variable indicating whether this actor can be fired.
     *  The value of this variable is set in the prefire method.
     */
    private boolean _enabled = false;
}
