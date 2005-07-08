/* An instance of FunctionDependencyOfFSMActor describes the function
 dependency information of an FSM actor.

 Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.domains.fsm.kernel;

import ptolemy.actor.util.FunctionDependency;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependencyOfFSMActor

/** An instance of FunctionDependencyOfFSMActor describes the function
 dependency relation between the externally visible ports of an FSM
 actor.
 <p>
 This class makes a conservative approximation of the dependency
 relation by assuming that all the output ports depend on all the
 input ports.

 @see FunctionDependency
 @author Haiyang Zheng
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class FunctionDependencyOfFSMActor extends FunctionDependency {
    /** Construct a FunctionDependencyOfFSMActor in the given actor.
     *  @param fsmActor The associated FSM actor.
     *  @param name The name of the FunctionDependency.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the attribute is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public FunctionDependencyOfFSMActor(FSMActor fsmActor, String name)
            throws IllegalActionException, NameDuplicationException {
        super(fsmActor, name);
    }
}
