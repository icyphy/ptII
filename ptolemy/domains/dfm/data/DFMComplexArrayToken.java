/* Complex array token class for DFM domain.

 Copyright (c) 1998 The Regents of the University of California.
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

import ptolemy.math.Complex;

//////////////////////////////////////////////////////////////////////////
//// DFMComplexArrayToken
/** 
 This is the complex array token class to be used in DFM domain.  It contains
 an array of complexes as its value.  The value must be set on constructor.  To get
 the complex array value, use complexArrayValue().
 <p>
@author  William Wu
@version $id$
*/
public class DFMComplexArrayToken extends DFMToken {

    /** Constructor
     * @param tag tag of the token
     * @param value complex array value of this token
     */	
    public DFMComplexArrayToken(String tag, Complex [] value) {
        super(tag);
        _complexArrayValue = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the complex array value carried on this token.
     * @return complex array value on this token.
     */	
    public Complex [] complexArrayValue(){
        return _complexArrayValue;
    }

    public Object getData(){
        return _complexArrayValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Complex [] _complexArrayValue;
}
