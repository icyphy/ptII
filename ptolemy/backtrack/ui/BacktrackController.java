/* Backtracking controller for composite actors.

 Copyright (c) 2005-2013 The Regents of the University of California.
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

package ptolemy.backtrack.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;

///////////////////////////////////////////////////////////////////
//// BacktrackController
/**
 Backtracking controller for composite actors. This controller is built on top
 of the backtracking sub-system. It helps to create checkpoints for Ptolemy II
 composite actors. Each of those checkpoints may correspond to multiple
 checkpoints in the backtracking sub-system, due to the fact that entities in
 a composite actor may be monitored by different checkpoint objects. This
 controller also allows to roll back the composite actor to its previous
 state.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class BacktrackController {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Commit a checkpoint handle. After it is committed, the system can no
     *  longer roll back to that checkpoint, or any other checkpoint taken
     *  before that one. Memory allocated for the committed checkpoints will be
     *  released.
     *
     *  @param handle The handle of the checkpoint to be committed.
     */
    public void commit(long handle) {
        Iterator<Long> handles = _checkpoints.keySet().iterator();
        while (handles.hasNext()) {
            Long handleKey = handles.next();
            if (handleKey.longValue() <= handle) {
                HashMap<Checkpoint, Long> checkpointsAndHandles = _checkpoints
                        .get(handleKey);
                for (Checkpoint checkpoint : checkpointsAndHandles.keySet()) {
                    long timestamp = checkpointsAndHandles.get(checkpoint)
                            .longValue();
                    checkpoint.commit(timestamp);
                }
            }
            handles.remove();
        }
    }

    /** Create a checkpoint for the given composite actor, and return the
     *  checkpoint handle.
     *
     *  This checkpoint handle is different from the checkpoint handles returned
     *  by {@link Checkpoint#createCheckpoint()}, which are the internal
     *  representations of checkpoint timestamps. This checkpoint handle,
     *  however, refers to a collection of checkpoint timestamps for different
     *  checkpoint objects that manage different entities in the container.
     *
     *  The returned checkpoint handle can only be used with {@link
     *  #commit(long)} and {@link #rollback(long, boolean)} in this class.
     *
     *  @param container The composite actor for which a checkpoint will be
     *   created.
     *  @return The checkpoint handle.
     */
    public synchronized long createCheckpoint(CompositeActor container) {
        HashMap<Checkpoint, Long> checkpointsAndHandles = new HashMap<Checkpoint, Long>();
        Set<Checkpoint> checkpoints = new HashSet<Checkpoint>();
        Iterator<?> objectsIterator = container.containedObjectsIterator();
        while (objectsIterator.hasNext()) {
            Object object = objectsIterator.next();
            if (object instanceof Rollbackable) {
                Rollbackable rollbackObject = (Rollbackable) object;
                Checkpoint checkpoint = rollbackObject.$GET$CHECKPOINT();
                if (!checkpoints.contains(checkpoint)) {
                    long timestamp = checkpoint.createCheckpoint();
                    checkpointsAndHandles.put(checkpoint, new Long(timestamp));
                    checkpoints.add(checkpoint);
                }
            }
        }
        _checkpoints.put(new Long(_currentHandle), checkpointsAndHandles);
        return _currentHandle++;
    }

    /** Roll back the system state with the records in the checkpoint with the
     *  given handle.
     *
     *  If the checkpoint objects that monitor the entities in the composite
     *  actor are not changed since the time when the checkpoint was created,
     *  this rollback only affects that composite actor and its children. This
     *  is true in most of the cases, but it is not guaranteed.
     *
     *  @param handle The checkpoint handle previous returned by {@link
     *   #createCheckpoint(CompositeActor)}.
     *  @param trim Whether the records in the checkpoint should be deleted. If
     *   the records are deleted, memory is freed, but lazy computation is
     *   impossible.
     *  @return Whether the given handle is valid.
     */
    public boolean rollback(long handle, boolean trim) {
        HashMap<Checkpoint, Long> checkpointsAndHandles = _checkpoints
                .get(new Long(handle));
        if (checkpointsAndHandles == null) {
            return false;
        } else {
            for (Checkpoint checkpoint : checkpointsAndHandles.keySet()) {
                long timestamp = (checkpointsAndHandles.get(checkpoint))
                        .longValue();
                checkpoint.rollback(timestamp, trim);
            }
            return true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The map from checkpoint handles used in this class to checkpoint handles
     *  used in the checkpointing sub-system. The keys of this map are handles
     *  (of Long type); the values are HashMaps. In each of those HashMaps, the
     *  keys are checkpoint objects, and the values are the checkpoint handles
     *  returned by those checkpoint objects.
     */
    private HashMap<Long, HashMap<Checkpoint, Long>> _checkpoints = new HashMap<Long, HashMap<Checkpoint, Long>>();

    /** The current checkpoint handle.
     */
    private long _currentHandle = 0;
}
