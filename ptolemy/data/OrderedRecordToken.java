/* A token that contains a set of label/token pairs - maintaining the original order.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.data;

import java.util.LinkedHashMap;
import java.util.Map;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// OrderedRecordToken

/**
 A token that contains a set of label/token pairs. Operations on record
 tokens result in new record tokens containing only the common fields,
 where the operation specifies how to combine the data in the common
 fields.  Thus, for example, if two record tokens
 are added or subtracted, then common records
 (those with the same labels) will be added or subtracted,
 and the disjoint records will not appear in the result.
 
 This implementation maintains the order of the entries as they were added

 @author Ben Leinfelder
 @version $Id$
 @Pt.ProposedRating yellow (leinfelder)
 @Pt.AcceptedRating red (leinfelder)
 */
public class OrderedRecordToken extends RecordToken {
	
	/**
	 * Default constructor
	 * @see RecordToken
	 */
	public OrderedRecordToken() {
		super();
	}
	/**
	 * @see RecordToken
	 * @param fieldMap
	 * @throws IllegalActionException
	 */
	public OrderedRecordToken(Map fieldMap) throws IllegalActionException {
		super(fieldMap);
	}
	/**
	 * @see RecordToken
	 * @param init
	 * @throws IllegalActionException
	 */
	public OrderedRecordToken(String init) throws IllegalActionException {
		super(init);
	}
	/**
	 * @see RecordToken
	 * @param labels
	 * @param values
	 * @throws IllegalActionException
	 */
	public OrderedRecordToken(String[] labels, Token[] values) throws IllegalActionException {
		super(labels, values);
	}
	
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
	/**
	 * Using a LinkedHashMap so that the original order of the record is maintained
	 */
    protected void _initializeStorage() {
    	_fields = new LinkedHashMap();
    }

}
