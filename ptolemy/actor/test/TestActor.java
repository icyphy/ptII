/* An atomic actor for testing.

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

package ptolemy.actor.test;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.mutation.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// TestActor
/**
A TestActor is a simple atomic actor that is used for testing the actor
package. It overrides the action methods with methods that record their
invocation in a list.

@author Edward A. Lee
@version $Id$
*/
public class TestActor extends AtomicActor {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public TestActor(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Mutation test.  Create a new instance of this test actor with
     *  the same container using queueMutation of the director.
     *  If there is no director, return doing nothing.
     *  @return The new actor.
     */
    public void addActor(final String name) {
        Mutation m = new Mutation() {
            // Create the mutation
            public void perform() {
                try {
                    _testactor =
                    new TestActor((CompositeActor)getContainer(), name);
                } catch (Exception ex) {
                    // Ignore exceptions
                }
            }
            public void update(MutationListener listener) {
                listener.addEntity((CompositeEntity)getContainer(), _testactor);

            }
            // Test actor generated in by perform().
            private TestActor _testactor = null;
        };
        Director dir = getDirector();
        if (dir != null) {
            dir.queueMutation(m);
        }
    }

    /** Clear the record, and reset the iteration count to zero.
     */
    public void clear() {
        _actions = new StringBuffer(1024);
    }

    /** Record the firing.
     */
    public void fire() {
        _actions.append(getFullName() + ".fire\n");
    }

    /** Get the record.
     */
    public static String getRecord() {
        return _actions.toString();
    }

    /** Record the initialization.
     */
    public void initialize() {
        _actions.append(getFullName() + ".initialize\n");
    }

    /** Record the invocation, then return true.
     */
    public boolean postfire() {
        _actions.append(getFullName() + ".postfire\n");
        return true;
    }

    /** Record the invocation, then return true.
     */
    public boolean prefire() {
        _actions.append(getFullName() + ".prefire\n");
        return true;
    }

    /** Record the invocation.
     */
    public void wrapup() {
        _actions.append(getFullName() + ".wrapup\n");
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // The list of action method invocations.
    private static StringBuffer _actions = new StringBuffer(1024);
}
