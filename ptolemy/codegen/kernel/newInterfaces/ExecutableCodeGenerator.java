/* Interface for code generator helper classes.

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
package ptolemy.codegen.kernel.newInterfaces;

import ptolemy.codegen.util.PartialResult;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ActorCodeGenerator

/** FIXME: class comments needed.
 *
 *  @author Man-kit Leung
 *  @version $Id$
 *  @since Ptolemy II 6.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Yellow (eal)
 */
public interface ExecutableCodeGenerator extends ComponentCodeGenerator {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate into the specified string buffer the code associated
     *  with one firing of the container composite actor.
     *  @return The generated fire code.
     *  @exception IllegalActionException If something goes wrong.
     */
    public PartialResult fire() throws IllegalActionException;

    public PartialResult iterate(PartialResult countExpression) throws IllegalActionException;

    
    /** Generate the postfire code of the associated composite actor.
     *
     *  @return The postfire code of the associated composite actor.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating the postfire code for the actor.
     */
    public PartialResult postfire() throws IllegalActionException;

    
    /** Generate the prefire code of the associated composite actor.
    *
    *  @return The prefire code of the associated composite actor.
    *  @exception IllegalActionException If the helper associated with
    *   an actor throws it while generating the prefire code for the actor.
    */
    public PartialResult prefire() throws IllegalActionException;

    
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
    public PartialResult preintialize() throws IllegalActionException;

    public PartialResult stop() throws IllegalActionException;

}
