/* An atomic actor for testing.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.actor.test;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TypedTestActor

/**
 A TypedTestActor is a simple atomic actor that is used for testing the actor
 package. It overrides the action methods with methods that record their
 invocation in a list.

 @author Edward A. Lee, John S. Davis II
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class TypedTestActor extends TypedAtomicActor {
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
    public TypedTestActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the record, and reset the iteration count to zero.
     */
    public static void clear() {
        _actions = new StringBuffer(1024);
    }

    /** Record the firing.
     *  @exception IllegalActionException If the super class throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        _actions.append(getFullName() + ".fire\n");
    }

    /** Get the record.
     */
    public static String getRecord() {
        return _actions.toString();
    }

    /** Record the initialization.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _actions.append(getFullName() + ".initialize\n");
    }

    /** Record the invocation, then return true.
     */
    @Override
    public boolean postfire() {
        _actions.append(getFullName() + ".postfire\n");
        return true;
    }

    /** Record the invocation, then return true.
     */
    @Override
    public boolean prefire() {
        _actions.append(getFullName() + ".prefire\n");
        return true;
    }

    /** Record the invocation.
     */
    @Override
    public void wrapup() {
        _actions.append(getFullName() + ".wrapup\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The list of action method invocations.
    private static StringBuffer _actions = new StringBuffer(1024);
}
