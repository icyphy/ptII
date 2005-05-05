/* The class of checkpoint objects.

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

package ptolemy.backtrack;

import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// CheckPoint
/**
   The class of checkpoint objects. A checkpoint object represents the smallest
   entity on which checkpoints are created and managed. It monitors one or more
   objects. When the {@link #rollback(long, boolean)} function is called, all
   the monitored objects are rolled back to their previous states, defined by a
   <em>timestamp</em>.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public class Checkpoint {

    ///////////////////////////////////////////////////////////////////
    ////                        constructors                       ////

    /** Construct a checkpoint object with an initial object in its monitored
     *  object list.
     *
     *  @param object The first object to be placed in the list, or
     *   <tt>null</tt> if the list is intended to be empty.
     */
    public Checkpoint(Rollbackable object) {
        if (object != null)
            addObject(object);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Add an object to the monitored object list.
     *
     *  @param object The object to be added.
     */
    public void addObject(Rollbackable object) {
        _state.getMonitoredObjects().add(object);
    }

    /** Commit the changes on all the monitored objects up to the given
     *  timestamp. Records of changes with timestamps less than the given
     *  timestamp are deleted.
     *
     *  @param timestamp The timestamp.
     */
    public synchronized void commit(long timestamp) {
        Iterator objectsIter = _state.getMonitoredObjects().iterator();
        while (objectsIter.hasNext())
            ((Rollbackable)objectsIter.next()).$COMMIT(timestamp);
    }

    /** Create a new checkpoint and return its handle. The current timestamp is
     *  increased by one before the it (as a handle) is returned.
     *
     *  @return The handle of the newly created checkpoint.
     */
    public synchronized long createCheckpoint() {
        return _state.createCheckpoint();
    }

    /** Get the current timestamp (also considered as the last created handle).
     *
     *  @return The current timestamp.
     */
    public synchronized long getTimestamp() {
        return _state.getTimestamp();
    }

    /** Test if the checkpointing facility is running.
     *
     *  @return <tt>true</tt> if the checkpointing facility is running.
     */
    public boolean isCheckpointing() {
        return _state != null;
    }

    /** Remove an object from the monitored object list.
     *
     *  @param object The object to be removed.
     */
    public void removeObject(Rollbackable object) {
        Iterator objectsIter = _state.getMonitoredObjects().iterator();
        while (objectsIter.hasNext())
            if (objectsIter.next() == object)
                objectsIter.remove();
    }

    /** Rollback all the monitored objects to their previous states defined by
     *  the given timestamp (or, handle). The records used in the rollback are
     *  automatically deleted. This is the same as <tt>rollback(timestamp,
     *  true)</tt>.
     *
     *  @param timestamp The timestamp taken at a previous time.
     *  @see #rollback(long, boolean)
     */
    public synchronized void rollback(long timestamp) {
        rollback(timestamp, true);
    }

    /** Rollback all the monitored objects to their previous states defined by
     *  the given timestamp (or, handle).
     *
     *  @param timestamp The timestamp taken at a previous time.
     *  @param trim Whether to delete the records used for the rollback.
     */
    public synchronized void rollback(long timestamp, boolean trim) {
        List objects = _state.getMonitoredObjects();
        int size = objects.size();
        for (int i = 0; i < objects.size();) {
            Rollbackable object = (Rollbackable)objects.get(i);
            object.$RESTORE(timestamp, trim);
            int newSize = objects.size();
            if (newSize < size)
                size = newSize;
            else
                i++;
        }
    }

    /** Set this checkpoint object to be the same as the given checkpoint
     *  object. The set of objects monitored by this checkpoint object is
     *  merged with those monitored by the given checkpoint.
     *
     *  @param checkpoint The given checkpoint object to be merged with this
     *   one.
     */
    public void setCheckpoint(Checkpoint checkpoint) {
        List objects = _state.getMonitoredObjects();
        while (objects.size() > 0) {
            Rollbackable object = (Rollbackable)objects.remove(0);
            object.$SET$CHECKPOINT(checkpoint);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The current state of the checkpoint object.
     */
    private CheckpointState _state = new CheckpointState();
}
