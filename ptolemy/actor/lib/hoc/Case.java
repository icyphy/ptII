/* Actor representing one of several refinements.

 Copyright (c) 2002-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.hoc;

import java.util.Iterator;

import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Case

/**
 An actor that executes one of several refinements depending on the
 value provided by the <i>control</i> port-parameter. To use this,
 drag the Case icon onto the canvas, right click and select Open Actor.
 To add a refinement, go to the Case menu in the menu bar and select Add Case,
 then populate populate the refinement with computations.
 Each refinement is a composite that is required to have its own director.
 The name of the refinement is value that the control must have to
 execute this refinement.
 This actor always provides one case called "default". This is
 the refinement that is executed if no other refinement matches
 the control input.  All refinements have the same ports,
 and adding ports to any one refinement or to the case actor
 itself results in identical ports being added to all refinements.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class Case extends MultiCompositeActor {

    /** Construct
     * a modal model with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Case(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct a Case in the specified workspace with no container and
     *  an empty string as a name. You can then change the name with
     *  setName(). If the workspace argument is null, then use the default
     *  workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public Case(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port-parameter on which the control token is provided.
     *  This can have any type, and is initialized with a default value
     *  of true.
     */
    public PortParameter control;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to ensure that the _default member
     *  points to the default refinement.
     *  @param workspace The workspace for the new object.
     *  @return A new Case.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Case newObject = (Case) super.clone(workspace);
        newObject._default = (Refinement) newObject.getEntity("default");
        newObject._current = newObject._default;
        newObject._director = (CaseDirector) newObject
                .getAttribute("_director");
        return newObject;
    }

    /** Return the current refinement, or null if prefire() has not
     *  yet been invoked.
     *  @return The current refinement.
     */
    public Refinement getCurrentRefinement() {
        return _current;
    }

    /** Override the base class to not read inputs, since this has been
     *  done in prefire().  Fire the current refinement, and then
     *  send any output data created by calling the local director's
     *  transferOutputs method.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's fire() method throws it, or if the actor is not
     *   opaque.
     */
    @Override
    public void fire() throws IllegalActionException {
        // FIXME: Case.fire() does not invoke the piggyback methods as
        // is done in CompositeActor.fire().
        if (_debugging) {
            _debug("Calling fire()");
        }

        try {
            _workspace.getReadAccess();

            _director.fire();

            if (_stopRequested) {
                return;
            }

            // Use the local director to transfer outputs.
            Iterator outports = outputPortList().iterator();

            while (outports.hasNext() && !_stopRequested) {
                IOPort p = (IOPort) outports.next();
                _director.transferOutputs(p);
            }
        } finally {
            _workspace.doneReading();
        }

        if (_debugging) {
            _debug("Called fire()");
        }
    }

    /** Create a new refinement with the specified name.
     *  @param name The name of the refinement.
     *  @return The new refinement.
     *  @exception IllegalActionException If the refinement cannot be created.
     *  @exception NameDuplicationException If a refinement already
     *  exists with this name.
     */
    public Refinement newRefinement(String name) throws IllegalActionException,
    NameDuplicationException {
        return new Refinement(this, name);
    }

    /** Return the class name for refinements that this Case actor
     *  expects to contain.
     *  @return The string "ptolemy.actor.lib.hoc.Refinement".
     */
    public String refinementClassName() {
        return "ptolemy.actor.lib.hoc.Refinement";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to ensure that the default refinement remains
     *  last.
     *  @exception IllegalActionException If the entity has no name, or the
     *   action would result in a recursive containment structure.
     *  @exception NameDuplicationException If the name collides with a name
     *  already in the entity.
     */
    @Override
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        super._addEntity(entity);
        if (entity instanceof Refinement) {
            // Ensure that the default refinement remains the last one.
            // Note however that this is called on the default itself,
            // at which time the local member has not been set.
            if (_default != null) {
                _default.moveToLast();
            }
        }
    }

    /** Create a director. This base class creates an instance of CaseDirector.
     *  @return The created director.
     *  @exception IllegalActionException If the director cannot be created.
     *  @exception NameDuplicationException If there is already an
     *  attribute with the name "_director".
     */
    protected CaseDirector _createDirector() throws IllegalActionException,
    NameDuplicationException {
        return new CaseDirector(this, "_director");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The current refinement. */
    protected Refinement _current;

    /** The default refinement. */
    protected Refinement _default;

    /** The director. */
    protected CaseDirector _director;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the class. */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        setClassName("ptolemy.actor.lib.hoc.Case");

        // Create the control port.
        control = new PortParameter(this, "control");
        // FIXME: This is awkward... If I provide some
        // non-boolean control input, I get obscure type
        // conflict error messages and have to change this
        // to match.
        control.setExpression("true");
        ParameterPort port = control.getPort();
        // Put the control input on the bottom of the actor.
        StringAttribute controlCardinal = new StringAttribute(port, "_cardinal");
        controlCardinal.setExpression("SOUTH");

        // Create the default refinement.
        _default = newRefinement("default");

        // Create the director.
        _director = _createDirector();
    }
}
