/* Double token class for DFM domain.

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
//// DFMDoubleToken
/** 
 This is the double token class to be used in DFM domain.  It contains
 a double as its value.  The value must be set on constructor.  To get
 the double value, use doubleValue().
 <p>
@author  William Wu
@version $id$
*/
public class DFMDoubleToken extends DFMToken {

    /** Constructor
     * @param tag tag of the token
     * @param value double value of this token
     */	
    public DFMDoubleToken(String tag, double value) {
        super(tag);
        _doubleValue = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the double value carried on this token.
     * @return double value on this token.
     */	
    public double doubleValue(){
        return _doubleValue;
    }

    public Object getData(){
        return (Object)(new Double(_doubleValue));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _doubleValue;
}
