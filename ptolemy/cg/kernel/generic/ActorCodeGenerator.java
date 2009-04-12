/* Interface for code generator helper classes.

 Copyright (c) 2005-2009 The Regents of the University of California.
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
package ptolemy.cg.kernel.generic;

import java.util.Set;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ActorCodeGenerator

/** FIXME: class comments needed.
 *  FIXME: This is C specific!
 *
 *  @author Christopher Brooks, Edward Lee, Jackie Leung, Gang Zhou, Ye Zhou,
 *   Contributors: Teale Fristoe
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Yellow (eal)
 */
public interface ActorCodeGenerator extends ComponentCodeGenerator {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Generate into the specified string buffer the code associated
     *  with one firing of the container composite actor.
     *  @return The generated fire code.
     *  @exception IllegalActionException If something goes wrong.
     */
    public String generateFireCode() throws IllegalActionException;

    /** Generate The fire function code. This method is called when the firing
     *  code of each actor is not inlined. Each actor's firing code is in a
     *  function with the same name as that of the actor.
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode() throws IllegalActionException;

    public String generateIterationCode(String countExpression) throws IllegalActionException;

    /** Generate mode transition code. It delegates to the director helper
     *  of the local director. The mode transition code generated in this
     *  method is executed after each global iteration, e.g., in HDF model.
     *
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the director helper throws it
     *   while generating mode transition code.
     *   // FIXME rodiers: remove this method, this is ModalModel specific.
     */
    public void generateModeTransitionCode(StringBuffer code)
            throws IllegalActionException;

    /** Generate the postfire code of the associated composite actor.
     *
     *  @return The postfire code of the associated composite actor.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating the postfire code for the actor.
     */
    public String generatePostfireCode() throws IllegalActionException;


    /** Generate the prefire code of the associated composite actor.
    *
    *  @return The prefire code of the associated composite actor.
    *  @exception IllegalActionException If the helper associated with
    *   an actor throws it while generating the prefire code for the actor.
    */
    public String generatePrefireCode() throws IllegalActionException;


    /** Generate the preinitialize code of the associated composite actor.
     *  It first creates buffer size and offset map for its input ports and
     *  output ports. It then gets the result of generatePreinitializeCode()
     *  method of the local director helper.
     *
     *  @return The preinitialize code of the associated composite actor.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating preinitialize code for the actor
     *   or while creating buffer size and offset map.
     */
    public String generatePreinitializeCode() throws IllegalActionException;

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     *   FIXME rodiers: this is procedural code specific
     */
    public String generateVariableDeclaration() throws IllegalActionException;

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization()
            throws IllegalActionException;

    /** Get the files needed by the code generated from this helper class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this helper class.
     *  @exception IllegalActionException If something goes wrong.
     *  // FIXME rodiers: this is specific for procedural code
     */
    public Set<String> getHeaderFiles() throws IllegalActionException;

    /** Return a set of the directories to search for the actor's include files.
     * @return A set of directories to search for the actor's include files.
     * @exception IllegalActionException If thrown when finding the directories.
     * // FIXME rodiers: this is specific for procedural code
     */
    public Set<String> getIncludeDirectories() throws IllegalActionException;

    /** Return a set of the libraries to link the generated code to.
     * @return A set of libraries to link.
     * @exception IllegalActionException If thrown when finding the libraries.
     * // FIXME rodiers: this is specific for C
     */
    public Set<String> getLibraries() throws IllegalActionException;

    /** Return a set of the directories to search for libraries.
     * @return A set of directories to search.
     * @exception IllegalActionException If thrown when finding the libraries.
     * // FIXME rodiers: this is specific for C
     */
    public Set<String> getLibraryDirectories() throws IllegalActionException;

    /** Return a set of parameters that will be modified during the execution
     *  of the model. The actor gets those variables if it implements
     *  ExplicitChangeContext interface or it contains PortParameters.
     *
     *  @return a set of parameters that will be modified.
     *  @exception IllegalActionException If an actor throws it while getting
     *   modified variables.
     */
    public Set<Parameter> getModifiedVariables() throws IllegalActionException;

    /**
     * Generate the shared code. This is the first generate method invoked out
     * of all, so any initialization of variables of this helper should be done
     * in this method. In this base class, return an empty set. Subclasses may
     * generate code for variable declaration, defining constants, etc.
     * @return An empty set in this base class.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public Set<String> getSharedCode() throws IllegalActionException;

}
