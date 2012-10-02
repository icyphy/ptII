/* Utilities for DDE LocalZeno demonstration

 Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.domains.dde.demo.LocalZeno;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// LocalZenoUtilities

/**
 *  Utilities for DDE LocalZeno demonstration.
 *
 *  @author Christopher Brooks
 *  @since Ptolemy II 4.1
 *  @Pt.ProposedRating Yellow (davisj)
 *  @Pt.AcceptedRating Red (davisj)
 */
public class LocalZenoUtilities {
    /** Instances of this class cannot be created.
     */
    private LocalZenoUtilities() {
    }

    /** Sleep the current thread for 100 ms.  If the Thread.sleep()
     *  call gets interrupted, and ProcessDirector.stop() was called,
     *  then we throw a TerminateProcessException, which stops all
     *  the other Processes.  If Thread.sleep() gets interrupted and
     *  ProcessDirector.stop() was not called, then we throw an
     *  IllegalActionException.
     *  <p>This method is used to slow down execution for the purposes
     *  of illustration.
     *  @param actor The actor used for checking whether stop() was called.
     *  @exception IllegalActionException If the Thread.sleep() call gets
     *  interrupted and stop() was not called on the director.
     */
    public static void sleepProcess(TypedAtomicActor actor)
            throws IllegalActionException {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            if (((ProcessDirector) actor.getDirector()).isStopFireRequested()) {
                throw new TerminateProcessException(actor, ex.getMessage());

                // Ignore
            } else {
                throw new IllegalActionException(actor, ex,
                        "InterruptedException during a sleeping thread.");
            }
        }
    }
}
