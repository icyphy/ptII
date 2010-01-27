/* A listener for FiringEvents from an Actor.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.actor;

//////////////////////////////////////////////////////////////////////////
//// ActorFiringListener

/**
 A FiringEventListener listens for events that are issued during
 AtomicActor's iterations. Specifically before and after fire events.
 The implementation collects firing events during workflow execution.
 <p>
 Currently this class is being used in Kepler by the
 ProvenanceExecutionListener for saving actor's firings with provenance
 information.  In the future, listeners will use this interface
 to initiate "smart" reruns or for fault tolerance.

 @author Efrat Frank
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (jaeger)
 @Pt.AcceptedRating
 @see AtomicActor
 */
public interface ActorFiringListener {
    /** Report an actor firing state.  This method will be called
     *  when an begins/finishes firing iteration.
     *  @param event The event to report.
     */
    public void firingEvent(FiringEvent event);

}
