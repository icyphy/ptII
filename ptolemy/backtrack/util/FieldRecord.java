/* The records (or change history) of a field in a class.

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

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// FieldRecord
/**
   The records (or change history) of a field in a class. In a Java program
   refactored with {@link ptolemy.backtrack.ast.Transformer} to support
   backtracking, every change on a field by means of assignment is recorded.
   The old value of the field is stored in a <tt>FieldRecord</tt>. Every
   entry in the record includes the information of the timestamp (see {@link
   ptolemy.backtrack.Checkpoint}) denoting the time when the value was
   recorded, as well as that old value of the field.
   <p>
   It is possible the records of a field have more than one dimension, when the
   field is an array. In that case, records of different dimensions are stored
   in different tables. E.g., an <tt>int</tt> is considered one dimensional, 
   and only one list is used to store its change history. an <tt>int[]</tt> is
   considered two dimensional (with one possible index). Two lists are used in
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
public final class FieldRecord {
    
    /** Construct a one-dimensional (scalar) field record.
     */
    public FieldRecord() {
        this(1);
    }
    
    /** Construct a multi-dimensional field record.
     * 
     *  @param dimensions Number of dimensions; can be omitted
     *   when it is 1.
     */
    public FieldRecord(int dimensions) {
        _records = new RecordList[dimensions];
    }
    
    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     * 
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], boolean, int)
     */
    public void add(boolean value, int timestamp) {
        _addRecord(0, new Record(null, new Boolean(value), timestamp));
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
    public void add(int[] indices, boolean value, int timestamp) {
        _addRecord(indices == null ? 0 : indices.length, 
                new Record(indices, new Boolean(value), timestamp));
    }
    
    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     * 
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], byte, int)
     */
    public void add(byte value, int timestamp) {
        _addRecord(0, new Record(null, new Byte(value), timestamp));
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
    public void add(int[] indices, byte value, int timestamp) {
        _addRecord(indices == null ? 0 : indices.length, 
                new Record(indices, new Byte(value), timestamp));
    }
    
    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     * 
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], char, int)
     */
    public void add(char value, int timestamp) {
        _addRecord(0, new Record(null, new Character(value), timestamp));
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
    public void add(int[] indices, char value, int timestamp) {
        _addRecord(indices == null ? 0 : indices.length, 
                new Record(indices, new Character(value), timestamp));
    }
    
    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     * 
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], double, int)
     */
    public void add(double value, int timestamp) {
        _addRecord(0, new Record(null, new Double(value), timestamp));
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
    public void add(int[] indices, double value, int timestamp) {
        _addRecord(indices == null ? 0 : indices.length, 
                new Record(indices, new Double(value), timestamp));
    }
    
    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     * 
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], float, int)
     */
    public void add(float value, int timestamp) {
        _addRecord(0, new Record(null, new Float(value), timestamp));
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
    public void add(int[] indices, float value, int timestamp) {
        _addRecord(indices == null ? 0 : indices.length, 
                new Record(indices, new Float(value), timestamp));
    }
    
    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     * 
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], int, int)
     */
    public void add(int value, int timestamp) {
        _addRecord(0, new Record(null, new Integer(value), timestamp));
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
    public void add(int[] indices, int value, int timestamp) {
        _addRecord(indices == null ? 0 : indices.length, 
                new Record(indices, new Integer(value), timestamp));
    }
    
    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     * 
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], long, int)
     */
    public void add(long value, int timestamp) {
        _addRecord(0, new Record(null, new Long(value), timestamp));
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
    public void add(int[] indices, long value, int timestamp) {
        _addRecord(indices == null ? 0 : indices.length, 
                new Record(indices, new Long(value), timestamp));
    }
    
    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     * 
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], short, int)
     */
    public void add(short value, int timestamp) {
        _addRecord(0, new Record(null, new Short(value), timestamp));
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
    public void add(int[] indices, short value, int timestamp) {
        _addRecord(indices == null ? 0 : indices.length, 
                new Record(indices, new Short(value), timestamp));
    }
    
    /** Add an old value to the records, associated with a timestamp. This
     *  is the same as calling <tt>add(null, value, timestamp)</tt>, where
     *  the value is stored without any indexing (e.g., the field is not
     *  an array, or no indexing is needed for the value).
     * 
     *  @param value The old value.
     *  @param timestamp The current timestamp to be associated with the
     *   old value.
     *  @see #add(int[], Object, int)
     */
    public void add(Object value, int timestamp) {
        _addRecord(0, new Record(null, value, timestamp));
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
    public void add(int[] indices, Object value, int timestamp) {
        _addRecord(indices == null ? 0 : indices.length, 
                new Record(indices, value, timestamp));
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
     *  (3-dimensional):
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
    
    //////////////////////////////////////////////////////////////////////////
    //// CombinedIterator
    /**
       Combinated iterator of all the dimensions. It returns records in their
       reversed timestamp order.
       
       @author Thomas Feng
       @version $Id$
       @since Ptolemy II 5.1
       @Pt.ProposedRating Red (tfeng)
       @Pt.AcceptedRating Red (tfeng)
       @see {@link FieldRecord#iterator()}
    */
    public class CombinedIterator implements Iterator {

        /** Test if there are more elements.
         * 
         *  @return <tt>true</tt> if there are more elements.
         */
        public boolean hasNext() {
            return _currentNum < _totalNum;
        }
        
        /** Return the next element.
         * 
         *  @return The next element.
         */
        public Object next() {
            _currentNum++;
            _lastIndex = _maxTimestampIndex();
            _lastRecord = _currentRecords[_lastIndex];
            _currentRecords[_lastIndex] = 
                _currentRecords[_lastIndex]._getNext();
            return _lastRecord._getRecord();
        }
        
        /** Remove the last element returned by {@link #next()}.
         *  This function must be called after {@link #next()}.
         */
        public void remove() {
            RecordList previous = _lastRecord._getPrevious();
            if (previous == null) {  // The first one.
                RecordList first = _lastRecord._getNext();
                if (first != null)
                    first._setPrevious(null);
                _records[_lastIndex] = first;
            } else
                previous._setNext(_lastRecord._getNext());
            _lastRecord = null;
            _totalNum--;
            _currentNum--;
        }
        
        /** Construct an iterator.
         */
        CombinedIterator() {
            int indices = _records.length;
            _currentRecords = new RecordList[indices];
            for (int i = 0; i < indices; i++)
                _currentRecords[i] = _records[i];
        }
        
        /** Get the index of the maximum timestamp in the current
         *  records.
         *  
         *  @return The index.
         */
        private int _maxTimestampIndex() {
            int max = -1;
            int maxIndex = -1;
            for (int i = 0; i < _currentRecords.length; i++) {
                RecordList list = _currentRecords[i];
                int timestamp;
                if (list != null && 
                        (timestamp = list._getRecord().getTimestamp()) > max) {
                    max = timestamp;
                    maxIndex = i;
                }
            }
            return maxIndex;
        }
        
        /** The current record for each dimension. Each current record is the
         *  next record to be returned by {@link #next()} for that index, or
         *  <tt>null</tt> if no more record for that dimension.
         */
        private RecordList[] _currentRecords;
        
        /** The number of records that have been returned by {@link #next()}.
         */
        private int _currentNum = 0;
        
        /** The dimension of the last returned record.
         */
        private int _lastIndex;
        
        /** The last returned record.
         */
        private RecordList _lastRecord;
    }
    
    //////////////////////////////////////////////////////////////////////////
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
                if (_currentList != null)
                    _currentList._setPrevious(null);
                _records[_index] = _currentList;
            } else
                previous._setNext(_currentList);
            _totalNum--;
        }
        
        /** Construct an iterator for the given index of dimensions.
         * 
         *  @param index The index.
         *  @see FieldRecord#iterator(int)
         */
        IndividualIterator(int index) {
            _index = index;
            _currentList = _records[index];
        }
        
        /** The index.
         */
        private int _index;
        
        /** The current record for that dimension.
         */
        private RecordList _currentList;
        
        /** The last record returned by {@link #next()}.
         */
        private RecordList _lastRecord;
    }
    
    //////////////////////////////////////////////////////////////////////////
    //// Record
    /**
       Record for the old value of an assignment.
       
       @author Thomas Feng
       @version $Id$
       @since Ptolemy II 5.1
       @Pt.ProposedRating Red (tfeng)
       @Pt.AcceptedRating Red (tfeng)
    */
    public class Record {
        
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
        public int getTimestamp() {
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
        
        /** Convert this record to a readable string.
         * 
         *  @return The string.
         */
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("FieldRecord: indices(");
            if (_indices == null)
                buffer.append("null");
            else
                for (int i = 0; i < _indices.length; i++) {
                    buffer.append(_indices[i]);
                    if (i < _indices.length - 1)
                        buffer.append(",");
                }
            buffer.append(") timestamp(");
            buffer.append(_timestamp);
            buffer.append(") oldValue(");
            buffer.append(_value);
            buffer.append(")");
            return buffer.toString();
        }
        
        /** Construct a record and store an old value in it.
         * 
         *  @param indices The indices on the left-hand side of the
         *   assignment.
         *  @param value The old value. If the old value is of a
         *   primitive type, it should be boxed with the corresponding
         *   object type.
         *  @param timestamp The current timestamp.
         */
        Record(int[] indices, Object value, int timestamp) {
            _indices = indices;
            _value = value;
            _timestamp = timestamp;
        }
        
        /** The indices.
         */
        private int[] _indices;
        
        /** The old value.
         */
        private Object _value;
        
        /** The timestamp.
         */
        private int _timestamp;
    }
    
    //////////////////////////////////////////////////////////////////////////
    //// RecordList
    /**
       Double linked list of records.
       
       @author Thomas Feng
       @version $Id$
       @since Ptolemy II 5.1
       @Pt.ProposedRating Red (tfeng)
       @Pt.AcceptedRating Red (tfeng)
    */
    private class RecordList {
        
        /** Construct a record list object with a record stored in it.
         * 
         *  @param record
         */
        RecordList(Record record) {
            _record = record;
        }
        
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
         *  @see #_setPrevious(RecordList)
         */
        protected void _setNext(RecordList next) {
            _next = next;
            if (next != null)
                next._previous = this;
        }
        
        /** Set the record list previous to this one. Its
         *  next record list is also set to this one.
         *  
         *  @param previous The previous record list.
         *  @see #_setNext(RecordList)
         */
        protected void _setPrevious(RecordList previous) {
            _previous = previous;
            if (previous != null)
                previous._next = this;
        }
        
        /** The record.
         */
        private Record _record;
        
        /** The record list previous to this one.
         */
        private RecordList _previous = null;
        
        /** The record list next to this one.
         */
        private RecordList _next = null;
    }

    /** Add a record to the list at the given index.
     * 
     *  @param indices The index.
     *  @param record The record.
     */
    private void _addRecord(int index, Record record) {
        RecordList list = new RecordList(record);
        list._setNext(_records[index]);
        _records[index] = list;
        _totalNum++;
    }
    
    /** The record lists for all the dimensions.
     */
    private RecordList[] _records;
    
    /** The total number of records in all the dimensions. Must be
     *  explicitly managed when records are added or removed.
     */
    private int _totalNum = 0;
}
