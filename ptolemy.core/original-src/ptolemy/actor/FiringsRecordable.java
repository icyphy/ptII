/* Interface for entities that can report FiringEvents.

Copyright (c) 2007-2014 The Regents of the University of California.
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

 */

package ptolemy.actor;

/** Interface for entities that can report FiringEvents.
 *
 * @author Daniel Crawl
 * @version $Id$
 * @since Ptolemy II 7.0
 * @Pt.ProposedRating Green (neuendor)
 * @Pt.AcceptedRating Yellow (neuendor)
 */
public interface FiringsRecordable {
    // FIXME: These methods should be added to Executable interface
    // instead of being separate.

    /** Append a listener to the current set of firing event listeners.
     *  @param listener The listener to be appended.
     *  @see #removeActorFiringListener(ActorFiringListener)
     */
    public void addActorFiringListener(ActorFiringListener listener);

    /** Unregister a firing event listener.
     *  @param listener The listener to be removed
     *  @see #addActorFiringListener(ActorFiringListener)
     */
    public void removeActorFiringListener(ActorFiringListener listener);

    /** Record a firing event.
     *  @param type The type of firing event to record.
     */
    public void recordFiring(FiringEvent.FiringEventType type);
}
