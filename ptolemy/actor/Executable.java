/* Interface for defining how an object can be invoked.

 Copyright (c) 1997- The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Executable
/**
This interface defines the <i>action methods</i>, which determine
how an object can be invoked. It should be implemented by actors
and directors. In an execution of an application,
the initialize() method should be
invoked exactly once, followed by any number of iterations, followed
by exactly one invocation of the wrapup() method. An <i>iteration</i>
is defined to be one firing of the prefire() method, any number of
firings of the fire() method, and one firing of the postfire() method.
The prefire() method returns a boolean that indicates whether firing
can occur.  The initialize(), fire() and postfire() methods may produce
output data (which can result in a CloneNotSupported exception).

@author Mudit Goel, Edward A. Lee
@version $Id$
*/
public interface Executable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This fires an actor and may be invoked several times between
     *  invocations of prefire() and postfire(). It may produce output
     *  data. Typically, the fire() method performs the computation associated
     *  with an actor.
     */
    public void fire()
            throws CloneNotSupportedException, IllegalActionException;

    /** This method should be invoked exactly once per execution
     *  of an application, before any of these other methods are invoked.
     *  It may produce output data.  This method typically initializes
     *  internal members of an actor and produces initial output data.
     */
    public void initialize()
            throws CloneNotSupportedException, IllegalActionException;

    /** This method should be invoked once per iteration, after the last
     *  invocation of fire() in that iteration. It may produce output data.
     *  It returns true if the execution can proceed into the next iteration.
     *  @return True if the execution can continue.  This method typically
     *  wraps up an iteration, which may involve updating local state.
     *  In an opaque, non-atomic entity, it may also transfer output data.
     */
    public boolean postfire()
           throws CloneNotSupportedException, IllegalActionException;

    /** This method should be invoked once per iteration, before the first
     *  invocation of fire() in that iteration.  It returns true if the
     *  iteration can proceed (the fire() method can be invoked). Thus
     *  this method will typically check preconditions for an iteration, if
     *  there are any. In an opaque, non-atomic entity,
     *  it may move data into an inner subsystem.
     *  @return True if the iteration can proceed.
     */
    public boolean prefire()
            throws CloneNotSupportedException, IllegalActionException,
            NameDuplicationException;

    /** This method should be invoked exactly once per execution
     *  of an application.  None of the other action methods should be
     *  be invoked afer it.  It finalizes an execution, typically closing
     *  files, displaying final results, etc.
     */
    public void wrapup() throws IllegalActionException;
}
