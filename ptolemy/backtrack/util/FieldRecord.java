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
import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// FieldRecord
/**
   The records (or change history) of a field in a class. In a Java program
   refactored with {@link ptolemy.backtrack.ast.Transformer} to support
   backtracking, every change on a field by means of assignment is recorded.
   The old value of the field is stored in a <tt>FieldRecord</tt>. Every
   entry in the record includes the information of the timestamp (see {@link
   ptolemy.backtrack.Checkpoint}) denoting the time when the entry was created, 
   as well as the old value of the field.
   <p>
   It is possible the records of a field have multiple dimensions, when the
   field is an array. In that case, records of different dimensions are stored
   in different tables.
 
   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public final class FieldRecord {
    
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
     *  
     *  @return The iterator.
     *  @see #iterator()
     */
    public Iterator iterator(int index) {
        if (index >= _records.size())
            _records.setSize(index + 1);
        return new IndividualIterator(index);
    }
    
    public class CombinedIterator implements Iterator {

        public boolean hasNext() {
            return _currentNum < _totalNum;
        }
        
        public Object next() {
            _currentNum++;
            _lastIndex = _maxTimestampIndex();
            _lastRecord = _currentRecords[_lastIndex];
            _currentRecords[_lastIndex] = 
                _currentRecords[_lastIndex].getNext();
            return _lastRecord.getRecord();
        }
        
        public void remove() {
            RecordList previous = _lastRecord.getPrevious();
            if (previous == null) {  // The first one.
                RecordList first = _lastRecord.getNext();
                if (first != null)
                    first.setPrevious(null);
                _records.set(_lastIndex, first);
            } else
                previous.setNext(_lastRecord.getNext());
            _lastRecord = null;
            _totalNum--;
            _currentNum--;
        }
        
        protected CombinedIterator() {
            int indices = _records.size();
            _currentRecords = new RecordList[indices];
            for (int i = 0; i < indices; i++) {
                RecordList list = (RecordList)_records.get(i);
                _currentRecords[i] = list;
            }
        }
        
        private int _maxTimestampIndex() {
            int max = -1;
            int maxIndex = -1;
            for (int i = 0; i < _currentRecords.length; i++) {
                RecordList list = _currentRecords[i];
                int timestamp;
                if (list != null && 
                        (timestamp = list.getRecord().getTimestamp()) > max) {
                    max = timestamp;
                    maxIndex = i;
                }
            }
            return maxIndex;
        }
        
        private RecordList[] _currentRecords;
        
        private int _currentNum = 0;
        
        private int _lastIndex;
        
        private RecordList _lastRecord;
    }
    
    public class IndividualIterator implements Iterator {
        
        public boolean hasNext() {
            return _currentList != null;
        }
        
        public Object next() {
            _lastRecord = _currentList;
            _currentList = _currentList.getNext();
            return _lastRecord.getRecord();
        }
        
        public void remove() {
            RecordList previous = _lastRecord.getPrevious();
            if (previous == null) {
                if (_currentList != null)
                    _currentList.setPrevious(null);
                _records.set(_index, _currentList);
            } else
                previous.setNext(_currentList);
            _totalNum--;
        }
        
        protected IndividualIterator(int index) {
            _index = index;
            _currentList = (RecordList)_records.get(index);
        }
        
        private int _index;
        
        private RecordList _currentList;
        
        private RecordList _lastRecord;
    }
    
    public class Record {
        
        public int[] getIndices() {
            return _indices;
        }
        
        public int getTimestamp() {
            return _timestamp;
        }
        
        public Object getValue() {
            return _value;
        }
        
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
        
        protected Record(int[] indices, Object value, int timestamp) {
            _indices = indices;
            _value = value;
            _timestamp = timestamp;
        }
        
        private int[] _indices;
        
        private Object _value;
        
        private int _timestamp;
    }
    
    private class RecordList {
        
        protected RecordList(Record record) {
            _record = record;
        }
        
        protected RecordList getNext() {
            return _next;
        }
        
        protected RecordList getPrevious() {
            return _previous;
        }
        
        protected Record getRecord() {
            return _record;
        }
        
        protected void setNext(RecordList next) {
            _next = next;
            if (next != null)
                next._previous = this;
        }
        
        protected void setPrevious(RecordList previous) {
            _previous = previous;
            if (previous != null)
                previous._next = this;
        }
        
        private Record _record;
        
        private RecordList _previous = null;
        
        private RecordList _next = null;
        
        private RecordList _end = null;
    }

    private void _addRecord(int indices, Record record) {
        if (_records.size() < indices + 1)
            _records.setSize(indices + 1);
        RecordList list = new RecordList(record);
        list.setNext((RecordList)_records.get(indices));
        _records.set(indices, list);
        _totalNum++;
    }
    
    private Vector _records = new Vector();
    
    private int _totalNum = 0;
}
