/* For testing the workspace synchronization features.

 Copyright (c) 2003-2005 The Regents of the University of California.
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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// TestWorkspaceBase

/**
 A base class for creating tests on the workspace synchronization features.
 A derived test creates a list of threads that access the workspace in
 various ways.

 @author Xiaojun Liu
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Red (cxh)

 */
public abstract class TestWorkspaceBase {
    /** Create threads that access a workspace in various ways.
     *  List the threads in _accessThreads.
     */
    public abstract void initializeTest();

    /** Initialize and run the test.
     */
    public void runTest() {
        initializeTest();

        Iterator threads = _accessThreads.iterator();

        while (threads.hasNext()) {
            Thread thread = (Thread) threads.next();
            thread.start();
        }

        try {
            Thread.sleep(_testTime);
        } catch (InterruptedException ex) {
            _profile = "Test interrupted\n";
        }

        threads = _accessThreads.iterator();

        while (threads.hasNext()) {
            Thread thread = (Thread) threads.next();
            thread.interrupt();

            try {
                thread.join();
            } catch (InterruptedException ex) {
                _profile += ("Test thread " + thread.getName() + " interrupted\n");
            }
        }

        if (_profile == null) {
            StringBuffer buf = new StringBuffer();
            Iterator records = _record.iterator();

            while (records.hasNext()) {
                buf.append((String) records.next());
                buf.append("\n");
            }

            _profile = buf.toString();
        }

        //System.out.println(_profile);
    }

    public String profile() {
        return _profile;
    }

    private String _profile;

    protected List _accessThreads = new LinkedList();

    protected List _record = Collections.synchronizedList(new LinkedList());

    protected long _testTime = 0;
}
