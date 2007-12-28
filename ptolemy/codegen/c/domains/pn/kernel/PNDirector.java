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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.Director;
import ptolemy.data.IntToken;
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

    /** Do nothing in generating fire function code. The fire code is
     *  wrapped in a for/while loop inside the thread function.
     *  The thread function is generated in 
     *  {@link #generatePreinitializeCode()} outside the main function. 
     *  @return An empty string.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        return "";
    }
    
    /** Generate the body code that lies between variable declaration
     *  and wrapup.
     *  @return The generated body code.
     *  @exception IllegalActionException If the
     *  {@link #generateFireCode()} method throws the exceptions.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        
        code.append(_codeGenerator.comment("Create a thread for each actor."));
        code.append(_eol + "pthread_attr_t pthread_custom_attr;" + _eol);
        code.append("pthread_attr_init(&pthread_custom_attr);" + _eol + _eol);

        List actorList = 
            ((CompositeActor) _director.getContainer()).deepEntityList();
        
        Iterator actors = actorList.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            
            code.append("pthread_create(");
            code.append("&thread_" + CodeGeneratorHelper.generateName((NamedObj) actor));
            code.append(", &pthread_custom_attr, ");
            code.append(CodeGeneratorHelper.generateName((NamedObj) actor));
            code.append(", NULL);" + _eol);
        }

        return code.toString();
    }

    /** Get the files needed by the code generated from this helper class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this helper class.
     *  @exception IllegalActionException If something goes wrong.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.add("<pthread.h>");
        //files.add("<thread.h>");
        return files;
    }

    /** Return the libraries specified in the "libraries" blocks in the
     *  templates of the actors included in this CompositeActor.
     *  @return A Set of libraries.
     *  @exception IllegalActionException If thrown when gathering libraries.
     */
    public Set getLibraries() throws IllegalActionException {
        Set libraries = new LinkedHashSet();
        libraries.add("pthread");
        //libraries.add("thread");
        return libraries;
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
        return code.toString();
    }
    
    /** Generate the preinitialize code for the associated PN director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating preinitialize code for the actor.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = 
            new StringBuffer(super.generatePreinitializeCode());
        
        List actorList = 
            ((CompositeActor) _director.getContainer()).deepEntityList();

        // Generate the global terminate flag which tells actor
        // whether or not to continue execution.
        code.append("boolean terminate = false;" + _eol);
        
        Iterator actors = actorList.iterator();
        while (actors.hasNext()) {
            // Generate the thread pointer.
            Actor actor = (Actor) actors.next();
            code.append("pthread_t *thread_");
            code.append(CodeGeneratorHelper.generateName((NamedObj) actor));
            code.append(";" + _eol);
        }
        
        // Generate the function for each actor thread.
        actors = actorList.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helper = 
                (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            
            code.append(_eol + "void " + CodeGeneratorHelper.generateName(
                    (NamedObj) actor) + "(void) {" + _eol);

            // if firingCountLimit exists, generate for loop.
            if (actor instanceof LimitedFiringSource) {
                int firingCount = ((IntToken) ((LimitedFiringSource) actor)
                        .firingCountLimit.getToken()).intValue();
                code.append("int i = 0;" + _eol);
                code.append("for (; i < " + firingCount 
                        + " && !terminate; i++) {" + _eol);
                
            } else {
                code.append("while (!terminate) {" + _eol);                
            }
            
            code.append(helper.generateFireCode());
            code.append(helper.generateTypeConvertFireCode());
            code.append("}" + _eol + "}" + _eol);
        }
        
        
        return code.toString();
    }

    /** Generate the wrapup code for the associated PN director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating preinitialize code for the actor.
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = 
            new StringBuffer(super.generateWrapupCode());
        
        Iterator actors = ((CompositeActor) _director.getContainer())
        .deepEntityList().iterator();

        while (actors.hasNext()) {
            // Generate the thread pointer.
            Actor actor = (Actor) actors.next();
            
            code.append("pthread_join(");
            code.append("thread_" + CodeGeneratorHelper.generateName((NamedObj) actor));
            code.append(", NULL);" + _eol);
        }
        return code.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

}
