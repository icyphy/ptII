/* A base class for random sources.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.lib.Source;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.backtrack.util.java.util.Random;
import ptolemy.data.LongToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
//////////////////////////////////////////////////////////////////////////
//// RandomSource

/** 
 * A base class for sources of random numbers.
 * It uses the class java.util.Random.
 * This base class manages the seed.
 * @author Edward A. Lee, Steve Neuendorffer
 * @version $Id$
 * @since Ptolemy II 0.3
 * @Pt.ProposedRating Green (eal)
 * @Pt.AcceptedRating Green (bilung)
 */
public abstract class RandomSource extends Source implements Rollbackable {

    protected Checkpoint $CHECKPOINT = new Checkpoint(this);

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**     
     * The seed that controls the random number generation.
     * A seed of zero is interpreted to mean that no seed is specified,
     * which means that each execution of the model could result in
     * distinct data. For the value 0, the seed is set to
     * System.currentTimeMillis() + hashCode(), which means that
     * with extremely high probability, two distinct actors will have
     * distinct seeds.  However, current time may not have enough
     * resolution to ensure that two subsequent executions of the
     * same model have distinct seeds.
     * This parameter contains a LongToken, initially with value 0.
     */
    public Parameter seed;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // Get an independent random number generator.
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    /**         ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

     * The Random object. 
     */
    protected Random _random = (Random)new Random().$SET$CHECKPOINT($CHECKPOINT);

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**     
     * Indicator that a new random number is needed. 
     */
    private boolean _needNew = false;

    /**     
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public RandomSource(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        seed = new Parameter(this, "seed", new LongToken(0));
        seed.setTypeEquals(BaseType.LONG);
    }

    /**     
     * Clone the actor into the specified workspace. This calls the
     * base class and then creates new ports and parameters.
     * @param workspace The workspace for the new object.
     * @return A new actor.
     * @exception CloneNotSupportedException If a derived class contains
     * an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException  {
        RandomSource newObject = (RandomSource)(super.clone(workspace));
        newObject._random = new Random();
        return newObject;
    }

    /**     
     * Generate a new random number if this is the first firing
     * of the iteration.
     * @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        if (_needNew) {
            _generateRandomNumber();
            $ASSIGN$_needNew(false);
        }
    }

    /**     
     * Calculate the next random number.
     * @exception IllegalActionException If the base class throws it.
     * @return True if it is ok to continue.
     */
    public boolean postfire() throws IllegalActionException  {
        $ASSIGN$_needNew(true);
        return super.postfire();
    }

    /**     
     * Initialize the random number generator with the seed, if it
     * has been given.  A seed of zero is interpreted to mean that no
     * seed is specified.  In such cases, a seed based on the current
     * time and this instance of a RandomSource is used to be fairly
     * sure that two identical sequences will not be returned.
     * @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException  {
        super.initialize();
        long sd = ((LongToken)(seed.getToken())).longValue();
        if (sd != 0) {
            _random.setSeed(sd);
        } else {
            _random.setSeed(System.currentTimeMillis() + hashCode());
        }
        $ASSIGN$_needNew(true);
    }

    /**     
     * Generate a new random number.
     * @exception IllegalActionException Not thrown in this base class.
     * Derived classes may throw it if there are problems getting parameter
     * values.
     */
    protected abstract void _generateRandomNumber() throws IllegalActionException ;

    private final boolean $ASSIGN$_needNew(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_needNew.add(null, _needNew, $CHECKPOINT.getTimestamp());
        }
        return _needNew = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _needNew = $RECORD$_needNew.restore(_needNew, timestamp, trim);
        if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
            $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
            FieldRecord.popState($RECORDS);
            $RESTORE(timestamp, trim);
        }
    }

    public final Checkpoint $GET$CHECKPOINT() {
        return $CHECKPOINT;
    }

    public final Object $SET$CHECKPOINT(Checkpoint checkpoint) {
        if ($CHECKPOINT != checkpoint) {
            Checkpoint oldCheckpoint = $CHECKPOINT;
            if (checkpoint != null) {
                $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint.getTimestamp());
                FieldRecord.pushState($RECORDS);
            }
            $CHECKPOINT = checkpoint;
            oldCheckpoint.setCheckpoint(checkpoint);
            checkpoint.addObject(this);
        }
        return this;
    }

    protected CheckpointRecord $RECORD$$CHECKPOINT = new CheckpointRecord();

    private FieldRecord $RECORD$_needNew = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_needNew
        };

}

