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
//// TestWorkspace5

/**
 Test the following scenario: thread T1 gets read access; T2 waits
 for write access and is interrupted, then waits for write access again;
 T1 releases read access; T2 gets/releases write access; another thread
 T3 gets read access; T2 handles failure (being interrupted) in getting
 write access.

 @author Xiaojun Liu
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Red (cxh)

 */
public class TestWorkspace5 extends TestWorkspaceBase {
    @Override
    public void initializeTest() {
        Workspace workspace = new Workspace();
        List actions = new LinkedList();
        AccessAction action = new AccessAction(workspace, 0, 'R', 1000, null,
                _record, "A0");
        actions.add(action);
        _accessThreads.add(new AccessThread("T1", actions, this));
        actions = new LinkedList();
        action = new AccessAction(workspace, 500, 'W', 500, null, _record, "A2");
        actions.add(action);
        action = new AccessAction(workspace, 0, 'W', 0, null, _record, "A3");
        actions.add(action);
        _thread = new AccessThread("T2", actions, this);
        _accessThreads.add(_thread);
        actions = new LinkedList();
        action = new AccessAction(workspace, 2200, 'R', 100, null, _record,
                "A1");
        actions.add(action);
        _accessThreads.add(new AccessThread("T3", actions, this));
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
