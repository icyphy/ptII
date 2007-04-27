/* Test class for codegen.kernel.ActorCodeGeneratro

 Copyright (c) 2007 The Regents of the University of California.
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
package ptolemy.codegen.kernel.test.actor;

import java.util.HashSet;
import java.util.Set;


import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.codegen.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**  
 *  Test class for codegen.kernel.ActorCodeGeneratro
 *
 *  @author Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 6.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class TypedCompositeActor implements ActorCodeGenerator {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create read and write offset variables if needed for the associated 
     *  composite actor. It delegates to the director helper of the local 
     *  director.
     *  @return A string containing declared read and write offset variables. 
     *  @exception IllegalActionException If the helper class cannot be found
     *   or the director helper throws it.
     */
    public String createOffsetVariablesIfNeeded()
            throws IllegalActionException {
        return "/* OffsetVariables */";
    }

    /** Generate into the specified string buffer the code associated
     *  with one firing of the container composite actor.
     *  @return The generated fire code.
     *  @exception IllegalActionException If something goes wrong.
     */
    public String generateFireCode() throws IllegalActionException {
        return "/* FireCode*/";
    }

    /** Generate The fire function code. This method is called when the firing
     *  code of each actor is not inlined. Each actor's firing code is in a 
     *  function with the same name as that of the actor.
     * 
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        return "/* FireFunctionCode*/";
    }

    /** Generate the main entry point.
     *  @return Return the definition of the main entry point for a program.
     *  In C, this would be defining main().
     *  @exception IllegalActionException Not thrown in this base class.
     */
    // public String generateMainEntryCode() throws IllegalActionException;
    /** Generate the main entry point.
     *  @return Return the a string that closes optionally calls exit
     *  and closes the main() method 
     *  @exception IllegalActionException Not thrown in this base class.
     */
    // public String generateMainExitCode() throws IllegalActionException;
    /** Generate mode transition code. It delegates to the director helper 
     *  of the local director. The mode transition code generated in this 
     *  method is executed after each global iteration, e.g., in HDF model. 
     * 
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the director helper throws it 
     *   while generating mode transition code. 
     */
    public void generateModeTransitionCode(StringBuffer code)
            throws IllegalActionException {
    }

    /** Generate the postfire code of the associated composite actor.
     *
     *  @return The postfire code of the associated composite actor.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating postfire code for the actor
     *   or while creating buffer size and offset map.
     */
    public String generatePostfireCode() throws IllegalActionException {
        return "/* PostfireCode */";
    }

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
    public String generatePreinitializeCode() throws IllegalActionException {
        return "/* PreinitializeCode */";
    }

    /** Get the files needed by the code generated from this helper class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this helper class.
     *  @exception IllegalActionException If something goes wrong.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        return new HashSet();
    }

    /** Return a set of parameters that will be modified during the execution
     *  of the model. The actor gets those variables if it implements 
     *  ExplicitChangeContext interface or it contains PortParameters. 
     * 
     *  @return a set of parameters that will be modified.
     *  @exception IllegalActionException If an actor throws it while getting 
     *   modified variables. 
     */
    public Set getModifiedVariables() throws IllegalActionException {
        return new HashSet();
    }

    /**
     * Generate the shared code. This is the first generate method invoked out
     * of all, so any initialization of variables of this helper should be done
     * in this method. In this base class, return an empty set. Subclasses may
     * generate code for variable declaration, defining constants, etc.
     * @return An empty set in this base class.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public Set getSharedCode() throws IllegalActionException {
        return new HashSet();
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        return "/* Variable Declaration */";
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization()
            throws IllegalActionException {
        return "/* VariableInitialization */";
    }

    /** Generate into the specified code stream the code associated
     *  with initialization of the container composite actor.
     *  @exception IllegalActionException If something goes wrong.
     *  @return The initialize code of the containing composite actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        return "/* InitializeCode */";
    }

    /** Generate into the specified code stream the code associated
     *  with wrapping up the container composite actor.
     *  @exception IllegalActionException If something goes wrong.
     *  @return The wrapup code of the containing composite actor.
     */
    public String generateWrapupCode() throws IllegalActionException {
        return "/* WrapupCode */";
    }

    /** Return the associated component.
     *  @return The component for which this is a helper to generate code.
     */
    public NamedObj getComponent() {
        return new ptolemy.kernel.util.NamedObj();
    }

    /** Set the code generator for the helper class implementing this interface.
     *  @param codeGenerator The code generator to be set.
     */
    public void setCodeGenerator(CodeGenerator codeGenerator) {
    }

}
