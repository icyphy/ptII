/* Abstract base class for variable step size ODE solvers.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating red (liuj@eecs.berkeley.edu)
@AcceptedRating Green (reviewmoderator@eecs.berkeley.edu)

*/


package ptolemy.domains.ct.kernel.solver;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.ct.kernel.*;
import java.util.Enumeration;


//////////////////////////////////////////////////////////////////////////
//// VariableStepSolver
/**
Base class for variable step size (with local error control) ODE solvers.
@author  Jie Liu
@version $Id$
@see classname
@see full-classname
*/
public abstract class VariableStepSolver extends ODESolver{

    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public VariableStepSolver() {
        super();
    }

    /** Construct a solver in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this solver.
     */
    public VariableStepSolver(String name) {
        super(name);
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
    public VariableStepSolver(Workspace workspace, String name) {
        super(workspace, name);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Return true always, since this is a fixed step size method.
     *  @return true always.
     
    public final boolean errorTolerable(){
        try {
            CTDirector dir = (CTDirector)getContainer();
            CTScheduler sch = (CTScheduler)dir.getScheduler();

            boolean tolerable = true;
            Enumeration errCtrlActors = sch.errorControlActors();
            while(errCtrlActors.hasMoreElements()) {
                CTErrorControlActor eca =
                    (CTErrorControlActor)errCtrlActors.nextElement();
                tolerable = tolerable && eca.isSuccessful();
            }
            if(DEBUG) {
                if(tolerable) {
                    System.out.println("A successful step at time: " +
                    dir.getCurrentTime() + " with step size: " +
                    dir.getCurrentStepSize());
                } else {
                    if(dir.STAT) {
                        dir.NFAIL ++;
                    }
                    System.out.println("Fail step at time: " +
                    dir.getCurrentTime() + " with step size: " +
                    dir.getCurrentStepSize());
                }
            }
            return tolerable;
        }catch (NullPointerException e) {
            throw new InvalidStateException(this,
                "has no scheduler avaliable");
        }
    }
    */
    /** Resolve the next step size if the current
     *  step is a success. It ask all the error control actors for
     *  their suggestion and resolve the minimum. Set this value
     *  to the director. (Director may further change it for the sake
     *  of break points/event detection)
     *  Different solver may implement it differently.
     *
     * @exception IllegalActionException Not thrown in this base
     *  class. May be needed by the derived class.
     
    public void resolveNextStepSize() {
        CTDirector dir = (CTDirector)getContainer();
        if(dir == null) {
            throw new InvalidStateException(this,
                " has no director.");
        }
        CTScheduler sch = (CTScheduler)dir.getScheduler();
        if(sch == null) {
            throw new InvalidStateException(this, dir,
                " has no scheduler.");
        }
        double newh = dir.getCurrentStepSize()*5.0;
        Enumeration errCtrlActors = sch.errorControlActors();
        while(errCtrlActors.hasMoreElements()) {
            CTErrorControlActor eca =
                (CTErrorControlActor)errCtrlActors.nextElement();
            newh = Math.min(newh, eca.suggestedNextStepSize());
        }
        dir.setSuggestedNextStepSize(newh);
        }*/

    /** Override the startOverLastStep() for the base class. Do nothing.
     
    public void startOverLastStep() throws NumericalNonconvergeException{
        CTDirector dir = (CTDirector)getContainer();
        if(dir == null) {
            throw new InvalidStateException(this,
                "has no director.");
        }
        double currentstep = dir.getCurrentStepSize();
        dir.setCurrentTime(dir.getCurrentTime()-currentstep);
        if(currentstep < dir.getMinStepSize()) {
            throw new NumericalNonconvergeException(this,
                "minimum step size reached but still can't find the"
                + " solution at time" + dir.getCurrentTime());
        }
        dir.setCurrentStepSize(currentstep/2.0);
        if(VERBOSE) {
            System.out.println("restart at time: " +
            dir.getCurrentTime() + " with step size: " +
            dir.getCurrentStepSize());
        }
    }
    */

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
