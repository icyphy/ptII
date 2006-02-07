/* 

Copyright (c) 2005-2006 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// BacktrackController
/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class BacktrackController {
    public synchronized long createCheckpoint(CompositeActor container) {
        HashMap checkpointsAndHandles = new HashMap();
        Iterator objectsIterator = container.containedObjectsIterator();
        Set checkpoints = new HashSet();
        while (objectsIterator.hasNext()) {
            Object object = objectsIterator.next();
            if (object instanceof Rollbackable) {
                Rollbackable rollbackObject = (Rollbackable)object;
                Checkpoint checkpoint = rollbackObject.$GET$CHECKPOINT();
                if (!checkpoints.contains(checkpoint)) {
                    long timestamp = checkpoint.createCheckpoint();
                    checkpointsAndHandles.put(checkpoint, new Long(timestamp));
                }
            }
        }
        _checkpoints.put(new Long(_currentHandle), checkpointsAndHandles);
        return _currentHandle++;
    }
    
    public boolean rollback(long handle, boolean trim) {
        HashMap checkpointsAndHandles =
            (HashMap)_checkpoints.get(new Long(handle));
        if (checkpointsAndHandles == null) {
            return false;
        } else {
            Iterator checkpoints = checkpointsAndHandles.keySet().iterator();
            while (checkpoints.hasNext()) {
                Checkpoint checkpoint = (Checkpoint)checkpoints.next();
                long timestamp =
                    ((Long)checkpointsAndHandles.get(checkpoint)).longValue();
                checkpoint.rollback(timestamp, trim);
            }
            return true;
        }
    }
    
    private long _currentHandle = 0;
    
    private HashMap _checkpoints = new HashMap();
}
