/* An actor for model checkpointing and rollback.

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
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Backtrack
/**
 This actor provides the interface to the backtracking mechanism. It is able to
 create checkpoints (in which case a checkpoint handle is returned) and roll
 back to previously created checkpoints.

 This actor has a "checkpoint" input port. It takes a boolean token in every
 firing, which means whether a checkpoint should be created. If it is true, a
 checkpoint will be created in the firing, and the checkpoint handle (a long
 number) will be output to the "handle" output port; otherwise, no output value
 will be sent to the "handle" port. The "rollback" input port takes a checkpoint
 handle (a long number) in each firing. If the handle is greater than 0, a
 rollback operation will be performed, and the whole model, including its
 hierarchical components if any, restores its previous state recorded for that
 checkpoint; if the given checkpoint handle is equal to or less than 0, the
 rollback operation will not be performed.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Backtrack extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Backtrack(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _checkpoint = new TypedIOPort(this, "checkpoint", true, false);
        _checkpoint.setTypeEquals(BaseType.BOOLEAN);

        _rollback = new TypedIOPort(this, "rollback", true, false);
        _rollback.setTypeEquals(BaseType.LONG);

        _handle = new TypedIOPort(this, "handle", false, true);
        _handle.setTypeEquals(BaseType.LONG);

        // Put the rollback input on the bottom of the actor.
        StringAttribute rollbackCardinal = new StringAttribute(_rollback,
                "_cardinal");
        rollbackCardinal.setExpression("SOUTH");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Backtrack newObject = (Backtrack) super.clone(workspace);

        newObject._handleMap = new HashMap();

        try {

            ptolemy.kernel.Port oldPort = newObject.getPort("checkpoint");
            if (oldPort != null) {
                oldPort.setContainer(null);
            }
            newObject._checkpoint = new TypedIOPort(newObject, "checkpoint",
                    true, false);
            newObject._checkpoint.setTypeEquals(BaseType.BOOLEAN);

            // These ports do not follow the naming convention, so we have
            // to clone them separately.

            oldPort = newObject.getPort("rollback");
            if (oldPort != null) {
                oldPort.setContainer(null);
            }
            newObject._rollback = new TypedIOPort(newObject, "rollback", true,
                    false);
            newObject._rollback.setTypeEquals(BaseType.LONG);

            oldPort = newObject.getPort("handle");
            if (oldPort != null) {
                oldPort.setContainer(null);
            }

            newObject._handle = new TypedIOPort(newObject, "handle", false,
                    true);
            newObject._handle.setTypeEquals(BaseType.LONG);
        } catch (Exception ex) {
            // CloneNotSupportedException does not have a constructor
            // that takes a cause argument, so we use initCause
            CloneNotSupportedException throwable = new CloneNotSupportedException();
            throwable.initCause(ex);
            throw throwable;
        }

        return newObject;
    }

    /** Create checkpoint and record the given checkpoint handle if required. A
     *  boolean token is read from the "checkpoint" port. If the token is true,
     *  a checkpoint is created, and the checkpoint handle is sent to the
     *  "handle" output port; if the token is false, no output is sent. A long
     *  token is read from the "rollback" port. If rollback is requested, the
     *  received token contains the checkpoint handle to be rolled back to,
     *  which is greater than 0. This checkpoint handle is recorded in a private
     *  field, and the rollback operation will actually be performed in {@link
     *  #postfire()}.
     *
     *  @exception IllegalActionException If the fire method of the superclass
     *  throws this exception.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        boolean checkpointTrigger = false;
        if (_checkpoint.isOutsideConnected() && _checkpoint.hasToken(0)) {
            checkpointTrigger = ((BooleanToken) _checkpoint.get(0))
                    .booleanValue();
        }

        if (_rollback.isOutsideConnected() && _rollback.hasToken(0)) {
            LongToken rollbackHandle = (LongToken) _rollback.get(0);
            _rollbackHandle = rollbackHandle.longValue();
        } else {
            _rollbackHandle = -1;
        }

        if (checkpointTrigger) {
            HashMap<Checkpoint, Long> handles = new HashMap<Checkpoint, Long>();
            _checkpoint(handles, (CompositeActor) getContainer());
            _currentHandle++;
            _handleMap.put(Long.valueOf(_currentHandle), handles);
            _handle.send(0, new LongToken(_currentHandle));
        }
    }

    /** Commit the rollback operation if it is required in {@link #fire()}. If
     *  the last token received from the "rollback" port is greater than 0, it
     *  is recorded as the checkpoint handle used for the rollback. When
     *  performed, the rollback operation restores the state of the whole model.
     *
     *  @exception IllegalActionException If the postfire method of the
     *  superclass throws this exception.
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();

        if (_rollbackHandle > 0) {
            HashMap<Checkpoint, Long> handles = _handleMap.get(Long
                    .valueOf(_rollbackHandle));

            if (handles != null) {
                for (Checkpoint checkpointObject : handles.keySet()) {
                    long handle = (handles.get(checkpointObject)).longValue();
                    checkpointObject.rollback(handle);
                }
            }

            Iterator keys = _handleMap.keySet().iterator();
            while (keys.hasNext()) {
                long handle = ((Long) keys.next()).longValue();
                if (handle > _rollbackHandle) {
                    keys.remove();
                }
            }
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create checkpoints for the actors in the given composite actor, and
     *  record the checkpoint handles in the map. The checkpoint objects are the
     *  keys in the map, and their checkpoint handles are the values associated
     *  with the keys.
     *
     *  Only the actors that implements the {@link Rollbackable} interface can
     *  be checkpointed.
     *
     *  This method recursively invokes itself on the composite actors in the
     *  given composite actor.
     *
     *  @param handles The map that associates the checkpoint objects with the
     *  checkpoint handles for them.
     *  @param container The composite actor to be checkpointed.
     */
    private void _checkpoint(HashMap<Checkpoint, Long> handles,
            CompositeActor container) {
        Iterator entities = container.entityList(Rollbackable.class).iterator();
        while (entities.hasNext()) {
            Rollbackable entity = (Rollbackable) entities.next();
            Checkpoint checkpointObject = entity.$GET$CHECKPOINT();
            if (!handles.containsKey(checkpointObject)) {
                long handle = checkpointObject.createCheckpoint();
                handles.put(checkpointObject, Long.valueOf(handle));
            }
        }

        Iterator compositeActors = container.entityList(CompositeActor.class)
                .iterator();
        while (compositeActors.hasNext()) {
            CompositeActor compositeActor = (CompositeActor) compositeActors
                    .next();
            if (!(compositeActor instanceof Rollbackable)) {
                _checkpoint(handles, compositeActor);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The input port that receives boolean tokens meaning whether checkpoints
     *  should be created.
     */
    private TypedIOPort _checkpoint;

    /** The last returned checkpoint handle. The next checkpoint handle to be
     *  returned will be greater by 1.
     */
    private long _currentHandle = 0;

    /** The output port that sends out the newly created checkpoint handle.
     */
    private TypedIOPort _handle;

    /** The map from checkpoint handle output by this actor to the map of
     *  checkpoint handles returned by checkpoint objects. For each checkpoint
     *  created by this actor, there is a unique handle. A map is recorded as
     *  the value for this handle. The keys in the map are checkpoint objects
     *  managing the actors in the model; the values in the map are the
     *  checkpoint handles returned by those checkpoint objects.
     */
    private HashMap<Long, HashMap<Checkpoint, Long>> _handleMap = new HashMap<Long, HashMap<Checkpoint, Long>>();

    /** The input port that receives long tokens as the checkpoint handles to
     *  roll back (if greater than 0).
     */
    private TypedIOPort _rollback;

    /** The checkpoint handle to roll back if greater than 0. The fire method
     *  records this field. The postfire method performs the rollback if this
     *  field is greater then 0.
     */
    private long _rollbackHandle;
}
