/* For testing the workspace synchronization features.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)

*/

package ptolemy.kernel.util.test;

import java.io.Serializable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
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

*/
public class TestWorkspace2 extends Thread {

    public TestWorkspace2(String name, Workspace ws) {
        _name = name;
        _workspace = ws;
        _notif = new Notification(_name + ".notif");
    }

    /** Start a thread for an instance of the inner class "Notification",
     *  obtain read access on the workspace 3 times, call wait(obj) on the
     *  workspace, ask the inner class to get a write access on the workspace
     *  and return after relinquishing the read accesses on the workspace.
     *  This method is synchronized both on this class and the inner class
     */
    public synchronized void run() {
        _notif.start();
        int i = 0;
        try {
            for (i = 0; i < 3; i++) {
                _workspace.getReadAccess();
                profile += _name + ".getReadAccess()\n";
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {}
            }
            synchronized(_notif) {
                _notif.getwriteaccess = true;
                _workspace.wait(_notif);
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

        public void run () {
            while (!done) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {}
                synchronized(this) {
                    if (getwriteaccess) {
                        try {
                            TestWorkspace2.this._workspace.getWriteAccess();
                            TestWorkspace2.this.profile +=
                                _name + ".getWriteAccess()\n";
                        } finally {
                            _workspace.doneWriting();
                            TestWorkspace2.this.profile +=
                                _name + ".doneWriting()\n";
                        }
                        getwriteaccess = false;
                        notifyAll();
                    }
                }
            }
        }
        public boolean done = false;
        public boolean getwriteaccess = false;
        public String _name;
    }
}
