/* Code generator helper class associated with the Director class.
 
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

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////
//// Director

/**
   Code generator helper associated with the Director class. This class
   is also associated with a code generator.
   
   @see CodeGenerator
   @author Ye Zhou
   @version $Id: Director.java
   @since Ptolemy II 5.0
   @Pt.ProsedRating Red (zhouye)
   @Pt.AcceptedRating Red (zhouye)
   
 */

public class Director implements ActorCodeGenerator {
    
    /** Construct the code generator helper associated with the given director.
     *  @param director The associated director.
     */
    public Director(ptolemy.actor.Director director) {
        _director = director;
    }
    
    /////////////////////////////////////////////////////////////////
    ////                Public Methods                           ////

    /** Get the code generator associated with this helper class.
     *  @return The code generator associated with this helper class.
     */
    public ComponentCodeGenerator getCodeGenerator() {
        return _codeGenerator;
    }
    
    /** Set the code generator associated with this helper class.
     *  @param codeGenerator The code generator associated with this
     *   helper class.
     */
    public void setCodeGenerator(CodeGenerator codeGenerator) {
        _codeGenerator = codeGenerator;
    }
    
    /** Generatre the code for the firing of actors.
     *  In this base class, it is attempted to fire all the actors once.
     *  In subclasses such as helper for SDF and Giotto directors, the
     *  firings of actors observe the associated schedule. In addition,
     *  some special handling is needed, e.g., the iteration limit in
     *  SDF and time advancement in Giotto.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating wrapup code for the actor.
     */
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
        code.append("/* The firing of the director. */\n");
        Iterator actors = ((CompositeActor) _codeGenerator.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = 
                (CodeGeneratorHelper) _getHelper((NamedObj)actor);
            helperObject.generateFireCode(code);
        }
    }
    
    /** Generate the initialize code of the director associated with this
     *  helper class. For this base class, the initialize code is just to
     *  generate the initialize code for each actor.
     *  @return The initialize code of the director associated with this
     *   helper class.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append("/* The initialization of the director. */\n");
        Iterator actors = ((CompositeActor) _codeGenerator.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            ComponentCodeGenerator helperObject = _getHelper((NamedObj)actor);
            // Set the buffer sizes and offsets of each port of the actor
            // to the default values in the initialization phase.
            ((CodeGeneratorHelper)helperObject).resetBufferSizes();
            ((CodeGeneratorHelper)helperObject).resetOffsets();
            code.append(helperObject.generateInitializeCode());
        }
        return code.toString();
    }

    /** Generate the wrapup code of the director associated with this helper
     *  class. For this base class, the wrapup code is just to generate
     *  the wrapup code for each actor.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException
     */
    public void generateWrapupCode(StringBuffer code)
            throws IllegalActionException {
        code.append("/* The wrapup of the director. */\n");
        Iterator actors = ((CompositeActor) _codeGenerator.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            ComponentCodeGenerator helperObject = _getHelper((NamedObj)actor);
            helperObject.generateWrapupCode(code);
        }
    }

    /** Return the director associated with this class.
     *  @return The director associated with this class.
     */
    public NamedObj getComponent() {
        return _director;
    }
    
    /** Get the buffer size of a given port. This base class method always
     *  returns 1. In subclasses, buffer size of a port may be dependent
     *  on the director.
     *  @param port The given port.
     *  @return The buffer size of the given port
     *  @throws IllegalActionException Subclass may throw an exception.
     */
    public int getBufferSize(IOPort port) throws IllegalActionException {
        return 1;
    }
    
    /////////////////////////////////////////////////////////////////////
    ////                   protected methods                         ////
    
    /** Get the helper class associated with the given component.
     *  @return the helper class associated with the given component.
     *  @exception IllegalActionException If the code generator throws
     *   it when getting the helper associated with the given component.
     */
    protected ComponentCodeGenerator _getHelper(NamedObj component)
            throws IllegalActionException {
        return _codeGenerator._getHelper(component);
    }
    
    ////////////////////////////////////////////////////////////////////
    ////                     private variables                      ////
    
    // The associate director;
    private NamedObj _director;
    
    // The code generator containing this director helper.
    protected CodeGenerator _codeGenerator;
}
