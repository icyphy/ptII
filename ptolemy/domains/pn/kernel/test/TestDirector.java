/* For testing the various methods of PNDirector.

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
package ptolemy.domains.pn.kernel.test;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.data.IntToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// TestDirector

/**
 This object implements a thread that obtains read permission to
 a workspace three times sequentially, then calls workspace.wait(obj) on an
 object and exits. The object "obj" on which the wait method is called is an
 inner class of TestWorkspace2 and has a thread of its own. This thread gets a
 write access on the workspace, after the TestWorkspace2 object calls wait(obj)
 on it. Then it gives up the write access and returns.
 To use it, create an instance and then call its start() method.
 To obtain a profile of what it did, call its profile() method.
 That will return only after the thread completes.
 NOTE: This is a very primitive test.  It does not check very much.

 @author Mudit Goel, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Green (mudit)
 @Pt.AcceptedRating Red (mudit)

 */
public class TestDirector extends AtomicActor {
    public TestDirector(CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input = new IOPort(this, "input", true, false);
        output = new IOPort(this, "output", false, true);
    }

    /** Clear the profile accumulated till now.
     */
    public void clearProfile() {
        profile = "";
    }

    /** Start a thread for an instance of the inner class "Notification",
     *  obtain read access on the workspace 3 times, call wait(obj) on the
     *  workspace, ask the inner class to get a write access on the workspace
     *  and return after relinquishing the read accesses on the workspace.
     *  This method is synchronized both on this class and the inner class
     */
    @Override
    public synchronized void fire() throws IllegalActionException {
        int i = 0;

        for (i = 0; i < 2; i++) {
            output.broadcast(new IntToken(i));
            profile += "broadcast new token " + i + "\n";
        }

        for (i = 0; i < 2; i++) {
            int ans = ((IntToken) input.get(0)).intValue();
            profile += "received new token " + ans + "\n";
        }

        try {
            ((CompositeActor) getContainer()).workspace().getReadAccess();

            // an actor should not call director.wrapup()
            //((PNDirector)getDirector()).wrapup();
        } finally {
            ((CompositeActor) getContainer()).workspace().doneReading();
        }

        output.broadcast(new IntToken(i));
        profile += "broadcast new token " + i + "\n";
    }

    /** Return a profile which contains the various actions performed by this
     *  object.
     */
    public synchronized String getProfile() {
        return profile;
    }

    @Override
    public boolean postfire() {
        return false;
    }

    public IOPort input;

    public IOPort output;

    public String profile = "";
}
