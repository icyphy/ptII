/* Record all input tokens for later querying.

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
//// Recorder
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import ptolemy.actor.lib.Sink;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * <p>Record all input tokens for later querying.  This actor can be used for
 * testing configurations of actors.  It can also be used in programs that
 * invoke Ptolemy models and wish to query the results after the model
 * is run.  The input tokens are read in the postfire() method so that
 * in domains with fixed-point semantics, only the final, settled value
 * is recorded.  The current time is also recorded for each value.
 * </p><p>
 * The <i>capacity</i> parameter limits the size of the record.
 * If the capacity is set to zero, then no tokens are recorded, but
 * the total number of input tokens is counted.  You can access
 * the count via the getCount() method.  If the capacity is 1,
 * then only the most recently seen token on each channel is recorded.
 * If the capacity is negative (the default), then the capacity
 * is infinite.</p>
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 0.3
 * @Pt.ProposedRating Green (eal)
 * @Pt.AcceptedRating Green (bilung)
 */
public class Recorder extends Sink implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * The capacity of the record for each channel.
     * This parameter must contain an IntToken.
     */
    public Parameter capacity;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // Remove the first element.
    //     public void wrapup() throws IllegalActionException {
    //         super.wrapup();
    //         for (int channel = 0; channel < input.getWidth(); channel++) {
    //             List history = getHistory(channel);
    //             Iterator tokens = history.iterator();
    //             while (tokens.hasNext()) {
    //                 Token token = (Token)tokens.next();
    //                 System.out.println(getFullName() + channel + ": " + token);
    //             }
    //         }
    //     }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Count of events seen.
    // A linked list of arrays.
    // The most recent set of inputs.
    // A linked list of Double objects, which are times.
    // A token to indicate absence.
    private int _count = 0;

    private List _records;

    Token[] _latest;

    private List _timeRecord;

    private static Token _bottom = new StringToken("_");

    /**
     * Construct an actor with an input multiport that can accept any
     * Token.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the entity cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public Recorder(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        capacity = new Parameter(this, "capacity", new IntToken(-1));
        capacity.setTypeEquals(BaseType.INT);
    }

    /**
     * Get the total number of events seen so far.
     * @return The total number of events seen so far.
     */
    public int getCount() {
        return _count;
    }

    /**
     * Get the history for the specified channel number.  If in any
     * firing there is no such channel, or no token was read on that
     * channel, then a string token with value "_" is returned in the
     * position of the list corresponding to that firing.
     * If nothing has been recorded (there have been no firings),
     * then return an empty list.
     * @param channel The input channel for which the history is desired.
     * @return A list of Token objects.
     */
    public List getHistory(int channel) {
        ArrayList result = new ArrayList();
        if (_records != null) {
            result.ensureCapacity(_records.size());
            Iterator firings = _records.iterator();
            while (firings.hasNext()) {
                Token[] record = (Token[])firings.next();
                if (channel < record.length) {
                    if (record[channel] != null) {
                        result.add(record[channel]);
                        continue;
                    }
                }
                result.add(_bottom);
            }
        }
        return result;
    }

    /**
     * Get the latest input for the specified channel.
     * If there has been no record yet for the specified channel,
     * then return the string token "_", representing "bottom".
     * @param channel The input channel for the record is desired.
     * @return The latest input token.
     */
    public Token getLatest(int channel) {
        if (_latest == null || channel >= _latest.length || _latest[channel] == null) {
            return _bottom;
        }
        return _latest[channel];
    }

    /**
     * Get the record for the specified channel number.  If in any
     * firing there is no such channel, or no token was read on that
     * channel, then a string token with value "_" is returned.
     * If nothing has been recorded (there have been no firings),
     * then return an empty enumeration.
     * @param channel The input channel for the record is desired.
     * @return An enumeration of Token objects.
     * @deprecated This method is deprecated. Use getHistory().
     */
    @Deprecated public Enumeration getRecord(int channel) {
        return Collections.enumeration(getHistory(channel));
    }

    /**
     * Get the history of the time of each invocation of postfire().
     * @return A list of Double objects.
     */
    public List getTimeHistory() {
        return _timeRecord;
    }

    /**
     * Get the record of the current time of each invocation of postfire().
     * @return An enumeration of Double objects.
     * @deprecated This method is deprecated. Use getTimeHistory().
     */
    @Deprecated public Enumeration getTimeRecord() {
        return Collections.enumeration(_timeRecord);
    }

    /**
     * Initialize the lists used to record input data.
     * @exception IllegalActionException If the parent class throws it.
     */
    @Override public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_records(new LinkedList());
        $ASSIGN$_timeRecord(new LinkedList());
        _latest = null;
        $ASSIGN$_count(0);
    }

    /**
     * Read at most one token from each input channel and record its value.
     * @exception IllegalActionException If there is no director.
     */
    @Override public boolean postfire() throws IllegalActionException  {
        if (!super.postfire()) {
            return false;
        }
        int width = input.getWidth();
        Token[] record = new Token[width];
        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);
                record[i] = token;
                $ASSIGN$SPECIAL$_count(11, _count);
            }
        }
        int capacityValue = ((IntToken)capacity.getToken()).intValue();
        if (capacityValue != 0) {
            _records.add(record);
            _timeRecord.add(Double.valueOf(getDirector().getModelTime().getDoubleValue()));
            if (capacityValue > 0 && _records.size() > capacityValue) {
                _records.remove(0);
                _timeRecord.remove(0);
            }
        }
        _latest = record;
        return true;
    }

    private final int $ASSIGN$_count(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_count.add(null, _count, $CHECKPOINT.getTimestamp());
        }
        return _count = newValue;
    }

    private final int $ASSIGN$SPECIAL$_count(int operator, long newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_count.add(null, _count, $CHECKPOINT.getTimestamp());
        }
        switch (operator) {
            case 0:
                return _count += newValue;
            case 1:
                return _count -= newValue;
            case 2:
                return _count *= newValue;
            case 3:
                return _count /= newValue;
            case 4:
                return _count &= newValue;
            case 5:
                return _count |= newValue;
            case 6:
                return _count ^= newValue;
            case 7:
                return _count %= newValue;
            case 8:
                return _count <<= newValue;
            case 9:
                return _count >>= newValue;
            case 10:
                return _count >>>= newValue;
            case 11:
                return _count++;
            case 12:
                return _count--;
            case 13:
                return ++_count;
            case 14:
                return --_count;
            default:
                return _count;
        }
    }

    private final List $ASSIGN$_records(List newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_records.add(null, _records, $CHECKPOINT.getTimestamp());
        }
        return _records = newValue;
    }

    private final List $ASSIGN$_timeRecord(List newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_timeRecord.add(null, _timeRecord, $CHECKPOINT.getTimestamp());
        }
        return _timeRecord = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _count = $RECORD$_count.restore(_count, timestamp, trim);
        _records = (List)$RECORD$_records.restore(_records, timestamp, trim);
        _timeRecord = (List)$RECORD$_timeRecord.restore(_timeRecord, timestamp, trim);
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

    private transient FieldRecord $RECORD$_count = new FieldRecord(0);

    private transient FieldRecord $RECORD$_records = new FieldRecord(0);

    private transient FieldRecord $RECORD$_timeRecord = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_count,
            $RECORD$_records,
            $RECORD$_timeRecord
        };

}

