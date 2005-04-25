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

import ptolemy.backtrack.util.FieldRecord.RecordList;

//////////////////////////////////////////////////////////////////////////
//// FieldRecordState
/**


@author Thomas Feng
@version $Id$
@since Ptolemy II 5.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class FieldRecordState {

    protected FieldRecordState(int dimensions) {
        _records = new RecordList[dimensions + 1];
    }

    protected int _decreaseTotalNum() {
        return --_totalNum;
    }

    protected int _getIdentifier() {
        return _identifier;
    }

    protected RecordList[] _getRecords() {
        return _records;
    }

    protected int _getTotalNum() {
        return _totalNum;
    }

    protected int _increaseIdentifier() {
        return ++_identifier;
    }

    protected int _increaseTotalNum() {
        return ++_totalNum;
    }

    protected void _setTotalNum(int totalNum) {
        _totalNum = totalNum;
    }

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
