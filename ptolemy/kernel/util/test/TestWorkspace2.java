/* For testing the workspace synchronization features.

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
package ptolemy.kernel.util.test;

import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TestWorkspace2

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
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Red (cxh)

 */
public class TestWorkspace2 extends Thread {
    public static void main(String[] args) {
        Workspace w = new Workspace("test");
        TestWorkspace2 tw = new TestWorkspace2("test", w);
        tw.start();
        System.out.println(tw.profile);
    }

    public TestWorkspace2(String name, Workspace workspace) {
        _name = name;
        _workspace = workspace;
        _notif = new Notification(_name + ".notif");
    }

    /** Start a thread for an instance of the inner class "Notification",
     *  obtain read access on the workspace 3 times, call wait(obj) on the
     *  workspace, ask the inner class to get a write access on the workspace
     *  and return after relinquishing the read accesses on the workspace.
     *  This method is synchronized both on this class and the inner class
     */
    @Override
    public synchronized void run() {
        _notif.start();

        int i = 0;

        try {
            for (i = 0; i < 3; i++) {
                _workspace.getReadAccess();
                profile += _name + ".getReadAccess()\n";

                try {
                    // FindBugs:
                    // [M M SWL] Method calls Thread.sleep() with a lock held [SWL_SLEEP_WITH_LOCK_HELD]
                    // In this test program however this is not a problem.
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }

            // NOTE: we must synchronize with _notif to not miss notifications
            // (we make sure that the other thread only starts once we have
            // called wait.
            synchronized (_notif) {
                _notif.getWriteAccess = true;

                // Remark:
                //      The while (_notif.getWriteAccess) is not
                //      necessary in the current implementation (with
                //      only two threads using _notif), but makes it
                //      more robust when changes are made.
                //      This problem was detected by Findbugs:
                //        UW: Unconditional wait (UW_UNCOND_WAIT)
                while (_notif.getWriteAccess) {
                    try {
                        _workspace.wait(_notif);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                _notif.done = true;
            }
        } finally {
            for (int j = i; j > 0; j--) {
                _workspace.doneReading();
                profile += _name + ".doneReading()\n";
            }
        }
    }

    /** Return a profile which contains the various actions performed by this
     *  object.
     */
    public synchronized String profile() {
        return profile;
    }

    public Workspace _workspace;

    public String profile = "";

    private Notification _notif;

    private String _name;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    /** Repeatedly calls notifyAll on itself to wake up any threads waiting
     *  on it.
     */
    public class Notification extends Thread {
        public Notification(String name) {
            _name = name;
        }

        @Override
        public void run() {
            while (!done) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }

                synchronized (this) {
                    if (getWriteAccess) {
                        try {
                            TestWorkspace2.this._workspace.getWriteAccess();
                            TestWorkspace2.this.profile += _name
                                    + ".getWriteAccess()\n";
                        } finally {
                            _workspace.doneWriting();
                            TestWorkspace2.this.profile += _name
                                    + ".doneWriting()\n";
                        }

                        getWriteAccess = false;
                        notifyAll();
                    }
                }
            }
        }

        public boolean done = false;

        public boolean getWriteAccess = false;

        public String _name;
    }
}
