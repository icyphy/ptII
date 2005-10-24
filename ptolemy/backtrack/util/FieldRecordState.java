/* The state of a field record.

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

import ptolemy.backtrack.util.FieldRecord.RecordList;

//////////////////////////////////////////////////////////////////////////
//// FieldRecordState

/**
 The state of a field record. A field record records the changes in a field.
 Each {@link Rollbackable} object may have 0 or more field records. The
 information of a field record is kept in a field record state. When a new
 checkpoint object is assigned to the {@link Rollbackable} object, its
 previous field record states are pushed on to stacks, and new states are
 allocated. When the previous checkpoint object is restored to the {@link
 Rollbackable} object, the previous field record states are popped out.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class FieldRecordState {
    ///////////////////////////////////////////////////////////////////
    ////                       constructor                         ////
    protected FieldRecordState(int dimensions) {
        _records = new RecordList[dimensions + 1];
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////

    /** Decrease the total number of changes recorded in this field record
     *  state by 1.
     *
     *  @return The new total number of changes recorded.
     *  @see #_getTotalNum()
     *  @see #_increaseTotalNum()
     *  @see #_setTotalNum(int)
     */
    protected int _decreaseTotalNum() {
        return --_totalNum;
    }

    /** Get the identifier of this field record state.
     *
     *  @return The identifier.
     */
    protected int _getIdentifier() {
        return _identifier;
    }

    /** Get the array of change history for different numbers of indices.
     *
     *  @return The array of change history.
     */
    protected RecordList[] _getRecords() {
        return _records;
    }

    /** Get the total number of changes recorded in this field record state.
     *
     *  @return The total number of changes.
     *  @see #_setTotalNum(int)
     */
    protected int _getTotalNum() {
        return _totalNum;
    }

    /** Increase the identifier of this field record state by 1.
     *
     *  @return The new identifier.
     *  @see #_getIdentifier()
     */
    protected int _increaseIdentifier() {
        return ++_identifier;
    }

    /** Increase the total number of changes recorded in this field record
     *  state by 1.
     *
     *  @return The new total number of changes recorded.
     *  @see #_decreaseTotalNum()
     *  @see #_getTotalNum()
     *  @see #_setTotalNum(int)
     */
    protected int _increaseTotalNum() {
        return ++_totalNum;
    }

    /** Set the total number of changes recorded in this field record state.
     *
     *  @param totalNum The new total number of changes.
     *  @see #_getTotalNum()
     */
    protected void _setTotalNum(int totalNum) {
        _totalNum = totalNum;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The record lists for all the dimensions.
     */
    private RecordList[] _records;

    /** The total number of records in all the dimensions. Must be
     *  explicitly managed when records are added or removed.
     */
    private int _totalNum = 0;

    /** An increasing identifier for each record.
     */
    private int _identifier = 0;
}
