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

package ptolemy.backtrack;

import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// CheckPoint
/**
 
 
 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Checkpoint {
    
    public Checkpoint(Rollbackable object) {
        addObject(object);
    }
    
    public void addObject(Rollbackable object) {
        _state.getMonitoredObjects().add(object);
    }
    
    public synchronized long createCheckpoint() {
        return _state.createCheckpoint();
    }

    public synchronized long getTimestamp() {
        return _state.getTimestamp();
    }
    
    public boolean isCheckpointing() {
        return _state != null;
    }
    
    public void removeObject(Rollbackable object) {
        _state.getMonitoredObjects().remove(object);
    }
    
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
    
    public void setCheckpoint(Checkpoint checkpoint) {
        List objects = _state.getMonitoredObjects();
        while (objects.size() > 0) {
            Rollbackable object = (Rollbackable)objects.remove(0);
            object.$SET$CHECKPOINT(checkpoint);
        }
    }
    
    private CheckpointState _state = new CheckpointState();
}
