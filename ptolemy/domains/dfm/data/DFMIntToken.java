/* Int token class for DFM domain.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.dfm.data;

//////////////////////////////////////////////////////////////////////////
//// DFMIntToken
/** 
 This is the int token class to be used in DFM domain.  It contains
 an int as its value.  The value must be set on constructor.  To get
 the int value, use intValue().
 <p>
@author  William Wu
@version $id$
*/
public class DFMIntToken extends DFMToken {

    /** Constructor
     * @param tag tag of the token
     * @param value double value of this token
     */	
    public DFMIntToken(String tag, int value) {
        super(tag);
        _intValue = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the int value carried on this token.
     * @return int value on this token.
     */	
    public int intValue(){
        return _intValue;
    }

    public Object getData(){
        return (Object)(new Integer(_intValue));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _intValue;
}
