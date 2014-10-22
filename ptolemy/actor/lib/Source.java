/* Base class for simple source actors.

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

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.TypeConstant;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Source

/**
 Base for simple data sources.  This provides an output
 port and a trigger input port, both exposed as public variables.
 The trigger port is a multiport with undeclared type, meaning that
 you can supply it with any data type.  The trigger port can also be
 left unconnected.  The purpose of the trigger input is to
 (optionally) supply events that cause the actor to fire.  If the
 port is connected to something, then this actor will check it
 for a token and return false from prefire() if there is no token.
 each channel of the trigger input, if any, and then discards the
 token.
 <p>
 Some derived classes may attach additional significance to an input
 on the trigger port. For example, they might fix the type and attach
 some significance to the value.  Note that it is not recommend to
 use getWidth() on the port to determine whether the port is connected,
 since the width may be greater than zero even if there
 is no actual source of data.  This can occur, for example, if a trigger port
 is connected to the inside of a port of an opaque composite actor, and
 there is nothing connected to the outside of that port. It is not
 recommended to make the behavior of an actor dependent on a global
 property such as whether there is ultimately a source of data.</p>
 <p>
 Any type of data on is accepted on the trigger port, therefore no
 type is declared. Instead, the type resolution algorithm finds
 the least fixed point. If backward type inference is enabled, and
 no type has been declared for the trigger, it is constrained to be
 equal to <code>BaseType.GENERAL</code>. This will result in upstream
 ports resolving to the most general type rather than the most specific.
 </p>


 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (bilung)
 */
public abstract class Source extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Source(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output = new TypedIOPort(this, "output", false, true);
        trigger = new TypedIOPort(this, "trigger", true, false);

        // NOTE: It used to be that trigger was set to GENERAL, but this
        // isn't really what we want.  What we want is an undeclared type
        // that can resolve to anything.  EAL 12/31/02
        // trigger.setTypeEquals(BaseType.GENERAL);
        trigger.setMultiport(true);

        // Parameter to get Vergil to label the trigger port.
        new SingletonParameter(trigger, "_showName")
                .setToken(BooleanToken.TRUE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port.  The type of this port is unspecified.
     *  Derived classes may set it.
     */
    public TypedIOPort output = null;

    /** The trigger port.  The type of this port is undeclared, meaning
     *  that it will resolve to any data type.
     */
    public TypedIOPort trigger = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read at most one input token from each channel of the trigger
     *  input and discard it.  If the trigger input is not connected
     *  or has no actual sources (it might be connected to other
     *  inputs, for example, or to an unconnected input port at
     *  a higher level in the hierarchy) then this method does
     *  nothing.  Derived classes should be
     *  sure to call super.fire(), or to consume the trigger input
     *  tokens themselves, so that they aren't left unconsumed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        // Note that the following derived classes do not call
        // super.fire(): WallClockTime.
        // if significant changes are made to this method, please review
        // the above classes.

        // NOTE: It might seem that using trigger.numberOfSources() is
        // correct here, but it is not. It is possible for channels
        // to be connected, for example, to other output ports or
        // even back to this same trigger port, in which case higher
        // numbered channels will not have their inputs read.
        for (int i = 0; i < trigger.getWidth(); i++) {
            // FIXME: Should this be:
            // if (trigger.isKnown(i) && trigger.hasToken(i)) {
            // DiscreteClock.fire() was checking if the trigger was known
            // before DiscreteClock.fire() was calling super.fire()
            if (trigger.hasToken(i)) {
                trigger.get(i);
                _triggered = true;
            }
        }
    }

    /** If the trigger input is connected and it has no input or an unknown
     *  state, then return false. Otherwise, return true.
     *  @return True, unless the trigger input is connected
     *   and has no input.
     *  @exception IllegalActionException If checking the trigger for
     *   a token throws it or if the super class throws it.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (trigger.numberOfSources() > 0) {
            // Have to consume all trigger inputs.
            boolean returnValue = false;
            for (int i = 0; i < trigger.getWidth(); i++) {
                if (trigger.isKnown(i) && trigger.hasToken(i)) {
                    returnValue = true;
                }
            }
            if (_debugging) {
                _debug("Called prefire(), which returns " + returnValue);
            }
            return returnValue;
        }

        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the input port greater than or equal to
     *  <code>BaseType.GENERAL</code> in case backward type inference is
     *  enabled and the input port has no type declared.
     *
     *  @return A set of inequalities.
     */
    @Override
    protected Set<Inequality> _customTypeConstraints() {
        HashSet<Inequality> result = new HashSet<Inequality>();
        if (isBackwardTypeInferenceEnabled()
                && trigger.getTypeTerm().isSettable()) {
            result.add(new Inequality(new TypeConstant(BaseType.GENERAL),
                    trigger.getTypeTerm()));
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Indicator of whether trigger inputs have arrived
     *  since the last output.
     */
    protected transient boolean _triggered;

}
