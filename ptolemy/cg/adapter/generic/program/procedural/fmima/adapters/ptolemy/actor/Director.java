/* Code generator adapter for generating FMIMA code for Director.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.fmima.adapters.ptolemy.actor;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.fmima.FMIMACodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

////Director

/**
 Code generator adapter for generating FMIMA code for Director.

 @see GenericCodeGenerator
 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)

 */
public class Director extends FMIMACodeGeneratorAdapter {

    /** Construct the code generator adapter associated with the given director.
     *  Note before calling the generate*() methods, you must also call
     *  setCodeGenerator(GenericCodeGenerator).
     *  @param director The associated director.
     */
    public Director(ptolemy.actor.Director director) {
        super(director);
    }

    ///////////////////////////////////////////////////////////////////
    ////                Public Methods                           ////

    /** Generate the code for the firing of actors.
     *  In this base class, it is attempted to fire all the actors once.
     *  In subclasses such as the adapters for SDF and Giotto directors, the
     *  firings of actors observe the associated schedule. In addition,
     *  some special handling is needed, e.g., the iteration limit in SDF
     *  and time advancement in Giotto.
     *  @return The generated code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating fire code for the actor.
     */
    @Override
    public String generateFMIMA() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        // Extending ProceduralCodeGenerator start.
        //NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());
        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getComponent());
        Iterator<?> actors = ((CompositeActor) adapter.getComponent()
                .getContainer()).deepEntityList().iterator();
        code.append(getCodeGenerator()
                .comment(
                        "ptolemy/cg/adapter/generic/program/procedural/fmima/adapters/ptolemy/actor/Director.java start"
                                + _eol
                                + "   "
                                + adapter.getComponent().getName()));
        // Extending ProceduralCodeGenerator end.

        // Extending GenericCodeGenerator start.
        // Iterator<?> actors = ((CompositeActor) getComponent().getContainer()).deepEntityList().iterator();
        // code.append("<li>" + /*adapter.*/getComponent().getName() + "</li>" + _eol);
        // Extending GenericCodeGenerator start.

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            FMIMACodeGeneratorAdapter codeGeneratorAdapter = null;
            Object object = getCodeGenerator().getAdapter(actor);
            try {
                codeGeneratorAdapter = (FMIMACodeGeneratorAdapter) object;
            } catch (ClassCastException ex) {
                throw new IllegalActionException(
                // Extending ProceduralCodeGenerator start.
                        adapter.
                        // Extending ProceduralCodeGenerator end.

                        // Extending GenericCodeGenerator start.
                        /* adapter.*/
                        // Extending GenericCodeGenerator end.

                        getComponent(), ex, "Failed to cast " + object
                                + " of class " + object.getClass().getName()
                                + " to "
                                + FMIMACodeGeneratorAdapter.class.getName()
                                + ".");

            }
            code.append(codeGeneratorAdapter.generateFMIMA());
        }
        code.append(getCodeGenerator()
                .comment(
                        "ptolemy/cg/adapter/generic/program/procedural/fmima/adapters/ptolemy/actor/Director.java end"));
        return code.toString();
    }
}
