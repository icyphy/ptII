/* Director for dynamic dataflow.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@ProposedRating Red (yourname@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.ddf.kernel;

import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// DDFDirector
/**
Director for dynamic dataflow.
<p>
The SDF director has a single parameter, "iterations", corresponding to a
limit on the number of times the director will fire its hierarchy
before it returns false in postfire.  FIXME: Explain what an iteration
is, and elaborate, as in the DDF chapter from Ptolemy Classic.
The default value of the iterations parameter is an IntToken with value zero,
which indicates that the run should proceed indefinitely.
<p>
This director is based on the design in Ptolemy Classic by Soonhoi Ha,
Edward A. Lee, and Thomas M. Parks.

@author Gilles Guerassimoff
@version $Id$
*/
public class DDFDirector extends Director {

    // NOTE: This class shares some code with SDFDirector.
    // Should SDFDirector be a base class?

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public DDFDirector() {
        super();
        _init();
    }

    /** Construct a director in the given workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     */
    public DDFDirector(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the name collides with an
     *   attribute in the container.
     */
    public DDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A Parameter representing the number of times that postfire may be
     *  called before it returns false.  If the value is less than or
     *  equal to zero, then the execution will never return false in postfire,
     *  and thus the execution can continue forever.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute one iteration of the graph.
     *
     *  @exception IllegalActionException If this director does
     *   not have a container.
     */
    public void fire() throws IllegalActionException {
        // FIXME: Implement a reasonable algorithm.
        super.fire();
    }

    /** Return a new receiver consistent with the DDF domain.
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** Preinitialize the actors associated with this director and
     *  initialize the number of iterations to zero.  The order in which
     *  the actors are preinitialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _iteration = 0;
    }

    /** Return false if the system has finished executing by
     *  reaching the iteration limit. Increment the number of iterations.
     *  @return True if the Director wants to be fired again in the
     *   future.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public boolean postfire() throws IllegalActionException {
        int numiterations = ((IntToken) (iterations.getToken())).intValue();
        _iteration++;
        if((numiterations > 0) && (_iteration >= numiterations)) {
            _iteration = 0;
            return false;
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object.
     */
    private void _init() {
        try {
            iterations = new Parameter(this,"iterations",new IntToken(0));
        } catch (Exception e) {
            throw new InternalErrorException(
                    "Cannot create default iterations parameter:\n" +
                    e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _iteration = 0;
}
