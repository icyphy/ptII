/* Interface for defining how an object can be invoked.

 Copyright (c) 1997-1998 The Regents of the University of California.
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
@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Red
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
is defined to be one firing of the prefire() method, followed by
any number of firings of the fire() method, followed by one firing
of the postfire() method.
The prefire() method returns true to indicate that firing
can occur.  The postfire() method returns false if no further firings
should occur. The initialize(), fire() and postfire() methods may produce
output data.
Since the Token class is immutable, the output data is not cloned
when there are multiple receivers.

@author Mudit Goel, Edward A. Lee, Lukito Muliadi, Steve Neuendorffer
@version $Id$
*/
public interface Executable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This fires an actor and may be invoked several times between
     *  invocations of prefire() and postfire(). It may produce output
     *  data. Typically, the fire() method performs the computation associated
     *  with an actor.
     *
     *  @exception IllegalActionException If firing is not permitted.
     */
    public void fire() throws IllegalActionException;

    /** This method should be invoked exactly once per execution
     *  of an application, before any of these other methods are invoked.
     *  It may produce output data.  This method typically initializes
     *  internal members of an actor and produces initial output data.
     *
     *  @exception IllegalActionException If initializing is not permitted.
     */
    public void initialize() throws IllegalActionException;

    /** This method should be invoked once per iteration, after the last
     *  invocation of fire() in that iteration. It may produce output data.
     *  It returns true if the execution can proceed into the next iteration.
     *  This method typically wraps up an iteration, which may involve
     *  updating local state. In an opaque, non-atomic entity, it may also
     *  transfer output data.
     *
     *  @return True if the execution can continue.
     *  @exception IllegalActionException If postfiring is not permitted.
     */
    public boolean postfire() throws IllegalActionException;

    /** This method should be invoked once per iteration, before the first
     *  invocation of fire() in that iteration.  It returns true if the
     *  iteration can proceed (the fire() method can be invoked). Thus
     *  this method will typically check preconditions for an iteration, if
     *  there are any. In an opaque, non-atomic entity,
     *  it may move data into an inner subsystem.
     *
     *  @return True if the iteration can proceed.
     *  @exception IllegalActionException If prefiring is not permitted.
     */
    public boolean prefire() throws IllegalActionException;

    /** This method is invoked to immediately terminate any execution
     *  within an actor.
     */
    public void terminate();

    /** This method should be invoked exactly once per execution
     *  of an application.  None of the other action methods should be
     *  be invoked after it.  It finalizes an execution, typically closing
     *  files, displaying final results, etc.
     *
     *  @exception IllegalActionException If wrapup is not permitted.
     */
    public void wrapup() throws IllegalActionException;
}
