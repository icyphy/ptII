/* An action to access a workspace.

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

 @ProposedRating Green (eal)

 */
package ptolemy.kernel.util.test;

import java.util.List;

import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Workspace;

public class AccessAction {
    public AccessAction(Workspace workspace, long sleepBefore, char action,
            long sleepAfter, Object lock, List record, String name) {
        _workspace = workspace;
        _sleepBefore = sleepBefore;
        _action = action;
        _sleepAfter = sleepAfter;
        _lock = lock;
        _record = record;
        _name = name;
    }

    public void access() throws InterruptedException {
        if (_sleepBefore > 0) {
            Thread.sleep(_sleepBefore);
        }

        switch (_action) {
        case 'R':

            synchronized (_workspace) {
                try {
                    _workspace.getReadAccess();
                    _record.add(_name + " got read access");
                } catch (InternalErrorException ex) {
                    _failed = true;
                    _record.add(_name + " failed to get read access");
                }
            }

            break;

        case 'W':

            synchronized (_workspace) {
                try {
                    _workspace.getWriteAccess();
                    _record.add(_name + " got write access");
                } catch (InternalErrorException ex) {
                    _failed = true;
                    _record.add(_name + " failed to get write access");
                }
            }

            break;

        case 'U':

            //synchronized (_workspace) {
            try {
                _record.add(_name + " entered waiting on lock");
                _workspace.wait(_lock);
                _record.add(_name + " woke up from waiting");
            } catch (InterruptedException ex) {
                _record.add(_name + " interrupted while waiting");
            }

            //}
            break;

        default:

            // no-op
            break;
        }

        if (_sleepAfter > 0) {
            Thread.sleep(_sleepAfter);
        }
    }

    public void deAccess() throws InterruptedException {
        if (_sleepAfter > 0) {
            Thread.sleep(_sleepAfter);
        }

        switch (_action) {
        case 'R':

            synchronized (_workspace) {
                _workspace.doneReading();

                if (_failed) {
                    _record.add(_name
                            + " handled failure in getting read access");
                } else {
                    _record.add(_name + " released read access");
                }
            }

            break;

        case 'W':

            synchronized (_workspace) {
                _workspace.doneWriting();

                if (_failed) {
                    _record.add(_name
                            + " handled failure in getting write access");
                } else {
                    _record.add(_name + " released write access");
                }
            }

            break;

        case 'U':

            //_workspace.wait(_lock);
            // no-op
            break;

        default:

            // no-op
            break;
        }

        if (_sleepBefore > 0) {
            Thread.sleep(_sleepBefore);
        }
    }

    private Workspace _workspace;

    private long _sleepBefore;

    private long _sleepAfter;

    private char _action;

    private Object _lock;

    private List _record;

    private String _name;

    private boolean _failed = false;
}
