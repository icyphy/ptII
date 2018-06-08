/* ConstraintSolver is an interface used to let the subclass schedule Metro events.

 Copyright (c) 2012-2014 The Regents of the University of California.
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

package ptolemy.domains.metroII.kernel;

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;

///////////////////////////////////////////////////////////////////
//// ConstraintSolver

/**
 * ConstraintSolver is an interface used to let the subclass schedule MetroII
 * events. The scheduling is via updating the MetroII events passed to resolve()
 * method. An MetroII event status is updated to NOTIFIED when it satisfies the
 * constraints in the subclass. Otherwise the event status should be updated to
 * WAITING.
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public interface ConstraintSolver {
    /**
     * Update the MetroII events in the specified list.
     *
     * @param metroIIEventList
     *            a list of MetroII events to be updated.
     */
    public void resolve(Iterable<Event.Builder> metroIIEventList);
}
