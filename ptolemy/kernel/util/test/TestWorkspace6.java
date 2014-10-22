/* For testing the workspace synchronization features.

 Copyright (c) 2003-2014 The Regents of the University of California.
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

import java.util.LinkedList;
import java.util.List;

import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// TestWorkspace6

/**
 Test the following scenario: thread T1 gets read access twice, then waits
 on a lock object (and in doing so, releases the read access); T2 gets/releases
 write access; T1 is interrupted, and reacquires read access.

 @author Xiaojun Liu
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Red (cxh)

 */
public class TestWorkspace6 extends TestWorkspaceBase {
    public static void main(String[] args) {
        TestWorkspace6 tw = new TestWorkspace6();
        tw.runTest();
    }

    @Override
    public void initializeTest() {
        Workspace workspace = new Workspace();
        List actions = new LinkedList();
        AccessAction action = new AccessAction(workspace, 0, 'R', 0, null,
                _record, "A0");
        actions.add(action);
        action = new AccessAction(workspace, 0, 'R', 0, null, _record, "A1");
        actions.add(action);
        action = new AccessAction(workspace, 0, 'U', 0, new Object(), _record,
                "A2");
        actions.add(action);
        _thread = new AccessThread("T1", actions, this);
        _accessThreads.add(_thread);
        actions = new LinkedList();
        action = new AccessAction(workspace, 500, 'W', 500, null, _record, "A3");
        actions.add(action);
        _accessThreads.add(new AccessThread("T2", actions, this));
        _testTime = 5000; // ms
    }

    @Override
    public void runTest() {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }

                // interrupt T2 while it is waiting for read access
                _thread.interrupt();
            }
        }.start();
        super.runTest();
    }

    private Thread _thread;
}
