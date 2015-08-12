/* Base class for sequence-based sources.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SequenceSource

/**
 Base class for sequence sources.  A sequence source is
 a source where the output value is logically a sequence, independent
 of time, but dependent on the iteration number.  For some time-based
 domains, such as CT, actors of this type probably do not make sense
 because the number of iterations that the actor experiences per unit
 time is not easily determined or controlled.  This actor has a parameter,
 <i>firingCountLimit</i>, that optionally limits the number of iterations
 for which the actor is fired.  If this number is <i>n</i> &gt; 0, then
 the <i>n</i>-th invocation of postfire() returns false, which ggindicates
 to the scheduler that it should stop invocations of this actor.
 The default value of <i>firingCountLimit</i>
 is NONE, which results in postfire always returning
 true.  Derived classes must call super.postfire() for this mechanism to
 work.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (bilung)
 */
public class SequenceSource extends LimitedFiringSource implements
SequenceActor {
    /** Construct an actor with the given container and name.
     *  The <i>firingCountLimit</i> parameter is also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SequenceSource(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        // NOTE: This actor only adds implementing the
        // marker interface SequenceActor to its base class.
        super(container, name);
    }
}
