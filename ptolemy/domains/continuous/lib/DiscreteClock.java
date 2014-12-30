/* Generate discrete events at prespecified time instants.

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
package ptolemy.domains.continuous.lib;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// DiscreteClock

/**
 Produce a periodic signal, a sequence of events at regularly spaced
 intervals.

 <p>This actor is identical to {@link ptolemy.actor.lib.DiscreteClock},
 except that for backward compatibility the default values of the parameters
 are changed to</p>
 <ul>
 <li> period: 2.0</li>
 <li> offsets: {0.0, 1.0}</li>
 <li> values {1, 0}</li>
 </ul>

 @author Haiyang Zheng and Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Yellow (hyzheng)
 @deprecated Use ptolemy.actor.lib.DiscreteClock instead.
 */
@Deprecated
public class DiscreteClock extends ptolemy.actor.lib.DiscreteClock {

    // This actor only generates predictable events and that is why it does not
    // implement the ContinuousStepSizeControlActor interface. This actor requests a
    // refiring at its initialize method to produce events. During its postfire
    // method, it requests further firings to produce more events if necessary.

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public DiscreteClock(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        period.setExpression("2.0");
        offsets.setExpression("{0.0, 1.0}");
        values.setExpression("{1, 0}");
    }
}
