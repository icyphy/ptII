/*

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

package ptolemy.backtrack.util;

import java.util.Stack;

import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;

//////////////////////////////////////////////////////////////////////////
//// CheckpointRecord
/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class CheckpointRecord {

    public void add(Checkpoint checkpoint, long timestamp) {
        _records.push(new Record(checkpoint, timestamp));
    }

    public void commit(long timestamp) {
        if (timestamp > getTopTimestamp())
            _records.clear();
    }

    public long getTopTimestamp() {
        if (_records.isEmpty())
            return -1;
        else
            return ((Record)_records.peek()).getTimestamp();
    }

    public Checkpoint restore(Checkpoint checkpoint, Rollbackable object,
            long timestamp, boolean trim) {
        if (_records.isEmpty())
            return checkpoint;
        else {
            Record topRecord = (Record)_records.peek();
            long topTimestamp = topRecord.getTimestamp();
            if (timestamp <= topTimestamp) {
                Checkpoint oldCheckpoint = topRecord.getCheckpoint();
                if (checkpoint != null)
                    checkpoint.removeObject(object);
                if (oldCheckpoint != null)
                    oldCheckpoint.addObject(object);
                if (trim)
                    _records.pop();
                return oldCheckpoint;
            } else
                return checkpoint;
        }
    }

    private class Record {
        Record(Checkpoint checkpoint, long timestamp) {
            _checkpoint = checkpoint;
            _timestamp = timestamp;
        }

        Checkpoint getCheckpoint() {
            return _checkpoint;
        }

        long getTimestamp() {
            return _timestamp;
        }

        private Checkpoint _checkpoint;

        private long _timestamp;
    }

    private Stack _records = new Stack();
}
