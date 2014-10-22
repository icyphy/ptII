/* A thread that performs a series of access actions on a workspace.

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

 @ProposedRating Green (eal)

 */
package ptolemy.kernel.util.test;

import java.util.List;

import ptolemy.kernel.util.InvalidStateException;

public class AccessThread extends Thread {
    public AccessThread(String name, List actions, TestWorkspaceBase test) {
        super(name);
        _actions = actions;
        _test = test;
    }

    @Override
    public void run() {
        try {
            getAccess(0);
        } catch (InvalidStateException ex) {
            _test._record.add(ex.getMessage());
        }
    }

    public void getAccess(int level) {
        if (level >= _actions.size()) {
            return;
        }

        AccessAction action = (AccessAction) _actions.get(level);
        ;

        try {
            action.access();
            getAccess(level + 1);
        } catch (InterruptedException e) {
            // ignore
        } finally {
            try {
                action.deAccess();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private List _actions;

    private TestWorkspaceBase _test;
}
