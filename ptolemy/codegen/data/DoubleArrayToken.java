/* A token that contains an array of Double tokens.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.codegen.data;

import ptolemy.kernel.util.IllegalActionException;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;


//////////////////////////////////////////////////////////////////////////
//// DoubleArrayToken
/**
A token that contains an array of double tokens.
<p>This class is a temporary hack for getting codegen
working.
@author Christopher Hylands
@version $Id$
*/

public class DoubleArrayToken extends ArrayToken {

    /** Construct an ArrayToken with the specified token array. All the
     *  tokens in the array must have the same type, otherwise an
     *  exception will be thrown.
     *  @param value An array of tokens.
     *  @exception IllegalActionException If the tokens in the array
     *   do not have the same type.
     */
    public DoubleArrayToken(Token[] value) throws IllegalActionException {
	super(value);
    }

    public DoubleArrayToken(DoubleToken[] value) throws IllegalActionException {
	super(value);
    }

    public DoubleArrayToken(double[] value) throws IllegalActionException {
	// SpecializedTokenVisitor._visitCastNode() needs this constructor
	super(new DoubleToken[]{new DoubleToken(value[0])});

	// FIXME: what if the array is greater than length 1?
	if (value.length != 1) {
	    // FIXME: is this the right exception?
	    throw new RuntimeException("DoubleArrayToken(double[] value): "
				       + "length is ' " + value.length 
				       + "', which is != 1");
	}

    }

    //public DoubleArrayToken(double value) {
	// SpecializedTokenVisitor._visitCastNode() needs this constructor
    //super(new DoubleToken[]{new DoubleToken(value)});
    //}
}
