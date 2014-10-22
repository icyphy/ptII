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

//////////////////////////////////////////////////////////////////////////
//// TestWorkspace

/**
 This object implements a thread that obtains read permission to
 a workspace three times sequentially, then obtains write permission.
 To use it, create an instance and then call its start() method.
 To obtain a profile of what it did, call its profile() method.
 That will return only after the thread completes.
 NOTE: This is a very primitive test.  It does not check very much.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Red (cxh)

 */
public class TestWorkspace extends Thread {
    public static void main(String[] args) {
        Workspace w = new Workspace("test");
        TestWorkspace tw = new TestWorkspace("test", w);
        tw.start();
        System.out.println(tw._profile);
    }

    public TestWorkspace(String name, Workspace workspace) {
        _name = name;
        _workspace = workspace;
    }

    @Override
    public synchronized void run() {
        for (int i = 0; i < 3; i++) {
            try {
                _workspace.getReadAccess();
                _profile += _name + ".getReadAccess()\n";

                try {
                    // FindBugs:
                    // [M M SWL] Method calls Thread.sleep() with a lock held [SWL_SLEEP_WITH_LOCK_HELD]
                    // In this test program however this is not a problem.
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            } finally {
                _workspace.doneReading();
                _profile += _name + ".doneReading()\n";
            }
        }

        try {
            _workspace.getWriteAccess();
            _profile += _name + ".getWriteAccess()\n";

            try {
                // FindBugs:
                // [M M SWL] Method calls Thread.sleep() with a lock held [SWL_SLEEP_WITH_LOCK_HELD]
                // In this test program however this is not a problem.
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        } finally {
            _workspace.doneWriting();
            _profile += _name + ".doneWriting()\n";
        }
    }

    public synchronized String profile() {
        return _profile;
    }

    private String _name;

    private Workspace _workspace;

    private String _profile = "";
}
