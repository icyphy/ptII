/* The Impluse Backward Euler Solver

 Copyright (c) 1998 The Regents of the University of California.
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
@ProposedRating Green (yourname@eecs.berkeley.edu)
@AcceptedRating Green (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel.solver;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.ct.kernel.*;
import java.util.Enumeration;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// ImpulseBESolver
/** 
Description of the class
@author  youname
@version %W%	%G%
@see classname
@see full-classname
*/
public class ImpulseBESolver extends BackwardEulerSolver {
    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public ImpulseBESolver() {
        super();
        try {
            setName(_name);
        } catch (NameDuplicationException e) {
            // this should never happen.
            throw new InternalErrorException(
                "internal error when set name to an ImpulseBESolver.");
        }
    }

    /** Construct a solver in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this solver.
     */
    public ImpulseBESolver(Workspace workspace) {
        super(workspace);
        try {
            setName(_name);
        } catch (NameDuplicationException e) {
            // this should never happen.
            throw new InternalErrorException(
                "internal error when set name to an ImpulseBESolver.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Description
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    public boolean resolveStates() throws IllegalActionException {
        
        super.resolveStates();
        CTDirector dir = (CTDirector)getContainer();
        Enumeration actors = ((CTScheduler)dir.getScheduler()
            ).dynamicActorSchedule();
        while(actors.hasMoreElements()) {
            Actor next = (Actor)actors.nextElement();
            if(DEBUG) {
                System.out.println("update..."+((Nameable)next).getName());
            }
            next.postfire();
        }
        dir.setCurrentStepSize(-dir.getCurrentStepSize());
        super.resolveStates();
        dir.setCurrentStepSize(-dir.getCurrentStepSize());
        return true;
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    private static final String _name="CT_ImpulseBE_Solver" ;

}
