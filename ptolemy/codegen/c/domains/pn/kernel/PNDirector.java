/* Code generator helper class associated with the PNDirector class.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.domains.pn.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.codegen.kernel.Director;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////
////PNDirector

/**
 Code generator helper associated with the PNDirector class.
 This director initializes all the actors, then starts a thread
 for each actor that invokes the fire code for the actor in an
 infinite loop.

 FIXME: No communication between actors is implemented yet.

 FIXME: How to make it possible for executions to be finite?

 @author Edward A. Lee (based on SDFDirector helper class)
 @version $Id$
 @since Ptolemy II 7.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class PNDirector extends Director {

    /** Construct the code generator helper associated with the given
     *  PNDirector.
     *  @param pnDirector The associated
     *  ptolemy.domains.pn.kernel.PNDirector
     */
    public PNDirector(ptolemy.domains.pn.kernel.PNDirector pnDirector) {
        super(pnDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Override to do nothing.
     *  @return An empty string.
     */
    public String generateFireCode() {
        return "";
    }

    /** Generate the body code that lies between variable declaration
     *  and wrapup.
     *  @return The generated body code.
     *  @exception IllegalActionException If the
     *  {@link #generateFireCode()} method throws the exceptions.
     */
    public String generateBodyCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        return code.toString();
    }

    /** Generate the initialize code for the associated PN director.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_codeGenerator
                .comment("Initialization code of the PNDirector."));

        // Generate the code to initialize all the actors.
        code.append(super.generateInitializeCode());

        code.append(_codeGenerator.comment("Create a thread for each actor."));

        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);

            code.append("void* ");
            code.append(CodeStream.indent(CodeGeneratorHelper
                    .generateName((NamedObj) actor)));
            code.append("() {" + _eol);

            code.append(helper.generateFireCode());
            code.append(helper.generateTypeConvertFireCode());

            code.append("}" + _eol);
        }

        return code.toString();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

}
