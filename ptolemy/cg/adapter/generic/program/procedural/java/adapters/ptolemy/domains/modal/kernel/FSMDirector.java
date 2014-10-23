/* Code generator adapter class associated with the FSMDirector class.

 Copyright (c)2009 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.domains.modal.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
////FSMDirector

/**
Code generator adapter associated with the FSMDirector class. This class
is also associated with a code generator.

@author William Lucas
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (sssf)
@Pt.AcceptedRating Red (sssf)
 */
public class FSMDirector
extends
ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.kernel.FSMDirector {

    /** Construct the code generator helper associated
     *  with the given modal controller.
     *  @param component The associated component.
     */
    public FSMDirector(ptolemy.domains.modal.kernel.FSMDirector component) {
        super(component);
        System.out
                .println("ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/modal/kernel/FSMDirector.java ctor");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Public methods                    ////

    /** We override the super method, because the declaration
     * of the variables are in the actor's files.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer(
                super.generateVariableDeclaration());
        Iterator<?> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor.getFullName().contains("_Controller")) {
                NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                        .getAdapter(actor);
                code.append(adapter.generateVariableDeclaration());
            }
        }

        return code.toString();
    }
}
