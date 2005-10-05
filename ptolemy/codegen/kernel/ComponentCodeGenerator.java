/* Interface for code generator helper classes.

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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// ComponentCodeGenerator

/** Interface for components that can generate code.
 *  Classes that implement this interface are helper classes, closely
 *  associated with a specific Ptolemy II component (such as an actor), and
 *  able to generate code in some language to implement the functionality of
 *  that component.
 *
 *  @author Christopher Brooks, Edward Lee, Jackie Leung, Gang Zhou, Ye Zhou
 *  @version $Id$
 *  @since Ptolemy II 5.1
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Yellow (eal)
 */
public interface ComponentCodeGenerator {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate into the specified code stream the code associated
     *  with initialization of the container composite actor.
     *  @exception IllegalActionException If something goes wrong.
     *  @return The initialize code of the containing composite actor.
     */
    public String generateInitializeCode() throws IllegalActionException;

    /** Generate into the specified code stream the code associated
     *  with wrapping up the container composite actor.
     *  @param code The code stream into which to generate the code.
     *  @exception IllegalActionException If something goes wrong.
     */
    public void generateWrapupCode(StringBuffer code)
            throws IllegalActionException;

    /** Return the associated component.
     *  @return The component for which this is a helper to generate code.
     */
    public NamedObj getComponent();
    
    /** Set the code generator for the helper class implementing this interface.
     *  @param codeGenerator The code generator to be set.
     */
    public void setCodeGenerator(CodeGenerator codeGenerator);
}
