/* A schedule element that contains a reference to an actor and a fire method name.

 Copyright (c) 1998-2011 The Regents of the University of California.
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
package ptolemy.domains.sequence.kernel;

import ptolemy.actor.sched.Firing;

///////////////////////////////////////////////////////////////////
//// SequenceFiring

/** A schedule element specifically for the SequenceDirector
 *  and ProcessDirector that can optionally contain a reference
 *  to a specific fire method for an actor.
 */
public class SequenceFiring extends Firing {

    /** Construct a firing with a default iteration count equal to one
     *  and with no parent schedule.
     */
    public SequenceFiring() {
        super();
        _methodName = null;
    }

    /** Get the method name to be executed when
     *  the actor fires, or null if there is none.
     *
     *  @return The method name, or null.
     *  @see #setMethodName(String)
     */
    public String getMethodName() {
        return _methodName;
    }

    /** Set the method name to be executed when the actor fires.
     *
     *  @param methodName The specified methodName.
     *  @see #getMethodName()
     */
    public void setMethodName(String methodName) {
        _methodName = methodName;
    }

    /** The method name to be executed when the actor fires.
     *  This is used for actors that have multiple fire methods.
     */
    private String _methodName = null;
}
