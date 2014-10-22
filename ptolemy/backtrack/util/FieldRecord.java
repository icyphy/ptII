/* The records (or change history) of a field in a class.

 Copyright (c) 2005-2014 The Regents of the University of California.
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

import java.util.Iterator;
import java.util.Stack;

///////////////////////////////////////////////////////////////////
//// FieldRecord

/**
 The records (or change history) of a field in a class. In a Java program
 refactored with ptolemy.backtrack.eclipse.ast.Transformer to support
 backtracking, every change on a field by means of assignment is recorded.
 The old value of the field is stored in a <tt>FieldRecord</tt>. Every
 entry in the record includes the information of the timestamp (see {@link
 ptolemy.backtrack.Checkpoint}) denoting the time when the value was
 recorded, as well as that old value of the field.
 <p>
 It is possible the records of a field have more than zero dimension, when the
 field is an array. In that case, records of different dimensions are stored
 in different tables. E.g., an <tt>int</tt> is considered zero-dimensional,
 and only one list is used to store its change history. an <tt>int[]</tt> is
 considered one-dimensional (with one possible index). Two lists are used in
 order to record assignments to the array itself, and the assignment to one
 of its element. In the latter case, the index of the changed element is also
 recorded. Similarly, a field of type <tt>int[][]</tt> requires three lists.
 <p>
 Lists of records are added with different <tt>add</tt> functions, which take
 a timestamp and record the old value taken at that timestamp at the head
 position of the corresponding list. Timestamps must be increasing. A value
 with larger timestamp means it is taken later in time.
 <p>
 To access the lists, iterators must be used to iterate from the most recent
 recorded value to the earliest ones. Records in the lists may also be removed
 with <tt>remove</tt> functions of the iterators. As with many other
 iterators, modifications cannot be made simultaneously with two different
 iterators. <tt>add</tt> functions cannot be called while accessing any list
 with an iterator, either. If changes are made simultaneously, the effect is
 unpredictable.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class FieldRecord {
    ///////////////////////////////////////////////////////////////////
    ////                        constructors                       ////

    /** Construct a zero-dimensional (scalar) field record.
     */
    public FieldRecord() {
        this(0);
    }

    /** Construct a multi-dimensional field record.
     *
     *  @param dimensions Number of dimensions; can be omitted
     *   when it is 1.
     */
    public FieldRecord(int dimensions) {
        _states.push(new FieldRecordState(dimensions + 1));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     *
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], Object, long)
     */
    public void add(Object value, long timestamp) {
        _addRecord(0, new Record(null, value, timestamp));
    }

    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     *
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], boolean, long)
     */
    public void add(boolean value, long timestamp) {
        _addRecord(0, new Record(null, value, timestamp));
    }

    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     *
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], byte, long)
     */
    public void add(byte value, long timestamp) {
        _addRecord(0, new Record(null, Byte.valueOf(value), timestamp));
    }

    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     *
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], char, long)
     */
    public void add(char value, long timestamp) {
        _addRecord(0, new Record(null, Character.valueOf(value), timestamp));
    }

    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     *
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], double, long)
     */
    public void add(double value, long timestamp) {
        _addRecord(0, new Record(null, Double.valueOf(value), timestamp));
    }

    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     *
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], float, long)
     */
    public void add(float value, long timestamp) {
        _addRecord(0, new Record(null, Float.valueOf(value), timestamp));
    }

    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     *
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], int, long)
     */
    public void add(int value, long timestamp) {
        _addRecord(0, new Record(null, Integer.valueOf(value), timestamp));
    }

    /** Add an old value to the specified indices of the records, and
     *  associate it with a timestamp. The indices is the array of indices
     *  to locate the element in the array. E.g., the following assignment
     *  makes it necessary to record the old value at indices
     *  <tt>[1][2][3]</tt>:
     *  <pre>buf[1][2][3] = new char[10];</pre>
     *  <p>
     *  If the indices array is null, it is assumed that no index is
     *  needed.
     *
     *  @param indices The indices.
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     */
    public void add(int[] indices, Object value, long timestamp) {
        _addRecord((indices == null) ? 0 : indices.length, new Record(indices,
                value, timestamp));
    }

    /** Add an old value to the specified indices of the records, and
     *  associate it with a timestamp. The indices is the array of indices
     *  to locate the element in the array. E.g., the following assignment
     *  makes it necessary to record the old value at indices
     *  <tt>[1][2][3]</tt>:
     *  <pre>buf[1][2][3] = new char[10];</pre>
     *  <p>
     *  If the indices array is null, it is assumed that no index is
     *  needed.
     *
     *  @param indices The indices.
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     */
    public void add(int[] indices, boolean value, long timestamp) {
        _addRecord((indices == null) ? 0 : indices.length, new Record(indices,
                value, timestamp));
    }

    /** Add an old value to the specified indices of the records, and
     *  associate it with a timestamp. The indices is the array of indices
     *  to locate the element in the array. E.g., the following assignment
     *  makes it necessary to record the old value at indices
     *  <tt>[1][2][3]</tt>:
     *  <pre>buf[1][2][3] = new char[10];</pre>
     *  <p>
     *  If the indices array is null, it is assumed that no index is
     *  needed.
     *
     *  @param indices The indices.
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     */
    public void add(int[] indices, byte value, long timestamp) {
        _addRecord((indices == null) ? 0 : indices.length, new Record(indices,
                Byte.valueOf(value), timestamp));
    }

    /** Add an old value to the specified indices of the records, and
     *  associate it with a timestamp. The indices is the array of indices
     *  to locate the element in the array. E.g., the following assignment
     *  makes it necessary to record the old value at indices
     *  <tt>[1][2][3]</tt>:
     *  <pre>buf[1][2][3] = new char[10];</pre>
     *  <p>
     *  If the indices array is null, it is assumed that no index is
     *  needed.
     *
     *  @param indices The indices.
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     */
    public void add(int[] indices, char value, long timestamp) {
        _addRecord((indices == null) ? 0 : indices.length, new Record(indices,
                Character.valueOf(value), timestamp));
    }

    /** Add an old value to the specified indices of the records, and
     *  associate it with a timestamp. The indices is the array of indices
     *  to locate the element in the array. E.g., the following assignment
     *  makes it necessary to record the old value at indices
     *  <tt>[1][2][3]</tt>:
     *  <pre>buf[1][2][3] = new char[10];</pre>
     *  <p>
     *  If the indices array is null, it is assumed that no index is
     *  needed.
     *
     *  @param indices The indices.
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     */
    public void add(int[] indices, double value, long timestamp) {
        _addRecord((indices == null) ? 0 : indices.length, new Record(indices,
                Double.valueOf(value), timestamp));
    }

    /** Add an old value to the specified indices of the records, and
     *  associate it with a timestamp. The indices is the array of indices
     *  to locate the element in the array. E.g., the following assignment
     *  makes it necessary to record the old value at indices
     *  <tt>[1][2][3]</tt>:
     *  <pre>buf[1][2][3] = new char[10];</pre>
     *  <p>
     *  If the indices array is null, it is assumed that no index is
     *  needed.
     *
     *  @param indices The indices.
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     */
    public void add(int[] indices, float value, long timestamp) {
        _addRecord((indices == null) ? 0 : indices.length, new Record(indices,
                Float.valueOf(value), timestamp));
    }

    /** Add an old value to the specified indices of the records, and
     *  associate it with a timestamp. The indices is the array of indices
     *  to locate the element in the array. E.g., the following assignment
     *  makes it necessary to record the old value at indices
     *  <tt>[1][2][3]</tt>:
     *  <pre>buf[1][2][3] = new char[10];</pre>
     *  <p>
     *  If the indices array is null, it is assumed that no index is
     *  needed.
     *
     *  @param indices The indices.
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     */
    public void add(int[] indices, int value, long timestamp) {
        _addRecord((indices == null) ? 0 : indices.length, new Record(indices,
                Integer.valueOf(value), timestamp));
    }

    /** Add an old value to the specified indices of the records, and
     *  associate it with a timestamp. The indices is the array of indices
     *  to locate the element in the array. E.g., the following assignment
     *  makes it necessary to record the old value at indices
     *  <tt>[1][2][3]</tt>:
     *  <pre>buf[1][2][3] = new char[10];</pre>
     *  <p>
     *  If the indices array is null, it is assumed that no index is
     *  needed.
     *
     *  @param indices The indices.
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     */
    public void add(int[] indices, long value, long timestamp) {
        _addRecord((indices == null) ? 0 : indices.length, new Record(indices,
                Long.valueOf(value), timestamp));
    }

    /** Add an old value to the specified indices of the records, and
     *  associate it with a timestamp. The indices is the array of indices
     *  to locate the element in the array. E.g., the following assignment
     *  makes it necessary to record the old value at indices
     *  <tt>[1][2][3]</tt>:
     *  <pre>buf[1][2][3] = new char[10];</pre>
     *  <p>
     *  If the indices array is null, it is assumed that no index is
     *  needed.
     *
     *  @param indices The indices.
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     */
    public void add(int[] indices, short value, long timestamp) {
        _addRecord((indices == null) ? 0 : indices.length, new Record(indices,
               Short.valueOf(value), timestamp));
    }

    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     *
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], long, long)
     */
    public void add(long value, long timestamp) {
        _addRecord(0, new Record(null, Long.valueOf(value), timestamp));
    }

    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     *
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], short, long)
     */
    public void add(short value, long timestamp) {
        _addRecord(0, new Record(null, Short.valueOf(value), timestamp));
    }

    /** Backup the values in an array, and associate the record with a
     *  timestamp. This is the same as calling <tt>backup(null, array,
     *  timestamp)</tt>.
     *
     *  @param array The array.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     */
    public void backup(Object array, long timestamp) {
        backup(null, array, timestamp);
    }

    /** Backup the values in an array, and associate the record with a
     *  timestamp.
     *
     *  @param indices The indices.
     *  @param array The array.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     */
    public void backup(int[] indices, Object array, long timestamp) {
        Object oldValue;

        if (array instanceof boolean[]) {
            oldValue = ((boolean[]) array).clone();
        } else if (array instanceof byte[]) {
            oldValue = ((byte[]) array).clone();
        } else if (array instanceof char[]) {
            oldValue = ((char[]) array).clone();
        } else if (array instanceof double[]) {
            oldValue = ((double[]) array).clone();
        } else if (array instanceof float[]) {
            oldValue = ((float[]) array).clone();
        } else if (array instanceof int[]) {
            oldValue = ((int[]) array).clone();
        } else if (array instanceof long[]) {
            oldValue = ((long[]) array).clone();
        } else if (array instanceof short[]) {
            oldValue = ((short[]) array).clone();
        } else if (array instanceof Object[]) {
            oldValue = ((Object[]) array).clone();
        } else {
            return;
        }

        _addRecord((indices == null) ? 0 : indices.length, new Record(indices,
                oldValue, timestamp, true));
    }

    /** Commit the changes in all the <tt>FieldRecord</tt> objects up to the
     *  time represented by the timestamp. Records older than that time are
     *  deleted.
     *
     *  @param records The array of field records.
     *  @param timestamp The timestamp.
     *  @param topStackTimestamp The timestamp taken when the checkpoint object
     *   was assigned.
     *  @see #commit(long)
     *  @see CheckpointRecord#getTopTimestamp()
     */
    public static void commit(FieldRecord[] records, long timestamp,
            long topStackTimestamp) {
        for (int i = 0; i < records.length; i++) {
            records[i].commit(timestamp);
        }

        if (timestamp > topStackTimestamp) {
            for (int i = 0; i < records.length; i++) {
                records[i].commitState();
            }
        }
    }

    /** Commit the changes up to the time represented by the timestamp. Records
     *  older than that time are deleted.
     *
     *  @param timestamp The timestamp.
     */
    public void commit(long timestamp) {
        FieldRecordState topState = _getTopState();

        if (topState != null) {
            RecordList[] recordLists = topState._getRecords();
            int totalNum = 0;

            for (int i = 0; i < recordLists.length; i++) {
                if (recordLists[i] != null) {
                    RecordList list = recordLists[i];

                    while ((list != null)
                            && (list._getRecord().getTimestamp() >= timestamp)) {
                        list = list._getNext();
                        totalNum++;
                    }

                    if (list != null) {
                        if (list._getPrevious() != null) {
                            list._getPrevious()._setNext(null);
                        } else {
                            recordLists[i] = null;
                        }
                    }
                }
            }

            topState._setTotalNum(totalNum);
        }
    }

    /** Commit the state of this field record, and delete older states in its
     *  stack.
     *  <p>
     *  Old states of field records are kept when a new checkpoint object is
     *  assigned to a monitored object. This function deletes those old states,
     *  but keep only the last (current) state.
     *
     *  @see #popState()
     *  @see #pushState()
     */
    public void commitState() {
        FieldRecordState lastState = _getTopState();
        _states.clear();

        if (lastState != null) {
            _states.push(lastState);
        }
    }

    /** Return the iterator of all the records. If the field is an array,
     *  the records with different indices are stored in separate lists.
     *  The iterator returned by this function combines all those lists,
     *  and the records that it returns are sorted with their timestamps.
     *  Records created more recently are returned earlier with {@link
     *  Iterator#next()}.
     *
     *  @return The iterator.
     *  @see #iterator(int)
     */
    public Iterator iterator() {
        return new CombinedIterator();
    }

    /** Return the iterator of the records with the specified index.
     *  E.g., for a field <tt>f</tt> of type <tt>int[][]</tt>
     *  (2-dimensional):
     *  <ol>
     *    <li>When <tt>index</tt> is 0, it returns the iterator of
     *      records saved for assignments to the field itself.</li>
     *    <li>When <tt>index</tt> is 1, it returns the iterator of
     *      records saved for assignments to the field with one
     *      index (e.g., <tt>f[1] = new int[10]</tt>).</li>
     *    <li>When <tt>index</tt> is 2, it returns the iterator of
     *      records saved for assignments to the field with two
     *      indices (e.g., <tt>f[1][2] = 3</tt>).</li>
     *  </ol>
     *
     *  @param index The index.
     *  @return The iterator.
     *  @see #iterator()
     */
    public Iterator iterator(int index) {
        return new IndividualIterator(index);
    }

    /** Pop out the top state in the states stack, and the state next to it
     *  becomes the top state. The states stack must have at least two states
     *  in it.
     */
    public void popState() {
        _states.pop();
    }

    /** For each state in the given array, pop out the top state. Used to
     *  simplify the implementation of refactoring.
     *
     *  @param records The array of field records.
     *  @see #popState()
     */
    public static void popState(FieldRecord[] records) {
        for (int i = 0; i < records.length; i++) {
            records[i].popState();
        }
    }

    /** Push a new state onto the top of the states stack, and the current top
     *  state becomes the one right below it.
     */
    public void pushState() {
        _states.push(new FieldRecordState(_getTopState()._getRecords().length));
    }

    /** For each state in the given array, push in a new state. Used to
     *  simplify the implementation of refactoring.
     *
     *  @param records The array of field records.
     *  @see #pushState()
     */
    public static void pushState(FieldRecord[] records) {
        for (int i = 0; i < records.length; i++) {
            records[i].pushState();
        }
    }

    /** Restore the old value at the timestamp to the field.
     *  <p>
     *  The given timestamp refers to the time when the field still possesses
     *  its old value. If the timestamp is increased at an assignment, the old
     *  value at that timestamp refers to the value of the field before
     *  assignment.
     *
     *  @param current The current value of the field.
     *  @param timestamp The timestamp.
     *  @param trim If <tt>true</tt>, any values newer than the restored value
     *   are deleted from the record.
     *  @return The old value to be assigned back to the field.
     */
    public Object restore(Object current, long timestamp, boolean trim) {
        int indices = _getTopState()._getRecords().length;

        if (indices == 1) {
            Iterator recordIter = iterator(0);
            Record record = _findRecord(recordIter, timestamp, trim);
            return record.getValue();
        } else {
            Iterator recordIter = iterator();

            while (recordIter.hasNext()) {
                Record record = (Record) recordIter.next();

                if (record.getTimestamp() < timestamp) {
                    break;
                } else {
                    current = _restoreField(current, record);
                }

                if (trim) {
                    recordIter.remove();
                }
            }

            return current;
        }
    }

    /** Restore the old value at the timestamp to the field.
     *  <p>
     *  The given timestamp refers to the time when the field still possesses
     *  its old value. If the timestamp is increased at an assignment, the old
     *  value at that timestamp refers to the value of the field before
     *  assignment.
     *
     *  @param current The current value of the field.
     *  @param timestamp The timestamp.
     *  @param trim If <tt>true</tt>, any values newer than the restored value
     *   are deleted from the record.
     *  @return The old value to be assigned back to the field.
     */
    public boolean restore(boolean current, long timestamp, boolean trim) {
        Iterator recordIter = iterator(0);
        Record record = _findRecord(recordIter, timestamp, trim);

        if (record == null) {
            return current;
        } else {
            return ((Boolean) record.getValue()).booleanValue();
        }
    }

    /** Restore the old value at the timestamp to the field.
     *  <p>
     *  The given timestamp refers to the time when the field still possesses
     *  its old value. If the timestamp is increased at an assignment, the old
     *  value at that timestamp refers to the value of the field before
     *  assignment.
     *
     *  @param current The current value of the field.
     *  @param timestamp The timestamp.
     *  @param trim If <tt>true</tt>, any values newer than the restored value
     *   are deleted from the record.
     *  @return The old value to be assigned back to the field.
     */
    public byte restore(byte current, long timestamp, boolean trim) {
        Iterator recordIter = iterator(0);
        Record record = _findRecord(recordIter, timestamp, trim);

        if (record == null) {
            return current;
        } else {
            return ((Byte) record.getValue()).byteValue();
        }
    }

    /** Restore the old value at the timestamp to the field.
     *  <p>
     *  The given timestamp refers to the time when the field still possesses
     *  its old value. If the timestamp is increased at an assignment, the old
     *  value at that timestamp refers to the value of the field before
     *  assignment.
     *
     *  @param current The current value of the field.
     *  @param timestamp The timestamp.
     *  @param trim If <tt>true</tt>, any values newer than the restored value
     *   are deleted from the record.
     *  @return The old value to be assigned back to the field.
     */
    public char restore(char current, long timestamp, boolean trim) {
        Iterator recordIter = iterator(0);
        Record record = _findRecord(recordIter, timestamp, trim);

        if (record == null) {
            return current;
        } else {
            return ((Character) record.getValue()).charValue();
        }
    }

    /** Restore the old value at the timestamp to the field.
     *  <p>
     *  The given timestamp refers to the time when the field still possesses
     *  its old value. If the timestamp is increased at an assignment, the old
     *  value at that timestamp refers to the value of the field before
     *  assignment.
     *
     *  @param current The current value of the field.
     *  @param timestamp The timestamp.
     *  @param trim If <tt>true</tt>, any values newer than the restored value
     *   are deleted from the record.
     *  @return The old value to be assigned back to the field.
     */
    public double restore(double current, long timestamp, boolean trim) {
        Iterator recordIter = iterator(0);
        Record record = _findRecord(recordIter, timestamp, trim);

        if (record == null) {
            return current;
        } else {
            return ((Double) record.getValue()).doubleValue();
        }
    }

    /** Restore the old value at the timestamp to the field.
     *  <p>
     *  The given timestamp refers to the time when the field still possesses
     *  its old value. If the timestamp is increased at an assignment, the old
     *  value at that timestamp refers to the value of the field before
     *  assignment.
     *
     *  @param current The current value of the field.
     *  @param timestamp The timestamp.
     *  @param trim If <tt>true</tt>, any values newer than the restored value
     *   are deleted from the record.
     *  @return The old value to be assigned back to the field.
     */
    public float restore(float current, long timestamp, boolean trim) {
        Iterator recordIter = iterator(0);
        Record record = _findRecord(recordIter, timestamp, trim);

        if (record == null) {
            return current;
        } else {
            return ((Float) record.getValue()).floatValue();
        }
    }

    /** Restore the old value at the timestamp to the field.
     *  <p>
     *  The given timestamp refers to the time when the field still possesses
     *  its old value. If the timestamp is increased at an assignment, the old
     *  value at that timestamp refers to the value of the field before
     *  assignment.
     *
     *  @param current The current value of the field.
     *  @param timestamp The timestamp.
     *  @param trim If <tt>true</tt>, any values newer than the restored value
     *   are deleted from the record.
     *  @return The old value to be assigned back to the field.
     */
    public int restore(int current, long timestamp, boolean trim) {
        Iterator recordIter = iterator(0);
        Record record = _findRecord(recordIter, timestamp, trim);

        if (record == null) {
            return current;
        } else {
            return ((Integer) record.getValue()).intValue();
        }
    }

    /** Restore the old value at the timestamp to the field.
     *  <p>
     *  The given timestamp refers to the time when the field still possesses
     *  its old value. If the timestamp is increased at an assignment, the old
     *  value at that timestamp refers to the value of the field before
     *  assignment.
     *
     *  @param current The current value of the field.
     *  @param timestamp The timestamp.
     *  @param trim If <tt>true</tt>, any values newer than the restored value
     *   are deleted from the record.
     *  @return The old value to be assigned back to the field.
     */
    public long restore(long current, long timestamp, boolean trim) {
        Iterator recordIter = iterator(0);
        Record record = _findRecord(recordIter, timestamp, trim);

        if (record == null) {
            return current;
        } else {
            return ((Long) record.getValue()).longValue();
        }
    }

    /** Restore the old value at the timestamp to the field.
     *  <p>
     *  The given timestamp refers to the time when the field still possesses
     *  its old value. If the timestamp is increased at an assignment, the old
     *  value at that timestamp refers to the value of the field before
     *  assignment.
     *
     *  @param current The current value of the field.
     *  @param timestamp The timestamp.
     *  @param trim If <tt>true</tt>, any values newer than the restored value
     *   are deleted from the record.
     *  @return The old value to be assigned back to the field.
     */
    public short restore(short current, long timestamp, boolean trim) {
        Iterator recordIter = iterator(0);
        Record record = _findRecord(recordIter, timestamp, trim);

        if (record == null) {
            return current;
        } else {
            return ((Short) record.getValue()).shortValue();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       nested classes                      ////
    ///////////////////////////////////////////////////////////////////
    //// CombinedIterator

    /**
     Combined iterator of all the dimensions. It returns records in their
     reversed timestamp order.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     @see FieldRecord#iterator()
     */
    public class CombinedIterator implements Iterator {
        /** Test if there are more elements.
         *
         *  @return <tt>true</tt> if there are more elements.
         */
        public boolean hasNext() {
            return _currentNum < _getTopState()._getTotalNum();
        }

        /** Return the next element.
         *
         *  @return The next element.
         */
        public Object next() {
            _currentNum++;
            _lastIndex = _maxTimestampIndex();
            _lastRecord = _currentRecords[_lastIndex];
            _currentRecords[_lastIndex] = _currentRecords[_lastIndex]
                    ._getNext();
            return _lastRecord._getRecord();
        }

        /** Remove the last element returned by {@link #next()}.
         *  This function must be called after {@link #next()}.
         */
        public void remove() {
            RecordList previous = _lastRecord._getPrevious();

            if (previous == null) { // The first one.

                RecordList first = _lastRecord._getNext();

                if (first != null) {
                    first._setPrevious(null);
                }

                _getTopState()._getRecords()[_lastIndex] = first;
            } else {
                previous._setNext(_lastRecord._getNext());
            }

            _lastRecord = null;
            _getTopState()._decreaseTotalNum();
            _currentNum--;
        }

        /** Construct an iterator.
         */
        CombinedIterator() {
            int indices = _getTopState()._getRecords().length;
            _currentRecords = new RecordList[indices];

            for (int i = 0; i < indices; i++) {
                _currentRecords[i] = _getTopState()._getRecords()[i];
            }
        }

        /** Get the index of the maximum timestamp in the current
         *  records.
         *
         *  @return The index.
         */
        private int _maxTimestampIndex() {
            int maxIdentifier = -1;
            long maxTimestamp = -1;
            int maxIndex = -1;

            for (int i = 0; i < _currentRecords.length; i++) {
                RecordList list = _currentRecords[i];

                if (list != null) {
                    Record record = list._getRecord();
                    long timestamp = record.getTimestamp();
                    int identifier = record.getIdentifier();

                    if ((timestamp > maxTimestamp)
                            || ((timestamp == maxTimestamp) && (identifier > maxIdentifier))) {
                        maxIdentifier = identifier;
                        maxTimestamp = timestamp;
                        maxIndex = i;
                    }
                }
            }

            return maxIndex;
        }

        /** The number of records that have been returned by {@link #next()}.
         */
        private int _currentNum = 0;

        /** The current record for each dimension. Each current record is the
         *  next record to be returned by {@link #next()} for that index, or
         *  <tt>null</tt> if no more record for that dimension.
         */
        private RecordList[] _currentRecords;

        /** The dimension of the last returned record.
         */
        private int _lastIndex;

        /** The last returned record.
         */
        private RecordList _lastRecord;
    }

    ///////////////////////////////////////////////////////////////////
    //// IndividualIterator

    /**
     Iterator of the records for the given dimension.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     @see FieldRecord#iterator(int)
     */
    public class IndividualIterator implements Iterator {
        /** Test if there are more elements.
         *
         *  @return <tt>true</tt> if there are more elements.
         */
        public boolean hasNext() {
            return _currentList != null;
        }

        /** Return the next element.
         *
         *  @return The next element.
         */
        public Object next() {
            _lastRecord = _currentList;
            _currentList = _currentList._getNext();
            return _lastRecord._getRecord();
        }

        /** Remove the last element returned by {@link #next()}.
         *  This function must be called after {@link #next()}.
         */
        public void remove() {
            RecordList previous = _lastRecord._getPrevious();

            if (previous == null) {
                if (_currentList != null) {
                    _currentList._setPrevious(null);
                }

                _getTopState()._getRecords()[_index] = _currentList;
            } else {
                previous._setNext(_currentList);
            }

            _getTopState()._decreaseTotalNum();
        }

        /** Construct an iterator for the given index of dimensions.
         *
         *  @param index The index.
         *  @see FieldRecord#iterator(int)
         */
        IndividualIterator(int index) {
            _index = index;
            _currentList = _getTopState()._getRecords()[index];
        }

        /** The current record for that dimension.
         */
        private RecordList _currentList;

        /** The index.
         */
        private int _index;

        /** The last record returned by {@link #next()}.
         */
        private RecordList _lastRecord;
    }

    ///////////////////////////////////////////////////////////////////
    //// Record

    /**
     Record of an old value.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public class Record {
        /** Get the identifier of this record. Each record for a field has a
         *  unique identifier. Identifiers increase over time. Records for
         *  different fields may have the same identifier.
         *
         *  @return The identifier.
         */
        public int getIdentifier() {
            return _identifier;
        }

        /** Get the indices on the left-hand side of the assignment.
         *
         *  @return The indices, or <tt>null</tt> if no index is used.
         */
        public int[] getIndices() {
            return _indices;
        }

        /** Get the timestamp taken at the time when the record is
         *  created.
         *
         *  @return The timestamp.
         */
        public long getTimestamp() {
            return _timestamp;
        }

        /** Get the old value of this record. If the old value is
         *  of a primitive type, it is boxed with the corresponding
         *  object type.
         *
         *  @return The old value.
         */
        public Object getValue() {
            return _value;
        }

        /** Test if this record is a backup of an array.
         *
         *  @return <tt>true</tt>if this record is a backup of an array.
         */
        public boolean isBackup() {
            return _isBackup;
        }

        /** Convert this record to a readable string.
         *
         *  @return The string.
         */
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("FieldRecord: indices(");

            if (_indices == null) {
                buffer.append("null");
            } else {
                for (int i = 0; i < _indices.length; i++) {
                    buffer.append(_indices[i]);

                    if (i < (_indices.length - 1)) {
                        buffer.append(",");
                    }
                }
            }

            buffer.append(") timestamp(");
            buffer.append(getTimestamp());
            buffer.append(") oldValue(");
            buffer.append(getValue());
            buffer.append(")");
            return buffer.toString();
        }

        /** Construct a record and store an old value in it. The record is not
         *  a backup of an array.
         *
         *  @param indices The indices on the left-hand side of the
         *   assignment.
         *  @param value The old value. If the old value is of a
         *   primitive type, it should be boxed with the corresponding
         *   object type.
         *  @param timestamp The current timestamp.
         */
        Record(int[] indices, Object value, long timestamp) {
            _indices = indices;
            _value = value;
            _timestamp = timestamp;
            _isBackup = false;
            _identifier = _getTopState()._increaseIdentifier();
        }

        /** Construct a record and store an old value in it.
         *
         *  @param indices The indices on the left-hand side of the
         *   assignment.
         *  @param value The old value. If the old value is of a
         *   primitive type, it should be boxed with the corresponding
         *   object type.
         *  @param timestamp The current timestamp.
         *  @param isBackup Whether this record is a backup of an array.
         */
        Record(int[] indices, Object value, long timestamp, boolean isBackup) {
            _indices = indices;
            _value = value;
            _timestamp = timestamp;
            _isBackup = isBackup;
            _identifier = _getTopState()._increaseIdentifier();
        }

        /** The identifier of this record (unique for each field).
         */
        private int _identifier;

        /** The indices.
         */
        private int[] _indices;

        /** Whether this record is a backup of an array.
         */
        private boolean _isBackup;

        /** The timestamp.
         */
        private long _timestamp;

        /** The old value.
         */
        private Object _value;
    }

    ///////////////////////////////////////////////////////////////////
    //// RecordList

    /** Add a record to the list at the given index.
     *
     *  @param index The index.
     *  @param record The record.
     */
    protected void _addRecord(int index, Record record) {
        RecordList list = new RecordList(record);
        list._setNext(_getTopState()._getRecords()[index]);
        _getTopState()._getRecords()[index] = list;
        _getTopState()._increaseTotalNum();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Perform a deep copy from a source array to a destination array. If
     *  those arrays are multi-dimensional, sub-arrays of them are copied
     *  respectively.
     *
     *  @param source The source array.
     *  @param destination The destination array.
     *  @return <tt>true</tt> if successfully copied; otherwise,
     *   <tt>false</tt>.
     */
    protected boolean _deepCopyArray(Object source, Object destination) {
        if (source instanceof boolean[]) {
            System.arraycopy(source, 0, destination, 0,
                    ((boolean[]) source).length);
            return true;
        } else if (source instanceof byte[]) {
            System.arraycopy(source, 0, destination, 0,
                    ((byte[]) source).length);
            return true;
        } else if (source instanceof char[]) {
            System.arraycopy(source, 0, destination, 0,
                    ((char[]) source).length);
            return true;
        } else if (source instanceof double[]) {
            System.arraycopy(source, 0, destination, 0,
                    ((double[]) source).length);
            return true;
        } else if (source instanceof float[]) {
            System.arraycopy(source, 0, destination, 0,
                    ((float[]) source).length);
            return true;
        } else if (source instanceof int[]) {
            System
                    .arraycopy(source, 0, destination, 0,
                            ((int[]) source).length);
            return true;
        } else if (source instanceof long[]) {
            System.arraycopy(source, 0, destination, 0,
                    ((long[]) source).length);
            return true;
        } else if (source instanceof short[]) {
            System.arraycopy(source, 0, destination, 0,
                    ((short[]) source).length);
            return true;
        } else if (source instanceof Object[]) {
            Object[] sourceArray = (Object[]) source;
            Object[] destinationArray = (Object[]) destination;

            for (int i = 0; i < sourceArray.length; i++) {
                if (!_deepCopyArray(sourceArray[i], destinationArray[i])) {
                    destinationArray[i] = sourceArray[i];
                }
            }

            return true;
        } else {
            return false;
        }
    }

    /** Find the record with the smallest timestamp that is larger than the
     *  given timestamp.
     *
     *  @param recordListIterator The iterator with which the records are
     *   searched.
     *  @param timestamp The timestamp.
     *  @param trim If <tt>true</tt>, the records found with timestamps equal
     *   to or larger than the given timestamp are deleted.
     *  @return The record, if found; otherwise, <tt>null</tt>.
     */
    protected Record _findRecord(Iterator recordListIterator, long timestamp,
            boolean trim) {
        Record lastRecord = null;

        while (recordListIterator.hasNext()) {
            Record record = (Record) recordListIterator.next();

            if (record.getTimestamp() >= timestamp) {
                lastRecord = record;
            } else {
                break;
            }

            if (trim) {
                recordListIterator.remove();
            }
        }

        return lastRecord;
    }

    /** Get the state on the top of the states stack.
     *
     *  @return The state on the top, or <tt>null</tt> if the states stack is
     *   empty.
     */
    protected FieldRecordState _getTopState() {
        if (_states.isEmpty()) {
            return null;
        } else {
            return _states.peek();
        }
    }

    /** Restore the old value in a record to the field.
     *
     *  @param field The field to be restored.
     *  @param record The record.
     *  @return The field. It may differ from the field in the arguments.
     */
    protected Object _restoreField(Object field, Record record) {
        int[] indices = record.getIndices();

        if ((indices == null) || (indices.length == 0)) {
            if (record.isBackup()) {
                _deepCopyArray(record.getValue(), field);
                return field;
            } else {
                return record.getValue();
            }
        } else {
            int length = indices.length;
            Object array = field;

            for (int i = 0; i < (length - 1); i++) {
                array = ((Object[]) array)[indices[i]];
            }

            int lastIndex = indices[length - 1];

            if (array instanceof boolean[]) {
                ((boolean[]) array)[lastIndex] = ((Boolean) record.getValue())
                        .booleanValue();
            } else if (array instanceof byte[]) {
                ((byte[]) array)[lastIndex] = ((Byte) record.getValue())
                        .byteValue();
            } else if (array instanceof char[]) {
                ((char[]) array)[lastIndex] = ((Character) record.getValue())
                        .charValue();
            } else if (array instanceof double[]) {
                ((double[]) array)[lastIndex] = ((Double) record.getValue())
                        .doubleValue();
            } else if (array instanceof float[]) {
                ((float[]) array)[lastIndex] = ((Float) record.getValue())
                        .floatValue();
            } else if (array instanceof int[]) {
                ((int[]) array)[lastIndex] = ((Integer) record.getValue())
                        .intValue();
            } else if (array instanceof long[]) {
                ((long[]) array)[lastIndex] = ((Long) record.getValue())
                        .longValue();
            } else if (array instanceof short[]) {
                ((short[]) array)[lastIndex] = ((Short) record.getValue())
                        .shortValue();
            } else {
                if (record.isBackup()) {
                    _deepCopyArray(record.getValue(),
                            ((Object[]) array)[lastIndex]);
                } else {
                    ((Object[]) array)[lastIndex] = record.getValue();
                }
            }

            return field;
        }
    }

    /**
     Double linked list of records.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    protected static class RecordList {
        // FindBugs suggests that this should be a static inner class.

        /** Get the record list next to this one.
         *
         *  @return The next record list.
         *  @see #_getPrevious()
         */
        protected RecordList _getNext() {
            return _next;
        }

        /** Get the record list previous to this one.
         *
         *  @return The previous record list.
         *  @see #_getNext()
         */
        protected RecordList _getPrevious() {
            return _previous;
        }

        /** Get the record.
         *
         *  @return The record.
         */
        protected Record _getRecord() {
            return _record;
        }

        /** Set the record list next to this one. Its previous
         *  record list is also set to this one.
         *
         *  @param next The next record list.
         *  @see #_setPrevious(ptolemy.backtrack.util.FieldRecord.RecordList)
         */
        protected void _setNext(RecordList next) {
            _next = next;

            if (next != null) {
                next._previous = this;
            }
        }

        /** Set the record list previous to this one. Its
         *  next record list is also set to this one.
         *
         *  @param previous The previous record list.
         *  @see #_setNext(ptolemy.backtrack.util.FieldRecord.RecordList)
         */
        protected void _setPrevious(RecordList previous) {
            _previous = previous;

            if (previous != null) {
                previous._next = this;
            }
        }

        /** Construct a record list object with a record stored in it.
         *
         *  @param record
         */
        RecordList(Record record) {
            _record = record;
        }

        /** The record list next to this one.
         */
        private RecordList _next = null;

        /** The record list previous to this one.
         */
        private RecordList _previous = null;

        /** The record.
         */
        private Record _record;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The stack of (possibly) multiple field record states.
     */
    private Stack<FieldRecordState> _states = new Stack<FieldRecordState>();
}
