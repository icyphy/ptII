/* Base class for code generators for static scheduling models of computation.

 Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.codegen.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// StaticSchedulingCodeGenerator

/** Base class for code generators for static scheduling models of computation.
 *
 *  @author Edward A. Lee, Gang Zhou, Ye Zhou, Contributor: Christopher Brooks
 *  @version $Id$
 *   StaticSchedulingCodeGenerator.java,v 1.25 2005/07/12 19:29:15 mankit Exp $
 *  @since Ptolemy II 5.1
 *  @Pt.ProposedRating Red (eal)
 *  @Pt.AcceptedRating Red (eal)
 */
public class StaticSchedulingCodeGenerator extends CodeGenerator
    implements ActorCodeGenerator {
    /** Create a new instance of the C code generator.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If super class throws it.
     *  @exception NameDuplicationException If super class throws it.
     */
    public StaticSchedulingCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the body code that lies between variable declaration
     *  and wrapup.
     *  @exception IllegalActionException If the generateFireCode(StringBuffer)
     *  method throws the exceptions.
     *  @return The generated body code.
     */
    public String generateBodyCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(comment("Static schedule:"));
        generateFireCode(code);
        return code.toString();
    }

    /** Generate code.  This is the main entry point.
     *  @param code The code stream into which to generate the code.
     *  @exception KernelException If a type conflict occurs or the model
     *  is running.
     */
    public void generateCode(StringBuffer code) throws KernelException {
        // If necessary, create a manager.
        Actor container = ((Actor) getContainer());
        Manager manager = container.getManager();

        if (manager == null) {
            CompositeActor toplevel =
                (CompositeActor) ((NamedObj) container).toplevel();
            manager = new Manager(toplevel.workspace(), "Manager");
            toplevel.setManager(manager);
        }

        try {
            manager.preinitializeAndResolveTypes();
            super.generateCode(code);
        } finally {
            manager.wrapup();
        }
    }

    /** Generate into the specified code stream the code associated
     *  with the execution of the container composite actor. This method
     *  calls the generateFireCode() method of the code generator helper
     *  associated with the director of the container.
     *  @param code The code stream into which to generate the code.
     *  @exception IllegalActionException If a static scheduling director is
     *   missing or the generateFireCode(StringBuffer) method of the
     *   director helper throws the exception.
     */
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
        CompositeEntity model = (CompositeEntity) getContainer();

        // NOTE: The cast is safe because setContainer ensures
        // the container is an Actor.
        ptolemy.actor.Director director = ((Actor) model).getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "The model "
                    + model.getName() + " does not have a director.");
        }

        if (!(director instanceof StaticSchedulingDirector)) {
            throw new IllegalActionException(this, "The director of the model "
                    + model.getName() + " is not a StaticSchedulingDirector.");
        }

        ComponentCodeGenerator directorHelper =
            _getHelper((NamedObj) director);
        ((Director) directorHelper).generateFireCode(code);
    }

    /** Set the container of this object to be the given container.
     *  This method overrides the base class to ensure that that the
     *  container is an Actor.
     *  @param container The given container.
     *  @exception IllegalActionException If the given container
     *   is not null and not an instance of CompositeActor.
     *  @exception NameDuplicationException If the super class throws the
     *   exception.
     */
    public void setContainer(NamedObj container)
        throws IllegalActionException, NameDuplicationException {
        if ((container != null) && !(container instanceof CompositeActor)) {
            throw new IllegalActionException(this, container,
                    "StaticSchedulingCodeGenerator can only be contained "
                            + " by CompositeActor");
        }

        super.setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the code generator helper associated with the given component.
     *  @param actor The given component actor.
     *  @exception IllegalActionException If the given actor is an
     *   implementing class for ActorCodeGenerator.
     *  @return The code generator helper.
     */
    protected ComponentCodeGenerator _getHelper(NamedObj actor)
            throws IllegalActionException {
        ComponentCodeGenerator helperObject = super._getHelper(actor);

        if (!(helperObject instanceof ActorCodeGenerator)) {
            throw new IllegalActionException(this,
                    "Cannot generate code for this actor: " + actor
                            + ". Its helper class does not"
                            + " implement ActorCodeGenerator.");
        }

        return helperObject;
    }
}
