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

package ptolemy.backtrack.manual.ptolemy.actor.lib;

import java.util.HashMap;
import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.data.BooleanToken;
import ptolemy.data.LongToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Backtrack
/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Backtrack extends TypedAtomicActor {
    public Backtrack(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        _checkpoint = new TypedIOPort(this, "checkpoint", true, false);
        _checkpoint.setTypeEquals(BaseType.BOOLEAN);
        
        _rollback = new TypedIOPort(this, "rollback", true, false);
        _rollback.setTypeEquals(BaseType.LONG);
        
        _handle = new TypedIOPort(this, "handle", false, true);
        _handle.setTypeEquals(BaseType.LONG);
    }
    
    public void fire() throws IllegalActionException {
        super.fire();
        
        BooleanToken checkpointTrigger = (BooleanToken)_checkpoint.get(0);
        this._checkpointTrigger = checkpointTrigger.booleanValue();
        
        LongToken rollbackHandle = (LongToken)_rollback.get(0);
        this._rollbackHandle = rollbackHandle.longValue();
        
        if (_checkpointTrigger) {
            HashMap handles = new HashMap();
            _checkpoint(handles, (CompositeActor)getContainer());
            _currentHandle++;
            _handleMap.put(new Long(_currentHandle), handles);
            _handle.send(0, new LongToken(_currentHandle));
        }
    }

    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        
        if (_rollbackHandle > 0) {
            HashMap handles = (HashMap)_handleMap.get(new Long(_rollbackHandle));
            
            if (handles != null) {
                Iterator checkpoints = handles.keySet().iterator();
                while (checkpoints.hasNext()) {
                    Checkpoint checkpointObject = (Checkpoint)checkpoints.next();
                    long handle = ((Long)handles.get(checkpointObject)).longValue();
                    checkpointObject.rollback(handle);
                }
            }
            
            Iterator keys = _handleMap.keySet().iterator();
            while (keys.hasNext()) {
                long handle = ((Long)keys.next()).longValue();
                if (handle > _rollbackHandle) {
                    keys.remove();
                }
            }
        }
        
        return result;
    }
    
    private void _checkpoint(HashMap handles, CompositeActor container) {
        Iterator entities = container.entityList(Rollbackable.class).
                iterator();
        while (entities.hasNext()) {
            Rollbackable entity = (Rollbackable)entities.next();
            Checkpoint checkpointObject = entity.$GET$CHECKPOINT();
            if (!handles.containsKey(checkpointObject)) {
                long handle = checkpointObject.createCheckpoint();
                handles.put(checkpointObject, new Long(handle));
            }
        }
        
        Iterator compositeActors = container.entityList(CompositeActor.class).
                iterator();
        while (compositeActors.hasNext()) {
            CompositeActor compositeActor = (CompositeActor)compositeActors.next();
            if (!(compositeActor instanceof Rollbackable)) {
                _checkpoint(handles, compositeActor);
            }
        }
    }
    
    private TypedIOPort _checkpoint;
    
    private boolean _checkpointTrigger;
    
    private long _currentHandle = 0;
    
    private TypedIOPort _handle;
    
    private HashMap _handleMap = new HashMap();
    
    private TypedIOPort _rollback;
    
    private long _rollbackHandle;
}
