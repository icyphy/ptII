/* An instance of FunctionDependencyOfFSMActor describes the function
   dependency information of an FSM actor.

   Copyright (c) 2003-2004 The Regents of the University of California.
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

import ptolemy.actor.Actor;
import ptolemy.actor.FunctionDependency;

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
     *  @param actor The associated actor.
     */
    public FunctionDependencyOfFSMActor(Actor actor) {
        super(actor);
    }
}
