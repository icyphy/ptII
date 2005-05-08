/* Record for checkpoint objects.

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

import java.util.Iterator;
import java.util.Stack;

import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;

//////////////////////////////////////////////////////////////////////////
//// CheckpointRecord
/**
   Record for checkpoint objects. Each {@link Rollbackable} is monitored by a
   checkpoint object at a time. The {@link Rollbackable} object maintains a
   record for its checkpoint objects. When a new checkpoint object is assigned,
   the old one is pushed to the checkpoint record. When a rollback is issued
   beyond the time when the last checkpoint object was assigned, the previous
   checkpoint object is then assigned back to the {@link Rollbackable}.
   <p>
   This record for checkpoint objects is basically a stack, with the top
   element as the last assigned checkpoint object (the checkpoint object that
   currently monitors a {@link Rollbackable}).

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public class CheckpointRecord {

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Add a checkpoint object to the top of this record, associating with it
     *  a timestamp. The timestamp is usually the current timestamp of the new
     *  checkpoint object.
     *  <p>
     *  When a rollback beyond this timestamp is issued later, the previous
     *  checkpoint object is restored.
     * 
     *  @param checkpoint The checkpoint object to be push to the top of this
     *   record.
     *  @param timestamp The current timestamp of the new checkpoint object.
     */
    public void add(Checkpoint checkpoint, long timestamp) {
        _records.push(new Record(checkpoint, timestamp));
    }

    /** Commit the changes of checkpoint objects up to (but not including) the
     *  time given by the timestamp. Older records of checkpoint object
     *  assignments are deleted. After that, it is not possible to roll back to
     *  any time before the given timestamp.
     * 
     *  @param timestamp The timestamp specifying the time up to when the
     *   changes of checkpoint objects are committed.
     */
    public void commit(long timestamp) {
        Iterator recordsIter = _records.iterator();
        while (recordsIter.hasNext()) {
            Record record = (Record)recordsIter.next();
            if (record.getTimestamp() < timestamp)
                recordsIter.remove();
        }
        
        // Previous design:
        // delete all the records only if the given timestamp is bigger than
        // the that associated with the last checkpoint.
        //if (timestamp > getTopTimestamp())
        //    _records.clear();
    }

    /** Get the timestamp associated with the checkpoint object on top of the
     *  stack.
     * 
     *  @return The timestamp associated with the top checkpoint object.
     */
    public long getTopTimestamp() {
        if (_records.isEmpty())
            return -1;
        else
            return ((Record)_records.peek()).getTimestamp();
    }
    
    /** Restore the old checkpoint object to the given {@link Rollbackable}
     *  object, if the given timestamp is less than or equal to the timestamp
     *  associated with the current checkpoint object.
     *  <p>
     *  This method is not meant to be called by user applications.
     * 
     *  @param checkpoint The checkpoint object of the {@link Rollbackable}
     *   object.
     *  @param object The {@link Rollbackable} object.
     *  @param timestamp The timestamp.
     *  @param trim Whether to delete the used checkpoint object record.
     *  @return The checkpoint object of the {@link Rollbackable} object after
     *   it is restored.
     */
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

    ///////////////////////////////////////////////////////////////////
    ////                        nested class                       ////

    //////////////////////////////////////////////////////////////////////////
    //// Record
    /**
       The record of a checkpoint object associated with a timestamp to be
       stored in the stack.
    
       @author Thomas Feng
       @version $Id$
       @since Ptolemy II 5.1
       @Pt.ProposedRating Red (tfeng)
       @Pt.AcceptedRating Red (tfeng)
    */
    private class Record {
        
        /** Construct a record with a checkpoint object and the timestamp
         *  associated with it.
         * 
         *  @param checkpoint The checkpoint object.
         *  @param timestamp The timestamp.
         */
        Record(Checkpoint checkpoint, long timestamp) {
            _checkpoint = checkpoint;
            _timestamp = timestamp;
        }

        /** Get the checkpoint object.
         * 
         *  @return The checkpoint object.
         */
        Checkpoint getCheckpoint() {
            return _checkpoint;
        }

        /** Get the timestamp.
         * 
         *  @return The timestamp.
         */
        long getTimestamp() {
            return _timestamp;
        }

        /** The checkpoint object.
         */
        private Checkpoint _checkpoint;

        /** The timestamp.
         */
        private long _timestamp;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The stack of records. Each record is an instance of {@link Record}.
     */
    private Stack _records = new Stack();
}
