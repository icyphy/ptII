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

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// FieldRecordArray
/**
 
 
 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public final class FieldRecordArray extends Vector {
    
    public FieldRecordArray getArray(int index) {
        FieldRecordArray array = (FieldRecordArray)get(index);
        if (array == null) {
            array = new FieldRecordArray();
            setArray(index, array);
        }
        return array;
    }

    public FieldRecord getRecord(int index) {
        FieldRecord record = (FieldRecord)get(index);
        if (record == null) {
            record = new FieldRecord();
            setRecord(index, record);
        }
        return record;
    }
    
    public void setArray(int index, FieldRecordArray array) {
        set(index, array);
    }

    public void setRecord(int index, FieldRecord record) {
        set(index, record);
    }
    
    public void setSize(int minSize) {
        if (minSize > size())
            super.setSize(minSize);
    }

    public void setSize(boolean[] array, int maxDepth) {
        if (array != null && maxDepth > 0)
            setSize(array.length);
    }
    
    public void setSize(byte[] array, int maxDepth) {
        if (array != null && maxDepth > 0)
            setSize(array.length);
    }
    
    public void setSize(char[] array, int maxDepth) {
        if (array != null && maxDepth > 0)
            setSize(array.length);
    }
    
    public void setSize(double[] array, int maxDepth) {
        if (array != null && maxDepth > 0)
            setSize(array.length);
    }
    
    public void setSize(float[] array, int maxDepth) {
        if (array != null && maxDepth > 0)
            setSize(array.length);
    }
    
    public void setSize(int[] array, int maxDepth) {
        if (array != null && maxDepth > 0)
            setSize(array.length);
    }
    
    public void setSize(long[] array, int maxDepth) {
        if (array != null && maxDepth > 0)
            setSize(array.length);
    }
    
    public void setSize(short[] array, int maxDepth) {
        if (array != null && maxDepth > 0)
            setSize(array.length);
    }
    
    public void setSize(Object[] array, int maxDepth) {
        if (array == null || maxDepth < 1)
            return;
        
        setSize(array.length);
        
        for (int i = 0; maxDepth > 1 && i < array.length; i++) {
            Object element = array[i];
            if (element == null)
                continue;
            else if (element instanceof boolean[])
                getArray(i).setSize(((boolean[])element).length);
            else if (element instanceof byte[])
                getArray(i).setSize(((byte[])element).length);
            else if (element instanceof char[])
                getArray(i).setSize(((char[])element).length);
            else if (element instanceof double[])
                getArray(i).setSize(((double[])element).length);
            else if (element instanceof float[])
                getArray(i).setSize(((float[])element).length);
            else if (element instanceof int[])
                getArray(i).setSize(((int[])element).length);
            else if (element instanceof long[])
                getArray(i).setSize(((long[])element).length);
            else if (element instanceof short[])
                getArray(i).setSize(((short[])element).length);
            else if (element instanceof Object[])
                getArray(i).setSize((Object[])element, maxDepth - 1);
        }
    }
}
