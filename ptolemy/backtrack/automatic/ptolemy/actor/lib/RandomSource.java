/* A base class for random sources.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
///////////////////////////////////////////////////////////////////
//// RandomSource
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import java.util.Random;
import ptolemy.actor.lib.Source;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.BooleanToken;
import ptolemy.data.LongToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * A base class for sources of random numbers.
 * It uses the class java.util.Random.
 * This base class manages the seed. Specifically,
 * the seed is a shared parameter, so setting the seed
 * in any one instance of a RandomSource results in setting
 * the seed in all instances. If the seed is set to value 0L,
 * the default, then this is interpreted as not specifying a seed,
 * and the random number generators are set to use a seed that
 * depends on the current time in milliseconds. If the seed
 * is set to any value other than 0L, then a seed is computed
 * for the random number generator by adding that specified seed
 * to the hashcode for the full name of the actor. This ensures
 * that with high probability, multiple instances of RandomSource
 * (and derived classes) will have distinct seeds, but that the
 * executions are deterministically repeatable.
 * <p>
 * If the <i>resetOnEachRun</i> parameter is true (it is
 * false by default), then each run resets the random number
 * generator. If the seed is non-zero, then this makes
 * each run identical.  This is useful for constructing
 * tests. If the seed is zero, then a new seed is generated
 * on each run using the same technique described above
 * (combining current time and the hash code).
 * @author Edward A. Lee, Steve Neuendorffer, Elaine Cheong
 * @version $Id$
 * @since Ptolemy II 0.3
 * @Pt.ProposedRating Green (eal)
 * @Pt.AcceptedRating Green (bilung)
 */
public abstract class RandomSource extends Source implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * If true, this parameter specifies that the random number
     * generator should be reset on each run of the model (in
     * the initialize() method). It is a boolean that defaults
     * to false. This is a shared parameter, meaning that changing
     * it somewhere in the model causes it to be changed everywhere
     * in the model.
     */
    public SharedParameter resetOnEachRun;

    /**
     * The seed that controls the random number generation.
     * This is a shared parameter, meaning that all instances of
     * RandomSource or derived classes in the same model share the
     * same value.
     * A seed of zero is interpreted to mean that no seed is specified,
     * which means that each execution of the model could result in
     * distinct data. For the value 0, the seed is set to
     * System.currentTimeMillis() + hashCode(), which means that
     * with extremely high probability, two distinct actors will have
     * distinct seeds.  However, current time may not have enough
     * resolution to ensure that two subsequent executions of the
     * same model have distinct seeds. For a value other than zero,
     * the seed is set to that value plus the hashCode() of the
     * full name of the actor. This means that with high probability,
     * two distinct actors will have distinct, but repeatable seeds.
     * This parameter contains a LongToken, initially with value 0.
     */
    public SharedParameter seed;

    /**
     * This private seed overrides the shared seed parameter to specify a
     * particular seed rather than using System.currentTimeMillis() or
     * hashCode() to compute the seed value.
     * By default, this parameter is empty, which means that the shared seed
     * parameter is used.
     * WARNING: It is up to the user to make sure that different seed
     * values are used in different random number generators.
     */
    public Parameter privateSeed;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // It is too soon to generate the new generator because
    // all clones will have the same actor name, which results
    // in the same seed.
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    // BTW - the reason to use the full name here is so that
    // each random number generator generates a sequence
    // of different random numbers.  If we use just the
    // display name, then two actors that have the same
    // name will generate the same sequence of numbers which
    // is bad for Monte Carlo simulations.
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    /**
     * The current value of the seed parameter.
     */
    protected long _generatorSeed = 0L;

    /**
     * Indicator that a new random number is needed.
     */
    protected boolean _needNew = false;

    /**
     * Indicator that a new generator is needed.
     */
    protected boolean _needNewGenerator = true;

    /**
     * The Random object.
     */
    protected Random _random;

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
        seed = new SharedParameter(this, "seed", RandomSource.class, "0L");
        seed.setTypeEquals(BaseType.LONG);
        privateSeed = new Parameter(this, "privateSeed");
        privateSeed.setTypeEquals(BaseType.LONG);
        resetOnEachRun = new SharedParameter(this, "resetOnEachRun", RandomSource.class, "false");
        resetOnEachRun.setTypeEquals(BaseType.BOOLEAN);
        new SingletonParameter(trigger, "_showName").setToken(BooleanToken.TRUE);
    }

    /**
     * If the attribute is <i>seed</i> or <i>useThisSeed</i>
     * then create the base random number generator.
     * @param attribute The attribute that changed.
     * @exception IllegalActionException If the change is not acceptable
     * to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException  {
        if (attribute == seed || attribute == privateSeed) {
            long seedValue;
            Token privateSeedToken = privateSeed.getToken();
            if (privateSeedToken != null) {
                seedValue = ((LongToken)privateSeedToken).longValue();
            } else {
                seedValue = ((LongToken)seed.getToken()).longValue();
            }
            if (seedValue != _generatorSeed) {
                _needNewGenerator = true;
            }
        } else {
            super.attributeChanged(attribute);
        }
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
        RandomSource newObject = (RandomSource)super.clone(workspace);
        newObject._needNewGenerator = true;
        return newObject;
    }

    /**
     * Generate a new random number if this is the first firing
     * of the iteration.
     * @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        if (_needNewGenerator) {
            _createGenerator();
        }
        if (_needNew) {
            _generateRandomNumber();
            _needNew = false;
        }
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
        if (_random == null || ((BooleanToken)resetOnEachRun.getToken()).booleanValue()) {
            _createGenerator();
        }
        _needNew = true;
    }

    /**
     * Calculate the next random number.
     * @exception IllegalActionException If the base class throws it.
     * @return True if it is ok to continue.
     */
    public boolean postfire() throws IllegalActionException  {
        _needNew = true;
        return super.postfire();
    }

    /**
     * Create the random number generator using current parameter values.
     * @exception IllegalActionException If thrown while reading the
     * seed Token.
     */
    protected void _createGenerator() throws IllegalActionException  {
        long seedValue;
        Token privateSeedToken = privateSeed.getToken();
        if (privateSeedToken != null) {
            seedValue = ((LongToken)privateSeedToken).longValue();
            _generatorSeed = seedValue;
        } else {
            seedValue = ((LongToken)seed.getToken()).longValue();
            _generatorSeed = seedValue;
            if (seedValue == 0L) {
                seedValue = System.currentTimeMillis() + hashCode();
            } else {
                seedValue = seedValue + getFullName().hashCode();
            }
        }
        _random = new Random(seedValue);
        _needNewGenerator = false;
        _needNew = true;
    }

    /**
     * Generate a new random number.
     * @exception IllegalActionException Not thrown in this base class.
     * Derived classes may throw it if there are problems getting parameter
     * values.
     */
    protected abstract void _generateRandomNumber() throws IllegalActionException ;

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
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

    protected transient CheckpointRecord $RECORD$$CHECKPOINT = new CheckpointRecord();

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
        };

}

