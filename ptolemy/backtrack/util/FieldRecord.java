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

package ptolemy.backtrack.util;

import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// FieldRecord
/**
 
 
  @author Thomas Feng
  @version $Id$
  @since Ptolemy II 5.1
  @Pt.ProposedRating Red (tfeng)
  @Pt.AcceptedRating Red (tfeng)
*/
public class FieldRecord {
    
    public void add(boolean value, int timestamp) {
        _records.add(new Record(new Boolean(value), timestamp));
    }
    
    public void add(byte value, int timestamp) {
        _records.add(new Record(new Byte(value), timestamp));
    }
    
    public void add(char value, int timestamp) {
        _records.add(new Record(new Character(value), timestamp));
    }
    
    public void add(double value, int timestamp) {
        _records.add(new Record(new Double(value), timestamp));
    }
    
    public void add(float value, int timestamp) {
        _records.add(new Record(new Float(value), timestamp));
    }
    
    public void add(int value, int timestamp) {
        _records.add(new Record(new Integer(value), timestamp));
    }
    
    public void add(long value, int timestamp) {
        _records.add(new Record(new Long(value), timestamp));
    }
    
    public void add(short value, int timestamp) {
        _records.add(new Record(new Short(value), timestamp));
    }
    
    public void add(Object value, int timestamp) {
        _records.add(new Record(value, timestamp));
    }
    
    public List getRecords() {
        return _records;
    }
    
    public class Record {
        
        public int getTimestamp() {
            return _timestamp;
        }
        
        public Object getValue() {
            return _value;
        }
        
        protected Record(Object value, int timestamp) {
            _value = value;
            _timestamp = timestamp;
        }
        
        private Object _value;
        
        private int _timestamp;
    }
    
    private List _records = new LinkedList();
}
